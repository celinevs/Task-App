package com.example.taskappuas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class CardAdapter(
    private val context: Context,
    private val items: List<CardItem>,
    private val tasks: List<Task>, // Add this parameter to receive actual Task objects
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val desc: TextView = itemView.findViewById(R.id.taskDescription)
        val date: TextView = itemView.findViewById(R.id.taskDate)

        fun bind(cardItem: CardItem, task: Task) {
            title.text = cardItem.title
            desc.text = cardItem.desc
            date.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(cardItem.dateUpdated)

            itemView.setOnClickListener {
                onItemClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.task_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(items[position], tasks[position])
    }

    override fun getItemCount(): Int = items.size
}