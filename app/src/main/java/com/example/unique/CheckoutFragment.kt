package com.example.unique

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CheckoutFragment : Fragment() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var phoneNumberEditText: TextInputEditText
    private lateinit var confirmButton: Button
    private lateinit var orderDetailsRecyclerView: RecyclerView
    private lateinit var totalCostTextView: TextView

    private lateinit var cartItems: List<Product> // List to hold the products for the order
    private val db = FirebaseFirestore.getInstance() // Initialize Firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_checkout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.nameEditText)
        addressEditText = view.findViewById(R.id.addressEditText)
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText)
        confirmButton = view.findViewById(R.id.confirmButton)
        orderDetailsRecyclerView = view.findViewById(R.id.orderDetailsRecyclerView)
        totalCostTextView = view.findViewById(R.id.totalCostTextView)

        // Set up the RecyclerView for order details
        orderDetailsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch the cart items from CartManager
        cartItems = CartManager.getCartItems()
        orderDetailsRecyclerView.adapter = OrderDetailsAdapter(cartItems)

        // Calculate and display the total cost
        updateTotalCost() // Update the total cost

        // Initially disable the confirm button
        confirmButton.isEnabled = false

        // Add TextWatcher to monitor input changes
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateInput() // Validate input on text change
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        nameEditText.addTextChangedListener(textWatcher)
        addressEditText.addTextChangedListener(textWatcher)
        phoneNumberEditText.addTextChangedListener(textWatcher)

        confirmButton.setOnClickListener {
            if (validateInput()) {
                // Save order details to Firestore
                saveOrderDetails(updateTotalCost()) // Pass the updated total cost
            } else {
                // Show error message if input validation fails
                Toast.makeText(requireContext(), "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTotalCost(): Double {
        var productTotal = 0.0

        // Calculate the total cost by iterating through the cart items
        for (product in cartItems) {
            productTotal += product.price * product.quantity
        }

        // Calculate GST (18% of product total)
        val gst = productTotal * 0.18

        // Set the delivery charge
        val deliveryCharge = 60.0

        // Calculate final total cost
        val finalTotalCost = productTotal + gst + deliveryCharge

        // Update the TextView to display the costs
        totalCostTextView.text = """
            Product Cost: ₹%.2f
            GST (18%%): ₹%.2f
            Delivery Charge: ₹%.2f
            Total Cost: ₹%.2f
        """.trimIndent().format(productTotal, gst, deliveryCharge, finalTotalCost)

        return finalTotalCost // Return the final total cost
    }

    private fun validateInput(): Boolean {
        val name = nameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val phoneNumber = phoneNumberEditText.text.toString().trim()

        // Validate the name and phone number format
        val isNameValid = name.isNotEmpty()
        val isAddressValid = address.isNotEmpty()
        val isPhoneNumberValid = phoneNumber.matches(Regex("^[0-9]{10}$"))

        // Enable the button only if all fields are filled correctly
        confirmButton.isEnabled = isNameValid && isAddressValid && isPhoneNumberValid

        return isNameValid && isAddressValid && isPhoneNumberValid
    }



    private fun saveOrderDetails(totalCost: Double) {
        // Get the current user ID from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a unique transaction ID using UUID
        val transactionId = UUID.randomUUID().toString()

        // Create an order object to hold the details, including the userId and transactionId
        val orderDetails = hashMapOf(
            "userId" to userId, // Include the userId
            "transactionId" to transactionId, // Include the unique transaction ID
            "customerName" to nameEditText.text.toString(),
            "address" to addressEditText.text.toString(),
            "phoneNumber" to phoneNumberEditText.text.toString(),
            "totalCost" to totalCost,
            "items" to cartItems.map {
                hashMapOf(
                    "productName" to it.name,
                    "quantity" to it.quantity,
                    "price" to it.price * it.quantity // total cost for each item
                )
            }
        )

        // Save order details to Firestore
        db.collection("orders")
            .add(orderDetails)
            .addOnSuccessListener { documentReference ->
                // Clear the cart after successful order placement
                CartManager.clearCart(requireContext())

                // Navigate to PaymentMethodFragment with total cost
                val paymentMethodFragment = PaymentMethodFragment().apply {
                    arguments = Bundle().apply {
                        putDouble("TOTAL_COST", totalCost) // Pass the total cost
                        putString("TRANSACTION_ID", transactionId) // Pass the transaction ID
                    }
                }
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, paymentMethodFragment)
                transaction.addToBackStack(null) // Allow back navigation
                transaction.commit()
            }
            .addOnFailureListener { e ->
                // Handle the error
                Toast.makeText(requireContext(), "Error saving order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}