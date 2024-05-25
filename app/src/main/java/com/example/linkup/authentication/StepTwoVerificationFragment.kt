package com.example.linkup.authentication

import ShowToast
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
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
import com.example.linkup.databinding.FragmentStepTwoVerificationBinding
import com.example.linkup.utility.SendEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StepTwoVerificationFragment : Fragment() {

    private lateinit var binding: FragmentStepTwoVerificationBinding
    private val args: StepTwoVerificationFragmentArgs by navArgs()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var vibrator: Vibrator

    private var password: String? = null
    private var email: String? = null
    private var verificationCode: String? = null
    private var name: String? = null

    private val sendEmail = SendEmail()
    private lateinit var showToast: ShowToast

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStepTwoVerificationBinding.inflate(inflater, container, false)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize Firebase and other services
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Initialize ShowToast
        showToast = ShowToast(requireContext())

        // Setup listeners and initializations
        binding.step2VerificationFragmentPasswordTIET.addTextChangedListener(createTextWatcher { s ->
            password = s.toString()
        })

        binding.step2VerificationFragmentVerifyBT.setOnClickListener {
            vibrator.vibrate(100)
            handlePasswordVerification()
        }

        binding.step2VerificationFragmentForgetTV.setOnClickListener {
            vibrator.vibrate(100)
            handleForgetPassword()
        }

        return binding.root
    }

    private fun workInProgressStart() {
        binding.step2VerificationFragmentPasswordTIL.isEnabled = false
        binding.step2VerificationFragmentVerifyBT.isEnabled = false
        binding.step2VerificationFragmentVerifyBT.visibility = View.INVISIBLE
        binding.step2VerificationFragmentPB.visibility = View.VISIBLE
        binding.step2VerificationFragmentForgetTV.isEnabled = false
    }

    private fun workInProgressEnd() {
        binding.step2VerificationFragmentPasswordTIL.isEnabled = true
        binding.step2VerificationFragmentVerifyBT.isEnabled = true
        binding.step2VerificationFragmentVerifyBT.visibility = View.VISIBLE
        binding.step2VerificationFragmentPB.visibility = View.GONE
        binding.step2VerificationFragmentForgetTV.isEnabled = true
    }

    private fun createTextWatcher(action: (CharSequence?) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                action(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun handlePasswordVerification() {
        if (password.isNullOrBlank()) {
            showToast.infoToast("Please enter password!")
        } else {
            if (password!!.length >= 6) {
                workInProgressStart()
                checkPassword(password!!)
            } else {
                showToast.infoToast("Password must be at least 6 characters long!")
            }
        }
    }

    private fun checkPassword(password: String) {
        val userUid = args.userUid
        if (userUid != null) {
            val docRef = fStore.collection("usersDetails").document(userUid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val storedPassword = document.getString("userPassword")
                        if (storedPassword == password) {
                            workInProgressEnd()
                            showToast.motionSuccessToast("Success","Login successful")
                            sendToHome()
                        } else {
                            workInProgressEnd()
                            showToast.errorToast("Incorrect password. Please try again.")
                        }
                    } else {
                        workInProgressEnd()
                        showToast.errorToast("No such document found.")
                        sendToSignIn()
                    }
                }
                .addOnFailureListener { e ->
                    workInProgressEnd()
                    showToast.errorToast("Error fetching document")
                    Log.d(TAG, "checkPassword: ${e.message}")
                    sendToSignIn()
                }
                .addOnCompleteListener {
                    workInProgressEnd()
                }
        } else {
            workInProgressEnd()
            showToast.errorToast("User not logged in.")
            sendToSignIn()
        }
    }

    private fun handleForgetPassword() {
        workInProgressStart()
        fetchUserDetails {
            verificationCode = sendEmail.sendEmailOtp(email.toString(), name.toString())
            workInProgressEnd()
            sendToForgetPasswordOtp()
        }
    }

    private fun fetchUserDetails(callback: () -> Unit) {
        val userUid = args.userUid
        val documentReference = fStore.collection("usersDetails").document(userUid)

        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    email = documentSnapshot.getString("userEmailId")
                    name = documentSnapshot.getString("userName")
                    Log.d("TAG", "User details fetched successfully: email=$email, name=$name")
                } else {
                    Log.e("TAG", "No such document found for user $userUid")
                    showToast.errorToast("No user details found")
                    workInProgressEnd()
                    sendToSignIn()
                }
                callback()
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Error fetching user details: ${e.message}")
                showToast.errorToast("Error fetching user details: ${e.message}")
                callback()
                workInProgressEnd()
                sendToSignIn()
            }
    }

    private fun sendToForgetPasswordOtp() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            StepTwoVerificationFragmentDirections.actionStepTwoVerificationFragmentToForgetPasswordOtpFragment(
                args.phoneNumber,
                email.toString(),
                name.toString(),
                args.userUid,
                verificationCode.toString()
            )
        findNavController().navigate(direction)
    }

    private fun sendToHome() {
        SplashFragment.setLoginStatus(requireContext(), true)
        val direction =
            StepTwoVerificationFragmentDirections.actionStepTwoVerificationFragmentToHomeFragment(
                args.phoneNumber,
                args.userUid
            )
        findNavController().navigate(direction)
    }

    private fun sendToSignIn() {
        SplashFragment.setLoginStatus(requireContext(), false)
        // Before navigating, ensure we are in the correct fragment context
        val navController = findNavController()
        Log.d("NavigationDebug", "Current Destination: ${navController.currentDestination?.label}")
        if (navController.currentDestination?.id == R.id.stepTwoVerificationFragment) {
            navController.navigate(R.id.action_stepTwoVerificationFragment_to_signInFragment)
        } else {
            Log.e(
                "NavigationDebug",
                "Attempted navigation from wrong fragment: ${navController.currentDestination?.label}"
            )
        }
    }
}
