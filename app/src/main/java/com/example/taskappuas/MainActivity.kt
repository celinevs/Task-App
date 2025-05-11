package com.example.taskappuas

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = CardAdapter(this@MainActivity, cardItems)

                    val recyclerView2 = findViewById<RecyclerView>(R.id.rvCategories)
                    recyclerView2.layoutManager = GridLayoutManager(this@MainActivity, 2)
                    recyclerView2.adapter = CategoryAdapter(this@MainActivity, orderedCategoryItems)
                }

            } catch (e: Exception) {
                Log.e("APIError", "Error: ${e.message}")
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        // Stay here
                        true
                    }
                    R.id.nav_add -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainLayout, SubmitFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.nav_list -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainLayout, ListFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> false
                }
            }

        }
    }


