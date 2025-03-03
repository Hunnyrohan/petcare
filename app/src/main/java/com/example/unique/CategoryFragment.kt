package com.example.unique

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BestDealsAdapter
    private var productList: List<Product> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = BestDealsAdapter(productList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Get category from arguments and fetch products
        val category = arguments?.getString("category")
        if (category != null) {
            fetchProductsFromFirebase(category)
        } else {
            Log.e("CategoryFragment", "Category argument is null")
        }
    }

    private fun fetchProductsFromFirebase(category: String) {
        val db = FirebaseFirestore.getInstance()
        // Fetch products based on the category from Firestore
        db.collection("products").whereEqualTo("category", category).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.e("CategoryFragment", "No products found for category: $category")
                } else {
                    productList = documents.map { document ->
                        val name = document.getString("name") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val price = document.getDouble("price") ?: 0.0
                        val id = document.id
                        Product(name, imageUrl, price, category, id)
                    }
                    adapter.updateProducts(productList) // Update adapter with new data
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CategoryFragment", "Error fetching products: ", exception)
            }
    }

    companion object {
        fun newInstance(category: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString("category", category)
            fragment.arguments = args
            return fragment
        }
    }
}
