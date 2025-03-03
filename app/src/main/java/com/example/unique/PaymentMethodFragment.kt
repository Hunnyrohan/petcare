package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class PaymentMethodFragment : Fragment() {

    private lateinit var paymentButton: Button
    private lateinit var placeOrderButton: Button
    private var isPaymentDone: Boolean = false // Flag to check if payment is done

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_method, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentButton = view.findViewById(R.id.btnPaymentDone) // Change to your actual button ID
        placeOrderButton = view.findViewById(R.id.btnPlaceOrder) // Change to your actual button ID

        // Retrieve total cost and transaction ID from arguments
        val totalCost = arguments?.getDouble("TOTAL_COST") ?: 0.0
        val transactionId = arguments?.getString("TRANSACTION_ID") ?: ""

        paymentButton.setOnClickListener {
            // Handle payment logic here, like integrating with a payment gateway

            // For demonstration, show a toast message indicating payment success
            Toast.makeText(requireContext(), "Payment done successfully!", Toast.LENGTH_SHORT).show()

            // Set the payment done flag to true
            isPaymentDone = true
        }

        placeOrderButton.setOnClickListener {
            // Check if the payment is done before placing the order
            if (isPaymentDone) {
                // Logic for placing the order goes here
                Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to OrderSuccessFragment
                val orderSuccessFragment = OrderSuccessFragment()
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, orderSuccessFragment) // Change to your fragment container ID
                transaction.addToBackStack(null) // Allow back navigation
                transaction.commit()
            } else {
                // Show a message indicating that the payment must be done first
                Toast.makeText(requireContext(), "Please confirm payment before placing the order.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
