package com.example.taskappuas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val seeAllTextView = findViewById<TextView>(R.id.seeAllUrgent) // Make sure this ID exists in your layout
        seeAllTextView.setOnClickListener {
            val intent = Intent(this@MainActivity, TaskList::class.java).apply {
                putExtra("filter_type", "urgent")
            }
            startActivity(intent)
        }

        // Load tasks
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getTasks()
                val urgentTasks = response.urgent_tasks

                // Map the categories to be displayed
                val categoryMap = response.task_counts.mapKeys { it.key.capitalize() }
                val orderedCategoryItems = listOf("Regular", "Urgent", "Important", "Done")
                    .mapNotNull { category -> categoryMap[category]?.let { count -> CategoryItem(category, count) } }

                // Convert urgent tasks to CardItems
                val cardItems = urgentTasks.mapNotNull { task ->
                    try {
                        CardItem(
                            title = task.title,
                            desc = task.description,
                            dateUpdated = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .parse(task.dateUpdated) ?: Date()
                        )
                    } catch (e: Exception) {
                        Log.e("DateParseError", "Failed to parse date for task: ${task.title}")
                        null
                    }
                }

                // Update UI on Main thread
                withContext(Dispatchers.Main) {
                    val recyclerView = findViewById<RecyclerView>(R.id.rvUrgentTasks)
                    recyclerView.layoutManager = LinearLayoutManager(
                        this@MainActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )

// Initialize with click listener
                    recyclerView.adapter = CardAdapter(
                        this@MainActivity,
                        cardItems,
                        urgentTasks, // Pass the original Task objects here
                        { task ->  // Click listener
                            val intent = Intent(this@MainActivity, TaskDetailActivity::class.java).apply {
                                putExtra("task_id", task.id)
                                putExtra("task_title", task.title)
                                putExtra("task_status", task.status)
                                putExtra("task_desc", task.description)
                                putExtra("task_type", task.type)
                            }
                            startActivity(intent)
                        }
                    )

                    val recyclerView2 = findViewById<RecyclerView>(R.id.rvCategories)
                    recyclerView2.layoutManager = GridLayoutManager(this@MainActivity, 3)
                    recyclerView2.adapter = CategoryAdapter(this@MainActivity, orderedCategoryItems) { categoryName ->
                        // Handle category click - navigate to TaskList with filter
                        val intent = Intent(this@MainActivity, TaskList::class.java).apply {
                            putExtra("filter_type", when(categoryName.lowercase()) {
                                "regular" -> "regular"
                                "important" -> "important"
                                "done" -> "done"
                                else -> "all"
                            })
                        }
                        startActivity(intent)
                    }
                }

            } catch (e: Exception) {
                Log.e("APIError", "Error: ${e.message}")
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Stay here
                    true
                }
                R.id.add -> {
                    startActivity(Intent(this@MainActivity, FormTask::class.java))
                    true
                }
                R.id.task -> {
                    startActivity(Intent(this@MainActivity, TaskList::class.java))
                    true
                }
                else -> false
            }
        }
    }
}