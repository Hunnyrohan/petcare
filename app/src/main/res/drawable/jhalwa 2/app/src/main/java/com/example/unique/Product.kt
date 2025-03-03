package com.example.unique

data class Product(
    val name: String,
    val imageUrl: String,
    val price: Double,
    val category: String,

    val id: String,
    var quantity: Int = 1 // Default quantity set to 1
)
