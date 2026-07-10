package io.github.mirancz.libreinfo.util

import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import kotlinx.coroutines.yield
import java.text.CollationKey
import java.text.Collator
import java.text.Normalizer
import java.util.Locale
import java.util.PriorityQueue
import kotlin.math.max

/// THIS CODE HAS BEEN AI GENERATED
/**
 * Fuzzy search over Czech stop names.
 *
 * Examples below use abstract placeholder names ("Axyz Bcde", "Abcov", ...);
 * lowercase strings in quotes are queries, capitalised ones are stop names.
 *
 * Matching model
 * --------------
 *  - Characters are compared on three levels: folded (case- and accent-less),
 *    accent-aware, and exact. A folded match always counts; matching accents/case
 *    only adds bonus points. So "c" finds "č" but ranks plain "c" higher, and
 *    typing "č" ranks "č" higher than "c".
 *  - A space in the query is a token separator, not a character: "a b" is two
 *    independent sub-queries "a" and "b" (fzf-style AND). Each token is aligned
 *    on its own, so "a b" prefers word starts ("Axyz Bcde") over the literal
 *    substring "a B" ("Wxyza Bcdef"), and the length of skipped word
 *    remainders costs nothing. Tokens may match in any order, but matches in
 *    the typed order earn BONUS_TOKENS_IN_ORDER, so "a b" ranks "Axyz Bcde"
 *    above names where the b-word comes first, while "bcde axyz" still finds
 *    "Axyz Bcde" (just without the nudge). If the independent per-token optima
 *    collide on the same word (e.g. mid-typing "axyz a" on the way to
 *    "axyz abcd"), an ordered non-overlapping retry lets a later word win
 *    together with the bonus.
 *  - Each token is scored with a semi-global, gap-affine alignment
 *    (Smith–Waterman family, like fzf / editor command palettes):
 *      * word-start bonus, larger for earlier words -> "abcd" ranks
 *        "Abcdov Xyz" above "Zzz, abcdov"
 *      * gaps are cheap inside a word ("abd" finds "Abcdov") but NEVER cross
 *        a word boundary: abbreviating several words needs spaces ("abc de",
 *        not "abcde"), so a spaceless query cannot stitch together the starts
 *        of different words.
 *      * mismatch penalty  -> typos like "axcde" still find "abcde"
 *
 * Favourites
 * ----------
 * Favourites are matched exactly like everything else, but routed into their
 * own section. A favourite is *pinned* into `favourites` only when its score
 * is competitive with the best match of this query (>= FAVOURITE_PIN_PERCENT
 * of it); weaker favourites are not dropped -- they compete in the normal
 * `others` ranking like any stop. So typing "abc" pins a favourite "Abcov"
 * but lets a favourite that only scrapes past the threshold (a scattered,
 * typo-assisted hit) sink to wherever a weak subsequence match belongs. The
 * percentage is relative to the query's best result, so typo-ridden queries
 * (where every score drops) still pin their favourites. Favourites live
 * outside the index (a predicate you pass per call), so toggling one costs
 * nothing.
 *
 * Usage
 * -----
 *   val search = FuzzyStopSearch(stops)      // build once, e.g. in a ViewModel
 *   val res = search.search("a b", limit = 50) { it.id in favouriteIds }
 *   res.favourites                           // competitive favourites, best first
 *   res.others                               // top `limit` of everything else
 *
 * Tuning: only the *relative* sizes of the constants below matter. Invariant
 * to keep: a word-start bonus that is only reachable through a gap (word 2+,
 * i.e. at most BONUS_WORD_START + BONUS_EARLY_WORD - EARLY_WORD_DECAY) minus
 * |PENALTY_GAP_OPEN| must stay below BONUS_CONSECUTIVE, so an unbroken run
 * ("abc" in "Abcov") always beats the same letters scattered across word
 * starts ("ab…c" in "Abov Cov"). If typos match too eagerly, lower
 * PENALTY_MISMATCH (more negative) or reduce maxTyposFor().
 */
class FuzzyStopSearch(stops: List<Stop>) {


    /** Two UI sections: pinned favourites and the best of the rest. */
    data class Results(val favourites: List<Stop>, val others: List<Stop>)

