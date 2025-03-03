package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment() {

    private lateinit var productImage: ImageView
    private lateinit var productName: TextView
    private lateinit var productPrice: TextView
    private lateinit var productDescription: TextView
    private lateinit var addToCartButton: Button

    companion object {
        private const val ARG_PRODUCT_ID = "productId"

        fun newInstance(productId: String) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PRODUCT_ID, productId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        productImage = view.findViewById(R.id.productImage)
        productName = view.findViewById(R.id.productName)
        productPrice = view.findViewById(R.id.productPrice)
        productDescription = view.findViewById(R.id.productDescription)
        addToCartButton = view.findViewById(R.id.addToCartButton)

        arguments?.getString(ARG_PRODUCT_ID)?.let { fetchProductDetails(it) }

        return view
    }

    private fun fetchProductDetails(productId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("bestDeals").document(productId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    productName.text = document.getString("name")
                    productPrice.text = "Price: ₹${document.getDouble("price") ?: 0.0}"
                    productDescription.text = document.getString("description")
                    val imageUrl = document.getString("imageUrl")
                    Glide.with(this).load(imageUrl).into(productImage)

                    addToCartButton.setOnClickListener {
                        // Create a product object to add to the cart
                        val product = Product(
                            name = productName.text.toString(),
                            imageUrl = imageUrl.orEmpty(),
                            price = document.getDouble("price") ?: 0.0,
                            category = document.getString("category").orEmpty(),
                            id = productId
                        )
                        CartManager.addToCart(product, requireContext())
                    }
                }
            }
            .addOnFailureListener {
                // Handle error, such as logging or showing a message to the user
            }
    }
}
