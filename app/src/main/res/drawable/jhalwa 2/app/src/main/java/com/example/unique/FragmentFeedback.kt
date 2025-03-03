package com.example.unique

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.unique.databinding.FragmentFeedbackBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedRating: Int = 0  // To store the selected rating

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Rating stars
        val stars = listOf(
            binding.star1, binding.star2, binding.star3, binding.star4, binding.star5
        )

        // Setting click listeners for stars
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                selectedRating = index + 1
                updateStarImages(stars, selectedRating)
            }
        }

        // Submit feedback and rating
        binding.submitFeedbackButton.setOnClickListener {
            val feedbackText = binding.feedbackText.text.toString()
            val user = firebaseAuth.currentUser

            if (feedbackText.isNotEmpty() && selectedRating > 0) {
                val feedback = hashMapOf(
                    "userId" to user?.uid,
                    "feedback" to feedbackText,
                    "rating" to selectedRating  // Store rating along with feedback
                )

                db.collection("feedbacks").add(feedback).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                    // Optional: Clear the form after submission
                    binding.feedbackText.text.clear()
                    updateStarImages(stars, 0)  // Reset star rating
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Failed to submit feedback: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter feedback and select a rating", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to update star images based on selected rating
    private fun updateStarImages(stars: List<ImageView>, rating: Int) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.baseline_star_24)  // Filled star
            } else {
                star.setImageResource(R.drawable.baseline_star_border_24)  // Unfilled star
            }
        }
    }
}