    // ------------------------------------------------------------- tuning ---
    private companion object {
        const val SCORE_MATCH = 16   // folded characters are equal
        const val BONUS_SAME_ACCENT = 2    // accents match too (query "č" on "č")
        const val BONUS_SAME_CASE = 1    // smart-case: only for UPPERCASE query chars
        const val BONUS_WORD_START = 12   // match at the start of any word
        const val BONUS_EARLY_WORD = 6    // extra on top for early words: +6, +4, +2, 0…
        const val EARLY_WORD_DECAY = 2
        const val BONUS_CONSECUTIVE = 12   // per char continuing an unbroken run
        const val PENALTY_MISMATCH = -10  // substituted character (typo)
        const val PENALTY_GAP_OPEN = -6   // start skipping characters of the name
        const val PENALTY_GAP_EXT = -2   // ...keep skipping
        const val PENALTY_QUERY_SKIP = -12  // query char that matches nothing (extra char)
        const val NEG = Int.MIN_VALUE / 2   // avoid underflows

        /**
         * Flat bonus when a multi-token query's tokens match the name in the
         * typed order (strictly increasing match positions). Breaks the tie
         * between "Axyz Bcde" and a reversed hit for "a b" without hiding
         * out-of-order matches.
         */
        const val BONUS_TOKENS_IN_ORDER = 10

        /**
         * A favourite is pinned on top only if it reaches this percentage of
         * the query's best score; weaker favourites rank among `others`.
         * Raise for a stricter favourites section, lower for a stickier one.
         */
        const val FAVOURITE_PIN_PERCENT = 50

        val WHITESPACE = Regex("\\s+")
        private val foldCache = CharArray(65536) { '\u0000' }

        /**
         * Lowercase + strip diacritics of a single char; non-alphanumerics
         * (space, '-', '/', '.', ...) become ' ' so they all act as word
         * separators. Per-char folding keeps indices aligned with the original
         * string, which the accent/case bonuses rely on. Works for the whole
         * Czech alphabet (á č ď é ě í ň ó ř š ť ú ů ý ž) via NFD decomposition.
         */
        fun foldChar(c: Char): Char {
            val code = c.code
            val cached = foldCache[code]
            if (cached != '\u0000') return cached

            val d = Normalizer.normalize(c.toString(), Normalizer.Form.NFD)
            val base = d.firstOrNull { it.category != CharCategory.NON_SPACING_MARK } ?: c
            val result = if (base.isLetterOrDigit()) base.lowercaseChar() else ' '

            foldCache[code] = result
            return result
        }

        /** Bit per character class, used for the cheap prefilter. */
        fun maskBit(c: Char): Long = when (c) {
            in 'a'..'z' -> 1L shl (c - 'a')
            in '0'..'9' -> 1L shl (26 + (c - '0'))
            else -> 1L shl 37
        }

        fun maxTyposFor(queryLen: Int) = when {
            queryLen <= 3 -> 0
            queryLen <= 6 -> 1
            else -> 2
        }
    }

    /** One space-separated part of the query, precomputed in all three forms. */
    private class Token(val raw: CharArray, val lower: CharArray, val folded: CharArray)

    private class Entry(
        val stop: Stop,
        val original: String,
        val lower: CharArray,        // lowercased, accents preserved
        val folded: CharArray,       // lowercased, accents stripped, punctuation -> ' '
        val boundaryBonus: IntArray, // >0 where a word begins; larger for earlier words
        val mask: Long,              // which folded chars occur at all (prefilter)
        val sortKey: CollationKey    // precomputed Czech collation key (fast tiebreaks)
    )

    /** A scored entry kept for output, before it becomes a Result. */
    private class Match(val entry: Entry, val score: Int)

    private val entries: List<Entry> = run {
        // Collator is only needed here: comparisons at query time use the
        // precomputed CollationKeys, which are ~100x cheaper than compare().
        val collator = Collator.getInstance(Locale.forLanguageTag("cs-CZ"))

        stops.map { stop ->
            val name = stop.name
            val lower = CharArray(name.length) { name[it].lowercaseChar() }
            val folded = CharArray(name.length) { foldChar(name[it]) }
            val boundaryBonus = IntArray(name.length)
            var mask = 0L
            var word = 0
            var lastNonSpaceCol = 0
            for (j in name.indices) {
                if (folded[j] == ' ') continue

                mask = mask or maskBit(folded[j])
                if (j == 0 || folded[j - 1] == ' ') {
                    boundaryBonus[j] =
                        BONUS_WORD_START + (BONUS_EARLY_WORD - EARLY_WORD_DECAY * word).coerceAtLeast(
                            0
                        )
                    word++
                }
                lastNonSpaceCol = j + 1   // as a DP column index
            }
            Entry(
                stop, name, lower, folded, boundaryBonus, mask, collator.getCollationKey(name)
            )
        }
    }
    private val maxNameLen: Int = entries.maxOfOrNull { it.original.length } ?: 0

    /** Cached all stops sorted alphabetically */
    private val allByName: List<Stop> by lazy {
        entries.sortedWith(compareBy { it.sortKey }).map { it.stop }
    }

