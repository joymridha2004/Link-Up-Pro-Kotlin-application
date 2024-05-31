package com.example.linkup.authentication

import ShowToast
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentHomeBinding
import com.example.linkup.utils.observeNetworkStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var showToast: ShowToast
    private var firstTimeCheck: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Initialize Firebase and other services
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        // Initialize ShowToast
        showToast = ShowToast(requireContext())
        // Update all details in SharedPreferences
        fetchUserDetailsThenUpdate {}

        binding.homeFragmentTV.text = "Home Fragment"

        binding.HomeFragmentLogoutButton.setOnClickListener {
            mAuth.signOut()
            sendToSignIn()
        }

        // Observe network connectivity status
        observeNetworkStatus(
            requireContext(),
            viewLifecycleOwner.lifecycleScope
        ) { title, message, isSuccess ->
            if (isSuccess) {
                if (firstTimeCheck) {
                    showToast.motionSuccessToast(title, message)
                }
            } else {
                showToast.motionWarningToast(title, message)
                firstTimeCheck = true
            }
        }

        return binding.root
    }

    private fun sendToSignIn() {
        SplashFragment.setLoginStatus(requireContext(), false)
        findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
    }

    private fun fetchUserDetailsThenUpdate(callback: () -> Unit) {
        val userUid = mAuth.uid.toString()
        val documentReference = fStore.collection("usersDetails").document(userUid)

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    SplashFragment.setUserUid(requireContext(), userUid)
                    SplashFragment.setUserName(
                        requireContext(),
                        documentSnapshot.getString("userName").toString()
                    )
                    SplashFragment.setUserPhoneNumber(
                        requireContext(),
                        documentSnapshot.getString("userPhoneNumber").toString()
                    )
                    SplashFragment.setUserEmailId(
                        requireContext(),
                        documentSnapshot.getString("userEmailId").toString()
                    )
                    SplashFragment.setUserPassword(
                        requireContext(),
                        documentSnapshot.getString("userPassword").toString()
                    )
                    Log.d("TAG", "User details fetched successfully")
                } else {
                    Log.e("TAG", "No such document found for user $userUid")
                    showToast.errorToast("No user details found")
                }
                callback()
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error fetching user details: ${e.message}")
                showToast.errorToast("Error fetching user details")
                callback()
            }
    }
}
