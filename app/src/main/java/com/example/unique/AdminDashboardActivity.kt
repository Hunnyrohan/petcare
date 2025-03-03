package com.example.unique

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var actionSpinner: Spinner
    private lateinit var addProductLayout: LinearLayout
    private lateinit var deleteProductLayout: LinearLayout
    private lateinit var updateOrderLayout: LinearLayout
    private lateinit var productListView: ListView
    private lateinit var collectionSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var products: MutableList<ProductItem>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productImageView: ImageView
    private lateinit var uploadImageButton: Button
    private var imageUri: Uri? = null
    private lateinit var orders: MutableList<Map<String, String>>

    data class ProductItem(
        val id: String,
        val name: String,
        val category: String,
        val collection: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        firestore = FirebaseFirestore.getInstance()

        // Spinner for selecting action
        actionSpinner = findViewById(R.id.actionSpinner)
        val options = arrayOf("Select Action", "Add Product", "Delete Product", "Update Order Status")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        actionSpinner.adapter = spinnerAdapter

        // Layouts for Add Product, Delete Product, and Update Order Status
        addProductLayout = findViewById(R.id.addProductLayout)
        deleteProductLayout = findViewById(R.id.deleteProductsLayout)
        updateOrderLayout = findViewById(R.id.updateOrderLayout)
        productListView = findViewById(R.id.productListView)

        // Spinners for selecting collection and category
        collectionSpinner = findViewById(R.id.collectionSpinner)
        val collections = arrayOf("Select Collection", "bestDeals", "adoptedPets")
        val collectionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, collections)
        collectionSpinner.adapter = collectionAdapter

        categorySpinner = findViewById(R.id.categorySpinner)
        val categories = arrayOf("Select Category", "Cats", "Dogs", "Fish", "Birds", "Accessories")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        categorySpinner.adapter = categoryAdapter

        // Image view and button for uploading product image
        productImageView = findViewById(R.id.productImageView)
        uploadImageButton = findViewById(R.id.uploadImageButton)

        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }

        // Set default visibility
        addProductLayout.visibility = View.GONE
        deleteProductLayout.visibility = View.GONE
        updateOrderLayout.visibility = View.GONE

        actionSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> {
                        // Show Add Product form
                        addProductLayout.visibility = View.VISIBLE
                        deleteProductLayout.visibility = View.GONE
                        updateOrderLayout.visibility = View.GONE
                    }
                    2 -> {
                        // Show Delete Product list
                        addProductLayout.visibility = View.GONE
                        deleteProductLayout.visibility = View.VISIBLE
                        updateOrderLayout.visibility = View.GONE
                        loadDeleteProductList()
                    }
                    3 -> {
                        // Show Update Order Status layout
                        addProductLayout.visibility = View.GONE
                        deleteProductLayout.visibility = View.GONE
                        updateOrderLayout.visibility = View.VISIBLE
                        loadOrdersList()
                    }
                    else -> {
                        // Hide all layouts
                        addProductLayout.visibility = View.GONE
                        deleteProductLayout.visibility = View.GONE
                        updateOrderLayout.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        // Handle Add Product form submission
        val addProductButton: Button = findViewById(R.id.addProductButton)
        addProductButton.setOnClickListener {
            val collection = collectionSpinner.selectedItem.toString()
            val name = findViewById<EditText>(R.id.productNameInput).text.toString().trim()
            val price = findViewById<EditText>(R.id.productPriceInput).text.toString().toDoubleOrNull()
            val description = findViewById<EditText>(R.id.productDescriptionInput).text.toString().trim()
            val category = categorySpinner.selectedItem.toString()
            val productId = findViewById<EditText>(R.id.productIdInput).text.toString().trim()

            if (collection == "Select Collection" || category == "Select Category" ||
                name.isEmpty() || price == null || description.isEmpty() || productId.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val storageRef = FirebaseStorage.getInstance().reference
            if (imageUri != null) {
                val imageRef = storageRef.child("products/${System.currentTimeMillis()}.jpg")
                imageRef.putFile(imageUri!!).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val product = hashMapOf(
                            "name" to name,
                            "price" to price,
                            "description" to description,
                            "category" to category,
                            "productId" to productId,
                            "imageUrl" to imageUrl
                        )
                        firestore.collection(collection).add(product)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show()
                                // Clear input fields after successful addition
                                clearAddProductFields()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show()
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearAddProductFields() {
        findViewById<EditText>(R.id.productNameInput).text.clear()
        findViewById<EditText>(R.id.productPriceInput).text.clear()
        findViewById<EditText>(R.id.productDescriptionInput).text.clear()
        findViewById<EditText>(R.id.productIdInput).text.clear()
        collectionSpinner.setSelection(0)
        categorySpinner.setSelection(0)
        productImageView.setImageResource(android.R.drawable.ic_menu_camera)
        imageUri = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            productImageView.setImageURI(imageUri)
        }
    }

    private fun loadDeleteProductList() {
        products = mutableListOf()
        val collections = listOf("bestDeals", "adoptedPets")

        collections.forEach { collection ->
            firestore.collection(collection).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val name = document.getString("name") ?: ""
                        val category = document.getString("category") ?: ""
                        val productId = document.id

                        products.add(ProductItem(productId, name, category, collection))
                    }

                    // Sort products by category
                    products.sortBy { it.category }

                    productAdapter = ProductAdapter(this, products) { productItem ->
                        deleteProduct(productItem)
                    }
                    productListView.adapter = productAdapter
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteProduct(productItem: ProductItem) {
        firestore.collection(productItem.collection).document(productItem.id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show()
                loadDeleteProductList() // Refresh the list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadOrdersList() {
        orders = mutableListOf<Map<String, String>>()

        firestore.collection("orders").get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val customerName = document.getString("customerName")
                    val transactionId = document.getString("transactionId")
                    val orderDate = document.getString("orderDate") ?: ""
                    val items = (document.get("items") as? List<String>)?.joinToString(", ") ?: ""

                    if (customerName != null && transactionId != null) {
                        val order = mapOf(
                            "customerName" to customerName,
                            "transactionId" to transactionId,
                            "orderDate" to orderDate,
                            "items" to items
                        )
                        orders.add(order)
                    }
                }

                // Create order cards
                val orderListView: ListView = findViewById(R.id.orderListView)
                val orderAdapter = object : ArrayAdapter<Map<String, String>>(this, 0, orders) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val order = getItem(position)
                        val orderCard = layoutInflater.inflate(R.layout.order_card, parent, false)

                        val customerNameTextView = orderCard.findViewById<TextView>(R.id.customerName)
                        val transactionIdTextView = orderCard.findViewById<TextView>(R.id.transactionId)
                        val orderDateTextView = orderCard.findViewById<TextView>(R.id.orderDate)
                        val itemsTextView = orderCard.findViewById<TextView>(R.id.items)
                        val orderStatusSpinner = orderCard.findViewById<Spinner>(R.id.orderStatusSpinner)
                        val updateStatusButton = orderCard.findViewById<Button>(R.id.updateStatusButton)

                        customerNameTextView.text = order?.get("customerName").toString()
                        transactionIdTextView.text = order?.get("transactionId").toString()
                        orderDateTextView.text = order?.get("orderDate").toString()
                        itemsTextView.text = order?.get("items").toString()

                        val statusOptions = arrayOf("Shipped", "Delivered", "Packed", "Out for Delivery")
                        val spinnerAdapter = ArrayAdapter(this@AdminDashboardActivity, android.R.layout.simple_spinner_item, statusOptions)
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        orderStatusSpinner.adapter = spinnerAdapter

                        updateStatusButton.setOnClickListener {
                            val selectedStatus = orderStatusSpinner.selectedItem.toString()
                            val transactionId = order?.get("transactionId")

                            if (transactionId != null) {
                                updateOrderStatus(transactionId, selectedStatus)
                            }
                        }

                        return orderCard
                    }
                }
                orderListView.adapter = orderAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateOrderStatus(transactionId: String, status: String) {
        firestore.collection("orders").whereEqualTo("transactionId", transactionId).get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    val updatedStatus: Map<String, Any> = hashMapOf("status" to status)
                    firestore.collection("orders").document(document.id).update(updatedStatus)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Order status updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update order status", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to find order", Toast.LENGTH_SHORT).show()
            }
    }

    // Inner class for custom adapter
    class ProductAdapter(
        private val context: Activity,
        private val products: List<ProductItem>,
        private val onDeleteClick: (ProductItem) -> Unit
    ) : ArrayAdapter<ProductItem>(context, 0, products) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.product_delete_card, parent, false)

            val product = getItem(position)

            val productNameTextView: TextView = view.findViewById(R.id.productNameTextView)
            val deleteButton: Button = view.findViewById(R.id.deleteProductButton)
            val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
            val collectionTextView: TextView = view.findViewById(R.id.collectionTextView)

            productNameTextView.text = product?.name
            categoryTextView.text = "Category: ${product?.category}"
            collectionTextView.text = "Collection: ${product?.collection}"

            deleteButton.setOnClickListener {
                product?.let { onDeleteClick(it) }
            }

            return view
        }
    }
}