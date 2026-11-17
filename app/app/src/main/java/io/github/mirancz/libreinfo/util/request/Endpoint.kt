package io.github.mirancz.libreinfo.util.request

import io.github.mirancz.libreinfo.DebugFlags

/**
 * A URL the app talks to, plus a [EndpointName] the error UI turns into user-facing text app-side
 * (see `ui/AppErrorUi.kt`).
 */
class Endpoint(@JvmField val url: String, @JvmField val name: EndpointName) {

    fun resolve(vararg subdomains: String): Endpoint =
        Endpoint(url + subdomains.joinToString("") { "/$it" }, name)

    override fun toString(): String = url

    companion object {
        @JvmField
        val STATIC_GTFS = if (DebugFlags.shouldUseLocalhost()) {
            Endpoint("http://localhost:8080/static", EndpointName.StaticGtfs)
        } else {
            Endpoint("https://api.libre-info.com/static", EndpointName.StaticGtfs)
        }

        @JvmField
        val APP_SERVER = if (DebugFlags.shouldUseLocalhost()) {
            Endpoint("http://localhost:8080", EndpointName.StaticGtfs)
        } else {
            Endpoint("https://api.libre-info.com", EndpointName.StaticGtfs)
        }

        @JvmField
        val GITHUB_API = Endpoint("https://api.github.com", EndpointName.GithubApi)
    }
}
