package me.miran.ids

data class ApiPost(
    val stopId: Int,
    val postId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isPublic: Boolean
) {
}
