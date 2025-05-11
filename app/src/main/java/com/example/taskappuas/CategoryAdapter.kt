package com.example.taskappuas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import android.graphics.drawable.GradientDrawable

class CategoryAdapter(
    private val context: Context,
    private var items: List<CategoryItem>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.categoryName)
        val count: TextView = itemView.findViewById(R.id.categoryCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.category_button, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = items[position]
        holder.category.text = item.category.replaceFirstChar { it.uppercaseChar() }
        holder.count.text = "Tasks: ${item.count}"

        // Set background color based on category
        val backgroundColor = when (item.category.lowercase()) {
            "regular" -> 0xFFFFA726.toInt() // Orange
            "urgent" -> 0xFFEF5350.toInt()  // Red
            "important" -> 0xFFFFEB3B.toInt() // Yellow
            "done" -> 0xFFE3F2FD.toInt()    // White-blue
            else -> 0xFF90CAF9.toInt()      // Default Blue
        }

        val drawable = holder.itemView.background.mutate()
        (drawable as GradientDrawable).setColor(backgroundColor)
        holder.itemView.background = drawable

        // Highlight urgent tasks with red stripe
        val leftStripe: View = holder.itemView.findViewById(R.id.leftStripe)
        if (item.category.lowercase() == "urgent") {
            leftStripe.setBackgroundColor(0xFFFF0000.toInt()) // Red
        } else {
            leftStripe.setBackgroundColor(0x00000000) // Transparent
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position].category.lowercase()) {
            "important" -> 1 // full span
            else -> 0 // half span
        }
    }

    fun updateData(newItems: List<CategoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
