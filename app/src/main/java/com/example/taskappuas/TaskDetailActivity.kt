package com.example.taskappuas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var task: Task

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Create Task object from intent extras
        task = Task(
            id = intent.getIntExtra("task_id", -1),
            title = intent.getStringExtra("task_title") ?: "",
            description = intent.getStringExtra("task_desc") ?: "",
            type = intent.getStringExtra("task_type") ?: "regular",
            status = intent.getStringExtra("task_status") ?: "pending",
            dateUpdated = intent.getStringExtra("task_date") ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )

        if (task.id == -1) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("TaskDetail", "Task loaded from intent: $task")
        bindTaskData()
        setupActionButtons()
    }

    private fun bindTaskData() {
        with(task) {
            findViewById<TextView>(R.id.tvTitle).text = title
            findViewById<TextView>(R.id.tvDescription).text = description
            findViewById<TextView>(R.id.tvType).text = type?.capitalize() ?: "Unknown"
            findViewById<TextView>(R.id.tvStatus).text =status?.capitalize() ?: "Unknown"
            findViewById<TextView>(R.id.tvDueDate).text = formatDate(dateUpdated)

            // Set priority indicator color
            val priorityColor = when (type?.lowercase()) {
                "urgent" -> Color.RED
                "important" -> Color.YELLOW
                "done" -> Color.GREEN
                else -> Color.BLUE
            }
            findViewById<View>(R.id.viewPriorityIndicator).setBackgroundColor(priorityColor)
        }
    }

    private fun setupActionButtons() {
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Hide buttons if task is done
        if (task.status.equals("done", ignoreCase = true)) {
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
            return
        }

        btnEdit.setOnClickListener {
            Intent(this, EditTaskActivity::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("task_title", task.title)
                putExtra("task_desc", task.description)
                putExtra("task_type", task.type)
                putExtra("task_status", task.status)
                putExtra("task_date", task.dateUpdated)
                startActivity(this)
            }
        }

        btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                deleteTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTask() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitClient.instance.deleteTask(task.id)
                }
                Toast.makeText(this@TaskDetailActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@TaskDetailActivity, TaskList::class.java))
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)?.let {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
            } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}