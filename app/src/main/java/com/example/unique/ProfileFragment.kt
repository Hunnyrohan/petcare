package com.example.unique

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.unique.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var profileImageUri: Uri? = null

    // Cache original values to support canceling edits
    private var originalName = ""
    private var originalPhone = ""
    private var originalAddress = ""
    private var isEditMode = false

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImageUri = it
                binding.profileImage.setImageURI(profileImageUri)
                uploadProfileImage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        setupInitialFieldState()
        loadUserProfile()

        // Edit mode toggles
        binding.editProfileButton.setOnClickListener {
            startEditingProfile(binding.userName)
        }

        binding.cancelEditButton.setOnClickListener {
            cancelEditing()
        }

        binding.saveProfileButton.setOnClickListener {
            saveProfileDetails()
        }

        // Allow tapping directly on fields to enter edit mode immediately
        val fieldClickListener = View.OnClickListener { v ->
            if (!isEditMode && v is EditText) {
                startEditingProfile(v)
            }
        }
        binding.userName.setOnClickListener(fieldClickListener)
        binding.userPhone.setOnClickListener(fieldClickListener)
        binding.userAddress.setOnClickListener(fieldClickListener)

        // Photo pickers
        binding.profileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.changePhotoBadge.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        return binding.root
    }

    private fun setupInitialFieldState() {
        setFieldsEditable(false)
    }

    private fun setFieldsEditable(editable: Boolean) {
        isEditMode = editable
        val editTexts = listOf(binding.userName, binding.userPhone, binding.userAddress)
        for (et in editTexts) {
            et.isFocusable = editable
            et.isFocusableInTouchMode = editable
            et.isCursorVisible = editable
            et.isClickable = true
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            binding.userEmail.text = "Not logged in"
            return
        }

        val userId = currentUser.uid
        binding.userEmail.text = currentUser.email ?: ""

        binding.profileProgressBar.visibility = View.VISIBLE

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!isAdded) return@addOnSuccessListener
                binding.profileProgressBar.visibility = View.GONE

                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName").orEmpty().trim()
                    val lastName = document.getString("lastName").orEmpty().trim()
                    val fullName = listOf(firstName, lastName).filter { it.isNotEmpty() }.joinToString(" ")

                    val phone = document.getString("phone").orEmpty()
                    val address = document.getString("address").orEmpty()

                    binding.userName.setText(fullName)
                    binding.userPhone.setText(phone)
                    binding.userAddress.setText(address)

                    if (fullName.isNotEmpty()) {
                        binding.profileDisplayName.text = fullName
                    }

                    originalName = fullName
                    originalPhone = phone
                    originalAddress = address

                    // Load profile picture from Firestore URL or fallback to Storage
                    val profileImageUrl = document.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.baseline_perm_identity_24)
                            .error(R.drawable.baseline_perm_identity_24)
                            .into(binding.profileImage)
                    } else {
                        loadProfileImageFromStorage(userId)
                    }
                } else {
                    // Document doesn't exist yet in Firestore
                    originalName = ""
                    originalPhone = ""
                    originalAddress = ""
                    loadProfileImageFromStorage(userId)
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                binding.profileProgressBar.visibility = View.GONE
                Toast.makeText(context, "Could not load profile details", Toast.LENGTH_SHORT).show()
                loadProfileImageFromStorage(userId)
            }
    }

    private fun loadProfileImageFromStorage(userId: String) {
        val profileImageRef = firebaseStorage.reference.child("profile_images/$userId.jpg")
        profileImageRef.downloadUrl.addOnSuccessListener { uri ->
            if (!isAdded) return@addOnSuccessListener
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.baseline_perm_identity_24)
                .error(R.drawable.baseline_perm_identity_24)
                .into(binding.profileImage)
        }.addOnFailureListener {
            if (!isAdded) return@addOnFailureListener
            binding.profileImage.setImageResource(R.drawable.baseline_perm_identity_24)
        }
    }

    private fun startEditingProfile(targetField: EditText?) {
        setFieldsEditable(true)
        binding.editProfileButton.visibility = View.GONE
        binding.editButtonsLayout.visibility = View.VISIBLE

        targetField?.let { field ->
            field.requestFocus()
            field.setSelection(field.text.length)
            showKeyboard(field)
        }
    }

    private fun cancelEditing() {
        // Revert to original cached values
        binding.userName.setText(originalName)
        binding.userPhone.setText(originalPhone)
        binding.userAddress.setText(originalAddress)
        binding.userName.error = null

        hideKeyboard()
        setFieldsEditable(false)
        binding.editProfileButton.visibility = View.VISIBLE
        binding.editButtonsLayout.visibility = View.GONE
    }

    private fun saveProfileDetails() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to update your profile", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val fullName = binding.userName.text.toString().trim()
        val phone = binding.userPhone.text.toString().trim()
        val address = binding.userAddress.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.userName.error = "Name cannot be empty"
            binding.userName.requestFocus()
            return
        }

        // Split fullName safely into firstName and lastName
        val nameParts = fullName.split(Regex("\\s+"))
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

        val userUpdates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "address" to address,
            "email" to (currentUser.email ?: "")
        )

        binding.profileProgressBar.visibility = View.VISIBLE
        binding.saveProfileButton.isEnabled = false
        binding.cancelEditButton.isEnabled = false

        // Use SetOptions.merge() so it updates existing documents or creates new ones without NOT_FOUND error!
        firestore.collection("users").document(userId)
            .set(userUpdates, SetOptions.merge())
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                binding.profileProgressBar.visibility = View.GONE
                binding.saveProfileButton.isEnabled = true
                binding.cancelEditButton.isEnabled = true

                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                binding.profileDisplayName.text = fullName
                originalName = fullName
                originalPhone = phone
                originalAddress = address

                hideKeyboard()
                setFieldsEditable(false)
                binding.editProfileButton.visibility = View.VISIBLE
                binding.editButtonsLayout.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                if (!isAdded) return@addOnFailureListener
                binding.profileProgressBar.visibility = View.GONE
                binding.saveProfileButton.isEnabled = true
                binding.cancelEditButton.isEnabled = true

                val message = exception.localizedMessage ?: exception.message ?: "Unknown error"
                Toast.makeText(context, "Failed to update profile: $message", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadProfileImage() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && profileImageUri != null) {
            val userId = currentUser.uid
            val storageRef = firebaseStorage.reference.child("profile_images/$userId.jpg")

            binding.profileProgressBar.visibility = View.VISIBLE
            storageRef.putFile(profileImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener downloadUrlSuccess@{ downloadUri ->
                        if (!isAdded) return@downloadUrlSuccess
                        binding.profileProgressBar.visibility = View.GONE

                        // Save image URL into Firestore as well
                        firestore.collection("users").document(userId)
                            .set(mapOf("profileImageUrl" to downloadUri.toString()), SetOptions.merge())

                        Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    if (!isAdded) return@addOnFailureListener
                    binding.profileProgressBar.visibility = View.GONE
                    val message = exception.localizedMessage ?: exception.message ?: "Upload failed"
                    Toast.makeText(context, "Failed to upload image: $message", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus ?: binding.root
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
