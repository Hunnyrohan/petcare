package com.example.unique

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.unique.R
import com.example.unique.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var profileImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        loadUserProfile()

        binding.editProfileButton.setOnClickListener {
            startEditingProfile()
        }

        binding.saveProfileButton.setOnClickListener {
            saveProfileDetails()
        }

        binding.profileImage.setOnClickListener {
            pickProfileImage()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.userName.setText(document.getString("firstName") + " " + document.getString("lastName"))
                        binding.userEmail.text = currentUser.email
                        binding.userPhone.setText(document.getString("phone"))
                        binding.userAddress.setText(document.getString("address"))

                        val profileImageRef = firebaseStorage.reference.child("profile_images/$userId.jpg")
                        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(this)
                                .load(uri)
                                .placeholder(R.drawable.baseline_perm_identity_24)
                                .into(binding.profileImage)
                        }.addOnFailureListener {
                            binding.profileImage.setImageResource(R.drawable.baseline_perm_identity_24)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to load user profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun startEditingProfile() {
        binding.userName.isEnabled = true
        binding.userPhone.isEnabled = true
        binding.userAddress.isEnabled = true
        binding.editProfileButton.visibility = View.GONE
        binding.saveProfileButton.visibility = View.VISIBLE
    }

    private fun saveProfileDetails() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val firstNameLastName = binding.userName.text.toString()
            val phone = binding.userPhone.text.toString()
            val address = binding.userAddress.text.toString()

            // Split firstName and lastName
            val nameParts = firstNameLastName.split(" ")
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            // Create a map of the updated data
            val userUpdates = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "phone" to phone,
                "address" to address
            )

            firestore.collection("users").document(userId)
                .update(userUpdates)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Profile details updated successfully:Kindly Login Again.", Toast.LENGTH_SHORT).show()
                    binding.userName.isEnabled = false
                    binding.userPhone.isEnabled = false
                    binding.userAddress.isEnabled = false
                    binding.editProfileButton.visibility = View.VISIBLE
                    binding.saveProfileButton.visibility = View.GONE
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to update profile details", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun pickProfileImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                // Directly use the selected image URI without cropping
                profileImageUri = selectedImageUri
                binding.profileImage.setImageURI(profileImageUri)
                uploadProfileImage() // Upload the selected image
            }
        }
    }

    private fun uploadProfileImage() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && profileImageUri != null) {
            val userId = currentUser.uid
            val storageRef = firebaseStorage.reference.child("profile_images/$userId.jpg")

            storageRef.putFile(profileImageUri!!)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Profile image updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
