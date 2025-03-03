package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderItemsAdapter(private val items: List<Map<String, Any>>) : RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productNameTextView: TextView = view.findViewById(R.id.productNameTextView)
        val quantityTextView: TextView = view.findViewById(R.id.quantityTextView)
        val priceTextView: TextView = view.findViewById(R.id.priceTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = items[position]
        holder.productNameTextView.text = "Product: ${item["productName"]}"
        holder.quantityTextView.text = "Quantity: ${item["quantity"]}"
        holder.priceTextView.text = "Price: ₹${item["price"]}"
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
