package com.example.taskappuas

data class Task (
    val id: Int,
    val title: String,
    val dateUpdated: String,
    val type: String,         // "urgent", "regular", or "important"
    val status: String,
    val description: String
) {
}