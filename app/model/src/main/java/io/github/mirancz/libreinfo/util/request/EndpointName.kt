package io.github.mirancz.libreinfo.util.request

sealed interface EndpointName {
    data object StaticGtfs : EndpointName
    data object AppServer : EndpointName
    data object GithubApi : EndpointName

    /** Ad-hoc endpoints (e.g. the APK download) that just carry a label. */
    data class Literal(val label: String) : EndpointName
}