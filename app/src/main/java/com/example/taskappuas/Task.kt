package com.example.taskappuas

class Task (
    val id: Int,
    val title: String,
    val dateUpdated: String,
    val type: String,         // "urgent", "regular", or "important"
    val description: String
) {
}