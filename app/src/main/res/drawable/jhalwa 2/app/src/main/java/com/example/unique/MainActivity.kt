package com.example.unique

import HomeFragment
import AdoptFragment
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // Reference to the HomeFragment
    private val homeFragment = HomeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize DrawerLayout, NavigationView, and Toolbar
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        // Set toolbar as the ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24) // Menu icon

        // Load the Home Fragment by default
        if (savedInstanceState == null) {
            loadFragment(homeFragment, false) // Load without adding to back stack
        }

        // Fetch and display the user's name and profile picture
        loadUserProfile()

        // Set up the drawer navigation menu
        setupDrawerMenu()

        // Bottom Navigation setup
        setupBottomNavigation()

        // Set up navigation header options click listeners
        setupNavHeaderOptions()
    }

    // Handle the back button press
    override fun onBackPressed() {
        val fragmentCount = supportFragmentManager.backStackEntryCount
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment is HomeFragment) {
            // If we are already on HomeFragment, do nothing
            // You can show a toast here if needed, e.g. "Press back again to exit"
        } else if (fragmentCount > 0) {
            // If there are fragments in the back stack, pop the last one
            supportFragmentManager.popBackStack()
        } else {
            // If no fragments in the back stack, finish the activity to exit the app
            finish() // This will exit the app without logging out
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)

        bottomNavView.selectedItemId = R.id.nav_home

        bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeFragment, false) // Load HomeFragment without adding to back stack
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment(),false) // Load SearchFragment for searching products
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.nav_adopt -> {
                    loadFragment(AdoptFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val headerView = navigationView.getHeaderView(0)

            // References to the views in the navigation header layout
            val profileImageView: ImageView = headerView.findViewById(R.id.nav_header_profile_image)
            val userNameTextView: TextView = headerView.findViewById(R.id.nav_header_title)
            val userEmailTextView: TextView = headerView.findViewById(R.id.nav_header_subtitle)

            // Set user email
            userEmailTextView.text = currentUser.email

            // Fetch user name from Firestore
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: "User"
                    val lastName = document.getString("lastName") ?: ""
                    val fullName = "$firstName $lastName".trim()
                    userNameTextView.text = fullName
                    userEmailTextView.text = "Dear $fullName, Welcome to PetCare"
                } else {
                    userNameTextView.text = "User"
                    userEmailTextView.text = "Dear User, Welcome to PetCare"
                }
            }.addOnFailureListener {
                userNameTextView.text = "User"
                userEmailTextView.text = "Dear User, Welcome to PetCare"
            }

            // Load profile image from Firebase Storage if available
            val storageRef = FirebaseStorage.getInstance().reference
            val profileImageRef = storageRef.child("profile_images/$userId.jpg")

            profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.baseline_perm_identity_24) // Use your placeholder
                    .into(profileImageView)
            }.addOnFailureListener {
                profileImageView.setImageResource(R.drawable.baseline_perm_identity_24) // Default image
            }
        }
    }

    private fun setupDrawerMenu() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout() // Call logout function
                    true
                }
                R.id.nav_feedback -> {
                    loadFragment(FeedbackFragment()) // Load FeedbackFragment
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(CartFragment()) // Load CartFragment
                    drawerLayout.closeDrawers() // Close the drawer
                    true
                }
                R.id.nav_orders->{
                    loadFragment(OrderDetailsFragment())
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavHeaderOptions() {
        val headerView = navigationView.getHeaderView(0)

        // Handle Home click
        headerView.findViewById<View>(R.id.nav_home).setOnClickListener {
            loadFragment(homeFragment, false) // Load HomeFragment without adding to back stack
            drawerLayout.closeDrawers()
        }

        // Handle Search click
        headerView.findViewById<View>(R.id.nav_search).setOnClickListener {
            loadFragment(SearchFragment()) // Load SearchFragment (implement it as needed)
            drawerLayout.closeDrawers()
        }

        // Handle Cart click
        headerView.findViewById<View>(R.id.nav_cart).setOnClickListener {
            loadFragment(CartFragment())
            drawerLayout.closeDrawers()
        }

        // Handle Profile click
        headerView.findViewById<View>(R.id.nav_profile).setOnClickListener {
            loadFragment(ProfileFragment())
            drawerLayout.closeDrawers()
        }

        // Handle Feedback click
        headerView.findViewById<View>(R.id.nav_feedback).setOnClickListener {
            loadFragment(FeedbackFragment())
            drawerLayout.closeDrawers()
        }
        headerView.findViewById<View>(R.id.nav_orders).setOnClickListener {
            loadFragment(OrderDetailsFragment()) // Load OrderDetailsFragment when "Your Orders" is clicked
            drawerLayout.closeDrawers()
        }

        // Handle Logout click
        headerView.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            logout()
        }
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null) // Add the transaction to the back stack if requested
        }

        transaction.commit()
    }

    private fun logout() {
        firebaseAuth.signOut() // Sign out from Firebase
        startActivity(Intent(this, LoginActivity::class.java)) // Navigate to LoginActivity
        finish() // Ensure the current activity is finished after logout
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