    /** score descending, then Czech-aware alphabetical */
    private val resultOrder = Comparator<Match> { a, b ->
        val byScore = b.score.compareTo(a.score)
        if (byScore != 0) byScore else a.entry.sortKey.compareTo(b.entry.sortKey)
    }

    /** Heap comparator: root of the bounded heap is the WORST kept match. */
    private val worstFirst = resultOrder.reversed()

    /**
     * Returns competitive favourites and the best [limit] other matches, each
     * section best first. Call from a background thread. An empty query
     * returns the full alphabetical list split into the same two sections
     * (browse mode), ignoring [limit].
     */

    suspend fun search(
        rawQuery: String,
        limit: Int = 50,
        isFavourite: (Stop) -> Boolean = { false },
    ): Results {
        val cleaned = rawQuery.trim().replace(WHITESPACE, " ")
        if (cleaned.isEmpty()) {
            val (fav, rest) = allByName.partition { isFavourite(it) }

            return Results(fav, rest)
        }

        if (limit <= 0) return Results(emptyList(), emptyList())

        // A space separates tokens; each token is matched independently, so it is never aligned as a character.
        val tokens = cleaned.split(' ').map { t ->
            Token(
                raw = t.toCharArray(),
                lower = CharArray(t.length) { t[it].lowercaseChar() },
                folded = CharArray(t.length) { foldChar(t[it]) },
            )
        }

        var qMask = 0L
        for (t in tokens) for (c in t.folded) qMask = qMask or maskBit(c)

        val totalLen = tokens.sumOf { it.raw.size }
        val maxTypos = maxTyposFor(totalLen)
        // Every query char should roughly earn a match; allow a few typos.
        val minScore = (totalLen - maxTypos) * SCORE_MATCH + maxTypos * PENALTY_MISMATCH

        // DP row buffers and per-token results, reused across all entries
        // (search() uses only locals, so concurrent calls stay safe)
        val hA = IntArray(maxNameLen + 1);
        val hB = IntArray(maxNameLen + 1)
        val mA = IntArray(maxNameLen + 1);
        val mB = IntArray(maxNameLen + 1)
        val tokScore = IntArray(tokens.size)
        val tokEnd = IntArray(tokens.size)

        // One scan, two collectors: favourites are few, so they are collected
        // unbounded (pinning is decided afterwards, relative to bestScore);
        // everything else goes through the bounded top-k heap where a losing
        // candidate costs one primitive int compare (the CollationKey is only
        // consulted on score ties) and allocates nothing.
        val favMatches = ArrayList<Match>()
        val heap = PriorityQueue(limit + 1, worstFirst)
        var bestScore = 0

        for (e in entries) {
            // give control back to the UI (if this search has been canceled)
            yield()

            // Prefilter: skip names missing more distinct query chars than the typo budget allows.
            if ((qMask and e.mask.inv()).countOneBits() > maxTypos) continue

            // Pass 1: each token takes its own best match, independently.
            var freeSum = 0
            var prevEnd = 0
            var inOrder = true
            for (k in tokens.indices) {
                val t = tokens[k]
                val packed = align(t.raw, t.lower, t.folded, e, 0, hA, hB, mA, mB)
                val s = (packed shr 32).toInt()
                val end = packed.toInt()       // column where this token's best match ends
                tokScore[k] = s
                tokEnd[k] = end
                freeSum += s
                if (end > prevEnd) prevEnd = end else inOrder = false
            }
            if (freeSum < minScore) continue

            // Ranking nudges only -- applied after the threshold, so the set of
            // matching stops is unchanged; they merely reorder it.
            var score = freeSum
            if (tokens.size > 1) {
                if (inOrder) {
                    score += BONUS_TOKENS_IN_ORDER
                } else {
                    // Pass 2 (rare): the free optima are out of order -- often
                    // because two tokens grabbed the same word (mid-typing
                    // "axyz a"). Retry left-to-right, each token constrained to
                    // start after the previous one's end, and keep the ordered,
                    // non-overlapping reading if it scores better overall.
                    var seq = tokScore[0]
                    var start = tokEnd[0]
                    var alive = true
                    for (k in 1 until tokens.size) {
                        yield()

                        val t = tokens[k]
                        val packed = align(t.raw, t.lower, t.folded, e, start, hA, hB, mA, mB)
                        val s = (packed shr 32).toInt()
                        if (s < NEG / 4) {
                            alive = false; break
                        }  // nothing left there
                        seq += s
                        start = packed.toInt()
                    }
                    if (alive) {
                        score = max(score, seq + BONUS_TOKENS_IN_ORDER)
                    }
                }
            }
            bestScore = max(bestScore, score)

            if (isFavourite(e.stop)) {
                favMatches.add(Match(e, score))
            } else if (heap.size < limit) {
                heap.offer(Match(e, score))
            } else {
                val match = Match(e, score)
                val worst: Match = heap.peek()

                if (resultOrder.compare(match, worst) < 0) {
                    heap.poll()
                    heap.offer(match)
                }
            }
        }

        // Pin only favourites competitive with the best match of this query;
        // the rest are still matches, they just rank normally in `others`
        // instead of squatting at the top of the screen.
        val pinned = ArrayList<Match>(favMatches.size)
        for (f in favMatches) {
            if (f.score * 100 >= bestScore * FAVOURITE_PIN_PERCENT) {
                pinned.add(f)
            } else if (heap.size < limit) {
                heap.offer(f)
            } else if (resultOrder.compare(f, heap.peek()) < 0) {
                heap.poll()
                heap.offer(f)
            }
        }

        pinned.sortWith(resultOrder)

        val top = ArrayList(heap)
        top.sortWith(resultOrder)

        return Results(
            pinned.map { it.entry.stop },
            top.map { it.entry.stop },
        )
    }

