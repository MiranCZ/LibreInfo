package io.github.mirancz.libreinfo.model.types

import kotlinx.serialization.Serializable

@Serializable
data class ParentStopStation(val stopId: Short, val postId: Short, val postType: PostType) {
    companion object {
        @JvmStatic
        val NONE = ParentStopStation(-1, -1, PostType.POST)
    }


}

enum class PostType {
    POST,
    PLATFORM
}