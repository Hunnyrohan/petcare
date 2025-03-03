package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CategoryProductsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BestDealsAdapter
    private var productList: List<Product> = listOf()

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String) = CategoryProductsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_CATEGORY, category)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = BestDealsAdapter(productList)
        recyclerView.adapter = adapter

        val category = arguments?.getString(ARG_CATEGORY)
        category?.let { fetchCategoryProductsFromFirebase(it) }
    }

    private fun fetchCategoryProductsFromFirebase(category: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("bestDeals").whereEqualTo("category", category).get()
            .addOnSuccessListener { documents ->
                productList = documents.map { document ->
                    val name = document.getString("name") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val id = document.id
                    Product(name, imageUrl, price, category, id)
                }
                adapter.updateProducts(productList)
            }
            .addOnFailureListener { exception ->
                // Handle the error appropriately
            }
    }
}
