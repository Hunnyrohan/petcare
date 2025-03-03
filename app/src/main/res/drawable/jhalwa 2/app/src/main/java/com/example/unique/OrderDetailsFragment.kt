package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OrderDetailsFragment : Fragment() {

    private lateinit var ordersRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView)
        ordersRecyclerView.layoutManager = LinearLayoutManager(context)

        // Fetch and display orders for the current user
        fetchOrdersForUser()
    }

    private fun fetchOrdersForUser() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val orders = querySnapshot.documents.map { document ->
                        Order(
                            transactionId = document.getString("transactionId") ?: "",
                            customerName = document.getString("customerName") ?: "",
                            address = document.getString("address") ?: "",
                            phoneNumber = document.getString("phoneNumber") ?: "",
                            totalCost = document.getDouble("totalCost") ?: 0.0,
                            items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        )
                    }

                    // Set up the RecyclerView with UserOrdersAdapter
                    ordersRecyclerView.adapter = UserOrdersAdapter(orders)
                } else {
                    Toast.makeText(requireContext(), "No orders found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
