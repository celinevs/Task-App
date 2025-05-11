package com.example.taskappuas

data class TaskResponse(    val id: Int,
                            val title: String,
                            val description: String,
                            val type: String,
                            val status: String,
                            val dateUpdated: String)
