package com.example.unique

data class Order(
    val transactionId: String,
    val customerName: String,
    val address: String,
    val phoneNumber: String,
    val totalCost: Double,
    val items: List<Map<String, Any>>
)
