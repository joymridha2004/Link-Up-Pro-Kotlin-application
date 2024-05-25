package com.example.linkup.authentication

import ShowToast
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentHomeBinding
import com.example.linkup.databinding.FragmentSplashBinding
import com.google.api.LogDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val args: HomeFragmentArgs by navArgs()
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var showToast: ShowToast
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        if (mAuth.uid.toString() != SplashFragment.getUserUid(requireContext())) {
            fetchUserDetailsThenUpdate {}
        }
        binding.homeFragmentTV.text = "Home Fragment"

        binding.HomeFragmentLogoutButton.setOnClickListener {
//            mAuth.signOut()
//            sendToSignIn()
            showToast.motionWarningToast("Warning", "Currently logout feature not available yet!")
        }

        return binding.root
    }

    private fun sendToSignIn() {
        SplashFragment.setLoginStatus(requireContext(), false)
        findNavController().navigate(R.id.action_homeFragment_to_signInFragment)
    }

    private fun fetchUserDetailsThenUpdate(callback: () -> Unit) {
        val userUid = args.userUid
        val documentReference = fStore.collection("usersDetails").document(userUid)

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    SplashFragment.setUserUid(requireContext(), args.userUid)
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