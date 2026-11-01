package me.miran.model;

data class Diversion(
     val id: Int?,
     val title: String,
     val content: String,
     val from: String?,
     val to: String?,
     val latitude: Double?,
     val longitude: Double?,
     val lines: List<Int>?,
) {
}
