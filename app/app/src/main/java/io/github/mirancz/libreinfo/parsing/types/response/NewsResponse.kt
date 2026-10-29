package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.NewsEntry
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(val news: List<NewsEntry>, val limit: Int, val offset: Int, val total: Int?)
