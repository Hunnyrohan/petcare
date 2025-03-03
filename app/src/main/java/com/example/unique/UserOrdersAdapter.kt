package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserOrdersAdapter(private val orders: List<Order>) : RecyclerView.Adapter<UserOrdersAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderNumberTextView: TextView = view.findViewById(R.id.orderNumberTextView) // Added Order Number
        val transactionIdTextView: TextView = view.findViewById(R.id.transactionIdTextView)
        val customerNameTextView: TextView = view.findViewById(R.id.customerNameTextView)
        val addressTextView: TextView = view.findViewById(R.id.addressTextView)
        val phoneNumberTextView: TextView = view.findViewById(R.id.phoneNumberTextView)
        val totalCostTextView: TextView = view.findViewById(R.id.totalCostTextView)
        val itemsRecyclerView: RecyclerView = view.findViewById(R.id.itemsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Set the order number
        holder.orderNumberTextView.text = "Order No: ${position + 1}"

        holder.transactionIdTextView.text = "Transaction ID: ${order.transactionId}"
        holder.customerNameTextView.text = "Name: ${order.customerName}"
        holder.addressTextView.text = "Address: ${order.address}"
        holder.phoneNumberTextView.text = "Phone: ${order.phoneNumber}"
        holder.totalCostTextView.text = "Total Cost: ₹%.2f".format(order.totalCost)

        // Set up the RecyclerView for order items
        holder.itemsRecyclerView.layoutManager = LinearLayoutManager(holder.itemsRecyclerView.context)
        holder.itemsRecyclerView.adapter = OrderItemsAdapter(order.items)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}
