package com.example.unique

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class SignupActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                profileImageView?.setImageURI(selectedImageUri)
            }
        }

    private var profileImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        val signUpButton: Button = findViewById(R.id.signupButton)
        val loginRedirectText: TextView = findViewById(R.id.loginRedirectText) // Find the TextView

        signUpButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.signupEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.signupPassword).text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user with email and password
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Show dialog to collect additional user details
                        showUserProfileDialog()
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Set OnClickListener for the login redirect text
        loginRedirectText.setOnClickListener {
            // Start LoginActivity
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun showUserProfileDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_user_profile)

        profileImageView = dialog.findViewById(R.id.profileImageView)
        val firstNameEditText: EditText = dialog.findViewById(R.id.firstNameEditText)
        val lastNameEditText: EditText = dialog.findViewById(R.id.lastNameEditText)
        val dobEditText: EditText = dialog.findViewById(R.id.dobEditText)
        val phoneEditText: EditText = dialog.findViewById(R.id.phoneEditText)
        val addressEditText: EditText = dialog.findViewById(R.id.addressEditText)
        val submitButton: Button = dialog.findViewById(R.id.submitButton)

        profileImageView?.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        dobEditText.setOnClickListener {
            showDatePickerDialog(dobEditText)
        }

        submitButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user profile
            saveUserProfile(firstName, lastName, dob, phone, address, dialog)
        }

        dialog.show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            editText.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun saveUserProfile(firstName: String, lastName: String, dob: String, phone: String, address: String, dialog: Dialog) {
        val userId = firebaseAuth.currentUser?.uid
        val imageRef = storageRef.child("profile_images/$userId.jpg")

        if (selectedImageUri != null) {
            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profileImageUrl = uri.toString()
                        val userProfile = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "dob" to dob,
                            "phone" to phone,
                            "address" to address,
                            "profileImageUrl" to profileImageUrl
                        )

                        userId?.let {
                            firestore.collection("users").document(it).set(userProfile)
                                .addOnSuccessListener {
                                    dialog.dismiss()
                                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
                                    val loginIntent = Intent(this, LoginActivity::class.java)
                                    startActivity(loginIntent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please select a profile image", Toast.LENGTH_SHORT).show()
        }
    }
}
