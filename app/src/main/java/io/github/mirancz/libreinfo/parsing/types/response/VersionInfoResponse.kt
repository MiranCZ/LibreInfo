package io.github.mirancz.libreinfo.parsing.types.response

import kotlinx.serialization.Serializable

@Serializable
data class VersionInfoResponse(val versionCode: Int, val versionName: String, val apks: Map<String, ApkInfo>)

@Serializable
data class ApkInfo(val name: String, val hash: String)