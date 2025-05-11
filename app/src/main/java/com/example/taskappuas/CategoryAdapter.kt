package com.example.taskappuas

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

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
        holder.category.text = item.category
        holder.count.text = item.count.toString() // Assuming count is Int
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<CategoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}