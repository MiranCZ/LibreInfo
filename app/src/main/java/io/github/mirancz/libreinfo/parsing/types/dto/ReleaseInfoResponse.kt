package io.github.mirancz.libreinfo.parsing.types.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseInfoResponse(val assets: List<AssetEntry>)

@Serializable
data class AssetEntry(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
) {

}