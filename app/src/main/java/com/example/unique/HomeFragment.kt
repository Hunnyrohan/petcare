import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.unique.BestDealsAdapter
import com.example.unique.CategoryAdapter
import com.example.unique.Product
import com.example.unique.R
import com.example.unique.SearchFragment
import com.example.unique.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bestDealsAdapter: BestDealsAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private var productList: List<Product> = listOf()
    private var categoryList: List<String> = listOf("Main", "Dogs", "Cats", "Fish", "Accessories", "Adopt Pet")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryMenu()
        setupProductRecyclerView()
        setupCategoryGridImages()
        setupSearchBar() // Set up the search bar click listener

        // Load default products from Firebase (Main Category)
        loadProductsByCategory("Main")
    }

    private fun setupCategoryMenu() {
        // Setting up the category menu as a horizontal list
        binding.categoryMenuRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            // Load products based on the selected category
            loadProductsByCategory(category)
        }
        binding.categoryMenuRecyclerView.adapter = categoryAdapter
    }

    private fun setupProductRecyclerView() {
        // Set up a GridLayoutManager for product listing (2 columns)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        bestDealsAdapter = BestDealsAdapter(productList)
        binding.recyclerView.adapter = bestDealsAdapter
    }

    private fun setupCategoryGridImages() {
        // Setup onClickListeners for category grid images
        binding.dogCategoryCard.setOnClickListener { loadProductsByCategory("Dogs") }
        binding.catCategoryCard.setOnClickListener { loadProductsByCategory("Cats") }
        binding.fishCategoryCard.setOnClickListener { loadProductsByCategory("Fish") }
        binding.accessoriesCategoryCard.setOnClickListener { loadProductsByCategory("Accessories") }
    }

    private fun setupSearchBar() {
        // Set up the click listener for the search bar
        binding.searchBarCard.setOnClickListener {
            // Navigate to SearchFragment with animations
            val searchFragment = SearchFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            // Set custom animations for the transition
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

            transaction.replace(R.id.fragment_container, searchFragment) // Use your fragment container ID
                .addToBackStack(null) // Optional: Add to back stack to allow navigation back
                .commit()
        }
    }

    private fun loadProductsByCategory(category: String) {
        // Set visibility of main page elements for the "Main" category
        val isMainCategory = category == "Main"
        binding.homeImageCard.visibility = if (isMainCategory) View.VISIBLE else View.GONE
        binding.exploreTextView.visibility = if (isMainCategory) View.VISIBLE else View.GONE
        binding.categoryGrid.visibility = if (isMainCategory) View.VISIBLE else View.GONE
        binding.bestDealsTextView.visibility = if (isMainCategory) View.VISIBLE else View.GONE

        val db = FirebaseFirestore.getInstance()

        // Check if the "Adopt Pet" category is selected
        if (category == "Adopt Pet") {
            // Fetch pets from the "adoptedPets" Firestore collection
            db.collection("adoptedPets")
                .get()
                .addOnSuccessListener { documents ->
                    productList = documents.map { document ->
                        val name = document.getString("name") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val age = document.getDouble("age") ?: 0.0
                        val id = document.id
                        // Assuming pets have "name", "imageUrl", and "age" fields
                        Product(name, imageUrl, age, category, id)
                    }
                    // Update the adapter with the new product list (adopted pets)
                    bestDealsAdapter.updateProducts(productList)
                }
                .addOnFailureListener { exception ->
                    // Handle any errors while fetching data
                    // e.g., log the error
                }
        } else {
            // Fetch products from the "bestDeals" Firestore collection for other categories
            db.collection("bestDeals")
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener { documents ->
                    productList = documents.map { document ->
                        val name = document.getString("name") ?: ""
                        val imageUrl = document.getString("imageUrl") ?: ""
                        val price = document.getDouble("price") ?: 0.0
                        val id = document.id
                        Product(name, imageUrl, price, category, id)
                    }
                    // Update the adapter with the new product list
                    bestDealsAdapter.updateProducts(productList)
                }
                .addOnFailureListener { exception ->
                    // Handle any errors while fetching data
                    // e.g., log the error
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}