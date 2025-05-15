package com.example.taskappuas

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TaskApiService {
    // Dashboard endpoint - gets urgent tasks and counts
    @GET("api/tasks")
    suspend fun getTasks(): ApiResponse

    // Get all tasks with optional filters
    @GET("api/tasks/all")
    suspend fun getAllTasks(
        @Query("type") type: String? = null,
        @Query("status") status: String? = null
    ):TaskListResponse

    // Get single task by ID
    @GET("api/tasks/{id}")
    suspend fun getTaskById(@Path("id") id: Int): Task

    // Create new task
    @POST("api/tasks")
    suspend fun createTask(@Body task: TaskRequest): Task

    // Update existing task
    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Int,
        @Body task: TaskRequest
    ): Task

    // Delete task
    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): ResponseBody
}


data class TaskListResponse(
    val success: Boolean,
    val count: Int,
    val tasks: List<Task>
)

