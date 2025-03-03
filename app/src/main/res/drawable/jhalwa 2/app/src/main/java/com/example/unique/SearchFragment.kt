package com.example.unique

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unique.databinding.FragmentSearchBinding
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchAdapter: BestDealsAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private var productList: List<Product> = listOf()
    private var categoryList: List<String> = listOf("Main", "Dogs", "Cats", "Fish", "Accessories", "Adopt Pet")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up category menu
        setupCategoryMenu()

        // Set up RecyclerView
        setupRecyclerView()

        // Set up SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return false
            }
        })
    }

    private fun setupCategoryMenu() {
        // Setting up the category menu as a horizontal list
        binding.categoryMenuRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            // You can handle category selection here if necessary
            // For now, just perform a search with the current query
            performSearch(binding.searchView.query.toString())
        }
        binding.categoryMenuRecyclerView.adapter = categoryAdapter
    }

    private fun setupRecyclerView() {
        // Set up a GridLayoutManager for product listing (2 columns)
        binding.searchRecyclerView.layoutManager = GridLayoutManager(context, 2)
        searchAdapter = BestDealsAdapter(productList)
        binding.searchRecyclerView.adapter = searchAdapter
    }

    private fun performSearch(query: String) {
        val db = FirebaseFirestore.getInstance()

        // Query Firestore to get all products
        db.collection("bestDeals")
            .get()
            .addOnSuccessListener { documents ->
                productList = documents.mapNotNull { document ->
                    val name = document.getString("name") ?: return@mapNotNull null
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val id = document.id
                    Product(name, imageUrl, price, "category", id)
                }

                // Filter products based on the case-insensitive query
                val filteredProducts = productList.filter { product ->
                    product.name.lowercase().contains(query.lowercase())
                }

                // Update the adapter with the filtered products
                searchAdapter.updateProducts(filteredProducts)
                Log.d("SearchFragment", "Found ${filteredProducts.size} products")
            }
            .addOnFailureListener { exception ->
                Log.e("SearchFragment", "Error fetching products", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
