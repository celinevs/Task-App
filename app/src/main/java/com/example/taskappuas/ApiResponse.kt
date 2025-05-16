package com.example.taskappuas

data class ApiResponse(    val urgent_tasks: List<Task>,
                           val task_counts: Map<String, Int>,
                           val error: String? = null)
