package com.example.taskappuas

import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface TaskApiService {
    @GET("api/tasks")
    suspend fun getTasks(): ApiResponse

//    @GET("api/tasks/{id}")
//    suspend fun getTask(@Path("id") id: Int): Task
//
//    @POST("api/tasks")
//    suspend fun createTask(@Body task: Task): Task
//
//    @PUT("api/tasks/{id}")
//    suspend fun updateTask(@Path("id") id: Int, @Body task: Task): Task
//
//    @DELETE("api/tasks/{id}")
//    suspend fun deleteTask(@Path("id") id: Int): retrofit2.Response<Unit>
}