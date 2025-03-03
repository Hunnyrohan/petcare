package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NavigationMenuAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NavigationMenuAdapter.NavigationMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false) // Use your existing item layout
        return NavigationMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavigationMenuViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    inner class NavigationMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)

        fun bind(item: String) {
            categoryTextView.text = item
        }
    }
}
