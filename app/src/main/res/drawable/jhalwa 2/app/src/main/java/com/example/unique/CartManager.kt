package com.example.unique

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private const val PREFS_NAME = "CartPrefs"
    private const val CART_ITEMS_KEY = "cart_items"
    private var cartItems = mutableListOf<Product>()

    fun initialize(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadCartFromPreferences(sharedPreferences)
    }

    fun addToCart(product: Product, context: Context) {
        val existingProduct = cartItems.find { it.id == product.id }
        if (existingProduct != null) {
            existingProduct.quantity += 1
        } else {
            cartItems.add(product)
        }
        saveCartToPreferences(context)
    }

    fun removeFromCart(product: Product, context: Context) {
        cartItems.remove(product)
        saveCartToPreferences(context)
    }

    fun getCartItems(): List<Product> {
        return cartItems
    }

    fun clearCart(context: Context) {
        cartItems.clear() // Clear the local cart items
        saveCartToPreferences(context) // Update shared preferences
    }

    private fun saveCartToPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(cartItems)
        editor.putString(CART_ITEMS_KEY, json)
        editor.apply()
    }

    private fun loadCartFromPreferences(sharedPreferences: SharedPreferences) {
        val gson = Gson()
        val json = sharedPreferences.getString(CART_ITEMS_KEY, null)
        val type = object : TypeToken<MutableList<Product>>() {}.type
        cartItems = gson.fromJson(json, type) ?: mutableListOf()
    }
}
