package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderDetailsAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
        val productQuantityTextView: TextView = itemView.findViewById(R.id.productQuantityTextView)
        val productCostTextView: TextView = itemView.findViewById(R.id.productCostTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.productNameTextView.text = product.name
        holder.productQuantityTextView.text = "Quantity: ${product.quantity}"
        holder.productCostTextView.text = "Cost: ₹%.2f".format(product.price * product.quantity)
    }

    override fun getItemCount(): Int {
        return products.size
    }
}
