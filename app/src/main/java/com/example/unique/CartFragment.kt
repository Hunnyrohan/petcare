package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private var cartItems: List<Product> = listOf() // Holds cart items
    private lateinit var checkoutButton: Button
    private lateinit var totalCostTextView: TextView
    private lateinit var emptyCartTextView: TextView // Declare the empty cart TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cart_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        totalCostTextView = view.findViewById(R.id.totalCostTextView)
        emptyCartTextView = view.findViewById(R.id.emptyCartTextView) // Initialize the empty cart TextView
        checkoutButton = view.findViewById(R.id.checkoutButton)

        cartRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the cart items from CartManager
        cartItems = CartManager.getCartItems()

        cartAdapter = CartAdapter(cartItems, ::updateTotalCost)
        cartRecyclerView.adapter = cartAdapter

        // Set up the checkout button listener
        checkoutButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, CheckoutFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Update UI based on the cart contents
        updateCartUI()
    }

    private fun updateCartUI() {
        if (cartItems.isEmpty()) {
            // Show the empty cart message and disable the checkout button
            emptyCartTextView.visibility = View.VISIBLE
            checkoutButton.isEnabled = false
        } else {
            // Hide the empty cart message and enable the checkout button
            emptyCartTextView.visibility = View.GONE
            checkoutButton.isEnabled = true
        }

        // Update the total cost
        updateTotalCost()
    }

    private fun updateTotalCost() {
        var totalCost = 0.0

        // Calculate total cost by iterating through the cart items
        for (product in cartItems) {
            totalCost += product.price * product.quantity
        }

        // Update the TextView to display the total cost
        totalCostTextView.text = "Total Cost: ₹%.2f".format(totalCost)
    }

    override fun onResume() {
        super.onResume()
        // Refresh the cart items when returning to this fragment
        cartItems = CartManager.getCartItems()
        cartAdapter.updateCartItems(cartItems) // Update the adapter with new cart items
        updateCartUI() // Update the UI
    }
}
