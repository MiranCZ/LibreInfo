package io.github.mirancz.libreinfo.util

import kotlinx.serialization.json.Json
import java.io.InputStream

@JvmField
val json = Json {
    coerceInputValues = true
    ignoreUnknownKeys = true
    explicitNulls = false
}

