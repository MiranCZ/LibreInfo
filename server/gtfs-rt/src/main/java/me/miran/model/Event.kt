package me.miran.model;

data class Event(
     val id: Int?,
     val title: String,
     val content: String,
     val from: String?,
     val to: String?,
     val delay: Int?,
     val lines: List<Int>?,
)
