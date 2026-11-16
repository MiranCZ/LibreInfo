package io.github.mirancz.libreinfo.util.request

import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.DebugFlags

/**
 * A URL the app talks to, plus a [Name] the error UI turns into user-facing text app-side
 * (see `ui/AppErrorUi.kt`).
 */
class Endpoint(@JvmField val url: String, @JvmField val name: Name) {

    sealed interface Name {
        data object StaticGtfs : Name
        data object AppServer : Name
        data object GithubApi : Name

        /** Ad-hoc endpoints (e.g. the APK download) that just carry a label. */
        data class Literal(val label: String) : Name
    }

    fun resolve(vararg subdomains: String): Endpoint =
        Endpoint(url + subdomains.joinToString("") { "/$it" }, name)

    override fun toString(): String = url

    companion object {
        @JvmField
        val STATIC_GTFS = if (DebugFlags.shouldUseLocalhost()) {
            Endpoint("http://localhost:8080/static", Name.StaticGtfs)
        } else {
            Endpoint("https://api.libre-info.com/static", Name.StaticGtfs)
        }

        @JvmField
        val APP_SERVER = if (DebugFlags.shouldUseLocalhost()) {
            Endpoint("http://localhost:8080", Name.StaticGtfs)
        } else {
            Endpoint("https://api.libre-info.com", Name.StaticGtfs)
        }

        @JvmField
        val GITHUB_API = Endpoint("https://api.github.com", Name.GithubApi)
    }
}
