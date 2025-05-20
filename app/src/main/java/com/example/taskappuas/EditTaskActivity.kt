package com.example.taskappuas

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

class EditTaskActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var rgTaskType: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var taskId: Int = -1
    private var currentStatus: String = ""
    private var currentDate: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        etTitle = findViewById(R.id.editTitle)
        etDescription = findViewById(R.id.editDescription)
        rgTaskType = findViewById(R.id.editType)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancelEdit)

        // Get task data from intent
        taskId = intent.getIntExtra("task_id", -1)
        val title = intent.getStringExtra("task_title") ?: ""
        val description = intent.getStringExtra("task_desc") ?: ""
        val type = intent.getStringExtra("task_type") ?: "regular"
        currentStatus = intent.getStringExtra("task_status") ?: "pending"
        currentDate = intent.getStringExtra("task_date") ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Pre-fill the form with existing values
        etTitle.setText(title)
        etDescription.setText(description)

        btnCancel.setOnClickListener {
            finish()
        }

        // Set the appropriate radio button based on task type
        when (type.lowercase()) {
            "important" -> rgTaskType.check(R.id.rbImportant)
            "urgent" -> rgTaskType.check(R.id.rbUrgent)
            else -> rgTaskType.check(R.id.rbRegular)
        }

        // Set up save button click listener
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (title.isEmpty()) {
            etTitle.error = "Title cannot be empty"
            return
        }

        // Get the selected task type
        val selectedType = when (rgTaskType.checkedRadioButtonId) {
            R.id.rbImportant -> "important"
            R.id.rbUrgent -> "urgent"
            R.id.rbRegular -> "regular"
            else -> "regular" // default case
        }

        lifecycleScope.launch {
            try {
                val updatedTask = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.updateTask(
                        taskId,
                        TaskRequest(
                            title = title,
                            description = description,
                            type = selectedType,
                            status = currentStatus,
                            dateUpdated = currentDate
                        )
                    )
                }

                Toast.makeText(this@EditTaskActivity, "Task updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@EditTaskActivity, TaskList::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@EditTaskActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}