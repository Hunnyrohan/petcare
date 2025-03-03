package com.example.unique

data class CartItem(
    val name: String,         // Name of the product
    val price: Double,       // Price of the product
    val imageResId: Int,
    val product: Product,
    var quantity: Int = 1
)
