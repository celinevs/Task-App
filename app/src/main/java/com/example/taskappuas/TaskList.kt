package com.example.taskappuas

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskList : AppCompatActivity() {
    private lateinit var adapter: TaskAdapter
    private lateinit var btnAll: Button
    private lateinit var btnRegular: Button
    private lateinit var btnImportant: Button
    private lateinit var btnUrgent: Button
    private lateinit var btnDone: Button
    private lateinit var rvTasks: RecyclerView
    private var currentFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        btnAll = findViewById(R.id.btnAll)
        btnRegular = findViewById(R.id.btnRegular)
        btnImportant = findViewById(R.id.btnImportant)
        btnUrgent = findViewById(R.id.btnUrgent)
        btnDone = findViewById(R.id.btnDone)
        rvTasks = findViewById(R.id.rvTasks)

        // Setup RecyclerView
        adapter = TaskAdapter(
            onItemClick = { task ->
                navigateToTaskDetail(task)
            },
            onMarkDone = { task, position ->
                markTaskAsDone(task, position)
            }
        )
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        // Set up filter buttons
        setupFilterButtons()

        // Check for filter_type from intent
        intent.getStringExtra("filter_type")?.let { filterType ->
            selectFilterButton(filterType)
            loadTasks(filterType)
        } ?: run {
            selectFilterButton(null)
            loadTasks()
        }

        setupBottomNavigation()
    }

    private fun setupFilterButtons() {
        val buttons = listOf(btnAll, btnRegular, btnImportant, btnUrgent, btnDone)

        buttons.forEach { button ->
            button.setOnClickListener {
                val filter = when (button.id) {
                    R.id.btnAll -> null
                    R.id.btnRegular -> "regular"
                    R.id.btnImportant -> "important"
                    R.id.btnUrgent -> "urgent"
                    R.id.btnDone -> "done"
                    else -> null
                }
                selectFilterButton(filter)
                loadTasks(filter)
            }
        }
    }

    private fun selectFilterButton(filter: String?) {
        val buttons = mapOf(
            null to btnAll,
            "regular" to btnRegular,
            "important" to btnImportant,
            "urgent" to btnUrgent,
            "done" to btnDone
        )

        buttons.forEach { (key, button) ->
            button.isSelected = key == filter
        }
    }

    private fun navigateToTaskDetail(task: Task) {
        Intent(this, TaskDetailActivity::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("task_title", task.title)
            putExtra("task_status", task.status)
            putExtra("task_desc", task.description)
            putExtra("task_type", task.type)
            startActivity(this)
        }
    }
    private fun loadTasks(filter: String? = null) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    when (filter) {
                        "done" -> RetrofitClient.instance.getAllTasks(status = "done")
                        "regular" -> RetrofitClient.instance.getAllTasks(type = "regular", status = "in-progress")
                        "important" -> RetrofitClient.instance.getAllTasks(type = "important", status = "in-progress")
                        "urgent" -> RetrofitClient.instance.getAllTasks(type = "urgent", status = "in-progress")
                        else -> RetrofitClient.instance.getAllTasks(status = "in-progress") // "All" filter
                    }
                }

                if (response.success) {
                    adapter.submitList(response.tasks)
                } else {
                    showToast("Failed to load tasks")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun markTaskAsDone(task: Task, position: Int) {
        lifecycleScope.launch {
            try {
                val updatedTask = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.updateTask(
                        task.id,
                        TaskRequest(
                            title = task.title,
                            description = task.description,
                            type = task.type,
                            status = "done",
                            dateUpdated = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                    )
                }

                // Update the item in the list
                val currentList = adapter.currentList.toMutableList()
                currentList[position] = updatedTask
                adapter.submitList(currentList)

                showToast("Task marked as done")

                // Refresh with current filter
                loadTasks(currentFilter)

            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.task
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.add -> {
                    startActivity(Intent(this, FormTask::class.java))
                    true
                }
                R.id.task -> true
                else -> false
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}