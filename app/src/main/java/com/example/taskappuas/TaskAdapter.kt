package com.example.taskappuas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onMarkDone: (Task, Int) -> Unit,
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_simple, parent, false)
        return TaskViewHolder(view, onItemClick, onMarkDone)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(
        view: View,
        private val onItemClick: (Task) -> Unit,
        private val onMarkDone: (Task, Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTaskTitle)
        private val tvTaskType: TextView = view.findViewById(R.id.tvTaskType)
        private val taskTypeIndicator: View = view.findViewById(R.id.taskTypeIndicator)
        private val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        private val btnMarkDone: Button = view.findViewById(R.id.btnMarkDone)

        fun bind(task: Task) {
            tvTitle.text = task.title
            tvTaskType.text = task.type.capitalize()

            // Set type-specific colors
            val (indicatorColor, typeColor) = when (task.type.lowercase()) {
                "urgent" -> Pair(Color.RED, "#F44336")
                "important" -> Pair(Color.YELLOW, "#FFC107")
                else -> Pair(Color.BLUE, "#2196F3") // Default for regular
            }

            taskTypeIndicator.background.setTint(indicatorColor)
            tvTaskType.setTextColor(Color.parseColor(typeColor))

            // Format date
            task.dateUpdated?.let {
                tvDueDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it) ?: Date())
            } ?: run {
                tvDueDate.text = "No date"
            }

            btnMarkDone.visibility = if (task.status != "done") View.VISIBLE else View.GONE
            itemView.setOnClickListener { onItemClick(task) }
            btnMarkDone.setOnClickListener { onMarkDone(task, adapterPosition) }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}