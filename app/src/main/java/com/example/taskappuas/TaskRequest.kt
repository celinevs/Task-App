package com.example.taskappuas

class TaskRequest (
    val title: String,
    val description: String? = null,
    val type: String,
    val status: String,
    val dateUpdated: String
) {
}