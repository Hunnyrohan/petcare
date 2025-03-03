package com.example.unique

import HomeFragment
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.unique.databinding.FragmentOrderSuccessBinding

class OrderSuccessFragment : Fragment() {

    private var _binding: FragmentOrderSuccessBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private val redirectToHomeRunnable = {
        navigateToHomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start a delayed transition to HomeFragment after 5 seconds
        handler.postDelayed(redirectToHomeRunnable, 5000)

        // Override back button to navigate to HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToHomeFragment()
        }
    }

    private fun navigateToHomeFragment() {
        // Clear the back stack completely and load HomeFragment
        requireActivity().supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(redirectToHomeRunnable) // Clean up to avoid memory leaks
    }
}
