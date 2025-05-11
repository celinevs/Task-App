package com.example.taskappuas

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Load tasks
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getTasks()
                val urgentTasks = response.urgent_tasks

                // Log category counts
                val categoryItems = response.task_counts.mapNotNull { (category, count) ->
                    try {
                        CategoryItem(category, count)
                    }
                    catch (e: Exception) {
                        Log.e("DateParseError", "Failed to parse date for task: ${category}")
                        null
                    }
                }

                // Convert to CardItems with safe date parsing
                val cardItems = urgentTasks.mapNotNull { task ->
                    try {
                        CardItem(
                            title = task.title,
                            desc = task.description,
                            dateUpdated = SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).parse(task.dateUpdated) ?: Date()
                        )
                    } catch (e: Exception) {
                        Log.e("DateParseError", "Failed to parse date for task: ${task.title}")
                        null
                    }
                }

                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    val recyclerView = findViewById<RecyclerView>(R.id.rvUrgentTasks)
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = CardAdapter(this@MainActivity, cardItems)

                    val recyclerView2 = findViewById<RecyclerView>(R.id.rvCategories)
                    recyclerView2.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView2.adapter = CategoryAdapter(this@MainActivity, categoryItems)
                }

            } catch (e: Exception) {
                Log.e("APIError", "Error: ${e.message}")
            }
        }
        }
    }
