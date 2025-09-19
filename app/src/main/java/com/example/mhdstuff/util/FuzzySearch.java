package com.example.mhdstuff.util;


import java.text.Normalizer;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FuzzySearch<T> {


    private static final BiPredicate<String, String> STRICT = String::startsWith;
    private static final BiPredicate<String, String> LOOSE = String::contains;
    private static final BiPredicate<String, String> WORD_START = (s, input) -> {
        if (!input.contains(" ")) return false;

        String[] inputParts = input.split(" ");
        String[] parts = s.split(" ");
        if (parts.length < inputParts.length) return false;

        for (int i = 0; i < inputParts.length; i++) {
            String part = inputParts[i];

            if (!parts[i].startsWith(part)) return false;
        }

        return true;
    };


    private final Map<String, T> map = new HashMap<>();
    private final List<String> items = new ArrayList<>();
    private final List<String> lowItems;
    private final List<String> normalizedItems;
    private final int longest;

    public FuzzySearch(List<T> items, Function<T, String> mapper) {
        long start = System.currentTimeMillis();

        int longest = 0;
        for (T item : items) {
            String mapped = mapper.apply(item);
            longest = Math.max(longest, mapped.length());

            map.put(mapped, item);
            this.items.add(mapped);
        }
        this.longest = longest;

        this.items.sort(Comparator.comparing(String::toLowerCase));

        this.lowItems = new ArrayList<>();
        this.normalizedItems = new ArrayList<>();
        for (String item : this.items) {
            lowItems.add(item.toLowerCase());
            normalizedItems.add(normalize(item));
        }

        System.out.println("BUILD IN "+(System.currentTimeMillis()-start));
    }


    public List<T> getResults(String input) {
        if (input.length() > longest) return List.of();

        String lowInput = input.toLowerCase();
        String normalizedInput = normalize(input);

        final class Result {
            final List<String> full = new ArrayList<>();
            final List<String> low = new ArrayList<>();
            final List<String> normalized = new ArrayList<>();
            final BiPredicate<String, String> predicate;

            Result(BiPredicate<String, String> predicate) {
                this.predicate = predicate;
            }

            boolean test(int index) {
                if (predicate.test(items.get(index), input)) {
                    full.add(items.get(index));
                    return true;
                }
                if (predicate.test(lowItems.get(index), lowInput)) {
                    low.add(items.get(index));
                    return true;
                }
                if (predicate.test(normalizedItems.get(index), normalizedInput)) {
                    normalized.add(items.get(index));
                    return true;
                }

                return false;
            }

            void addTo(List<T> output) {
                output.addAll(full.stream().map(map::get).collect(Collectors.toList()));
                output.addAll(low.stream().map(map::get).collect(Collectors.toList()));
                output.addAll(normalized.stream().map(map::get).collect(Collectors.toList()));
            }
        }

        List<Result> results = new ArrayList<>();
        results.add(new Result(STRICT));
        results.add(new Result(LOOSE));
//        results.add(new Result(WORD_START)); // TODO implement more efficiently


        long m = System.currentTimeMillis();
        for (int i = 0; i < items.size(); i++) {
            for (Result result : results) {
                if (result.test(i)) {
                    break;
                }
            }
        }
        System.out.println("Loop took " + (System.currentTimeMillis() - m));


        List<T> result = new ArrayList<>();
        for (Result res : results) {
            res.addTo(result);
        }

        return result;
    }

    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static String normalize(String str) {
        str = Normalizer.normalize(str.toLowerCase(), Normalizer.Form.NFD).toLowerCase();
        str = pattern.matcher(str).replaceAll("");

        return str;

    }

}
