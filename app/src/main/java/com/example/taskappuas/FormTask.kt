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
import java.util.*

class FormTask : AppCompatActivity() {
    // Declare views
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_form_task)

//        // Handle edge-to-edge display
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Initialize views
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        rgType = findViewById(R.id.rgType)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnCancel = findViewById(R.id.btnCancel)


        // Set up submit button click listener
        btnSubmit.setOnClickListener {
            submitTask()
        }

        btnCancel.setOnClickListener {
            finish()
        }

    }

    private fun submitTask() {
        val title = etTitle.text.toString().trim()
        val type = when (rgType.checkedRadioButtonId) {
            R.id.rbUrgent -> "urgent"
            R.id.rbImportant -> "important"
            else -> "regular"
        }
        val description = etDescription.text.toString().trim()

        if (validateInput(title, description)) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.instance.createTask(
                        TaskRequest(
                            title = title,
                            type = type,
                            description = description,
                            status = "in-progress",
                            dateUpdated = currentDate
                        )
                    )

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormTask, "Task added successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@FormTask, MainActivity::class.java))
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@FormTask, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun validateInput(title: String, description: String): Boolean {
        var isValid = true

        if (title.isEmpty()) {
            etTitle.error = "Title is required"
            isValid = false
        }

        if (description.isEmpty()) {
            etDescription.error = "Description is required"
            isValid = false
        }

        return isValid
    }
}