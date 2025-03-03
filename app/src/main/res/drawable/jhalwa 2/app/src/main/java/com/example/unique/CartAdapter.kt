package com.example.unique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var cartItems: List<Product>,
    private val updateTotalCostCallback: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val productPrice: TextView = view.findViewById(R.id.productPrice)
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val quantityTextView: TextView = view.findViewById(R.id.quantityTextView)
        val plusButton: Button = view.findViewById(R.id.plusButton)
        val minusButton: Button = view.findViewById(R.id.minusButton)
        val removeButton: Button = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val product = cartItems[position]

        // Bind data to views
        holder.productName.text = product.name
        holder.productPrice.text = "₹${product.price * product.quantity}"
        holder.quantityTextView.text = product.quantity.toString()

        // Load product image
        Glide.with(holder.productImage.context)
            .load(product.imageUrl)
            .into(holder.productImage)

        // Handle quantity increase
        holder.plusButton.setOnClickListener {
            product.quantity++
            notifyItemChanged(position)
            updateTotalCostCallback()
        }

        // Handle quantity decrease
        holder.minusButton.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                notifyItemChanged(position)
                updateTotalCostCallback()
            }
        }

        // Remove product from cart
        holder.removeButton.setOnClickListener {
            CartManager.removeFromCart(product, holder.itemView.context)
            notifyDataSetChanged()
            updateTotalCostCallback()
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    fun updateCartItems(newItems: List<Product>) {
        cartItems = newItems
        notifyDataSetChanged() // Notify the adapter to refresh the UI
        updateTotalCostCallback() // Update the total cost
    }
}