    /**
     * Semi-global gap-affine alignment: one whole token is aligned against the
     * best-fitting part of the name. Leading/trailing name characters are free,
     * so short tokens match long names; [startCol] > 0 restricts the free start
     * to columns at or after it (used by the ordered non-overlapping retry).
     * The token may start at any allowed word, but its matched span never
     * crosses a word boundary.
     *
     * Returns the best score and the DP column where that best alignment ends,
     * packed as (score shl 32) or endColumn -- used for the in-order bonus
     * without allocating or keeping instance state.
     */
    private fun align(
        qRaw: CharArray,
        qLower: CharArray,
        qFolded: CharArray,
        e: Entry,
        startCol: Int,
        hBufA: IntArray,
        hBufB: IntArray,
        mBufA: IntArray,
        mBufB: IntArray
    ): Long {
        var hPrev = hBufA;
        var hCur = hBufB   // H = best score at (i, j), any state
        var mPrev = mBufA;
        var mCur = mBufB   // M = best score ending in a match at (i, j)
        val m = qFolded.size
        val n = e.folded.size

        // hoisted out of the hot loop so ART can keep them in registers
        val ef = e.folded;
        val el = e.lower;
        val bb = e.boundaryBonus
        val name = e.original

        // row 0: nothing of the token consumed; free start at/after startCol
        val from0 = minOf(startCol, n + 1)
        if (from0 > 0) hPrev.fill(NEG, 0, from0)
        hPrev.fill(0, from0, n + 1)
        mPrev.fill(NEG, 0, n + 1)

        var best = NEG
        var bestEnd = 0
        for (i in 1..m) {
            hCur[0] = hPrev[0] + PENALTY_QUERY_SKIP   // token chars "before" the name
            mCur[0] = NEG
            var gap = NEG                             // affine gap state within this row
            val qf = qFolded[i - 1];
            val ql = qLower[i - 1];
            val qr = qRaw[i - 1]
            val qrUpper = qr.isUpperCase()

            for (j in 1..n) {
                val nc = ef[j - 1]
                // 1) q[i-1] aligned to name[j-1]: graded match or substitution
                mCur[j] = if (qf == nc) {
                    var s = SCORE_MATCH
                    if (ql == el[j - 1]) {
                        s += BONUS_SAME_ACCENT
                        // smart-case: lowercase input is case-neutral; typing
                        // "H" prefers "H" (Title-Case names aren't penalised)
                        if (qrUpper && qr == name[j - 1]) s += BONUS_SAME_CASE
                    }
                    s += bb[j - 1]
                    val from = maxOf(hPrev[j - 1], mPrev[j - 1] + BONUS_CONSECUTIVE)

                    s + from
                } else if (nc == ' ') {
                    NEG               // no substitution onto a word boundary
                } else {
                    hPrev[j - 1] + PENALTY_MISMATCH
                }
                // 2) skip name[j-1] -- gaps never cross a word boundary
                gap = if (nc == ' ') NEG
                else maxOf(hCur[j - 1] + PENALTY_GAP_OPEN, gap + PENALTY_GAP_EXT)
                // 3) skip q[i-1] (user typed an extra character)
                val skipQ = hPrev[j] + PENALTY_QUERY_SKIP

                hCur[j] = maxOf(mCur[j], gap, skipQ)
                if (i == m && hCur[j] > best) {       // free trailing chars
                    best = hCur[j]
                    bestEnd = j
                }
            }
            if (i == m && hCur[0] > best) {
                best = hCur[0]
                bestEnd = 0
            }

            val ht = hPrev; hPrev = hCur; hCur = ht
            val mt = mPrev; mPrev = mCur; mCur = mt
        }
        return (best.toLong() shl 32) or bestEnd.toLong()
    }
}