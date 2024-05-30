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
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentStepTwoVerificationBinding
import com.example.linkup.utils.SendEmail
import com.example.linkup.utils.observeNetworkStatus
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
    private var internetStatus: Boolean? = null
    private var firstTimeCheck: Boolean = false

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

        // Observe network connectivity status
        observeNetworkStatus(
            requireContext(),
            viewLifecycleOwner.lifecycleScope
        ) { title, message, isSuccess ->
            if (isSuccess) {
                if (firstTimeCheck) {
                    showToast.motionSuccessToast(title, message)
                }
                internetStatus = true
            } else {
                showToast.motionWarningToast(title, message)
                firstTimeCheck = true
                internetStatus = false
            }
        }

        // Setup listeners and initializations
        binding.step2VerificationFragmentPasswordTIET.addTextChangedListener(createTextWatcher { s ->
            password = s.toString()
        })

        binding.step2VerificationFragmentVerifyBT.setOnClickListener {
            vibrator.vibrate(100)
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                handlePasswordVerification()
            }
        }

        binding.step2VerificationFragmentForgetTV.setOnClickListener {
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                vibrator.vibrate(100)
                handleForgetPassword()
            }
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
            showToast.warningToast("Please enter password!")
        } else {
            if (password!!.length >= 6) {
                workInProgressStart()
                checkPassword(password!!)
            } else {
                showToast.warningToast("Password must be at least 6 characters long!")
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
                            showToast.motionSuccessToast("Success", "Login successful")
                            sendToHome()
                        } else {
                            workInProgressEnd()
                            showToast.motionErrorToast(
                                "Error status",
                                "Incorrect password. Please try again."
                            )
                        }
                    } else {
                        workInProgressEnd()
                        if (!internetStatus!!) {
                            showToast.motionWarningToast("Warning", "You are currently offline")
                        } else {
                            showToast.motionErrorToast("Error status", "No such document found.")
                            sendToSignIn()
                        }

                    }
                }
                .addOnFailureListener { e ->
                    workInProgressEnd()
                    if (!internetStatus!!) {
                        showToast.motionWarningToast("Warning", "You are currently offline")
                    } else {
                        showToast.motionErrorToast("Error status", "Error fetching document")
                        Log.d(TAG, "checkPassword: ${e.message}")
                        sendToSignIn()
                    }
                }
                .addOnCompleteListener {
                    workInProgressEnd()
                }
        } else {
            workInProgressEnd()
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                showToast.motionErrorToast("Error status", "User not logged in.")
                sendToSignIn()
            }
        }
    }

    private fun handleForgetPassword() {
        workInProgressStart()
        fetchUserDetails {
            verificationCode =
                sendEmail.sendAccountVerifyEmailOtp(email.toString(), name.toString())
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
                    workInProgressEnd()
                    if (!internetStatus!!) {
                        showToast.motionWarningToast("Warning", "You are currently offline")
                    } else {
                        Log.e("TAG", "No such document found for user $userUid")
                        showToast.motionErrorToast("Error status", "No user details found")
                        sendToSignIn()
                    }
                }
                callback()
            }
            .addOnFailureListener { e ->
                workInProgressEnd()
                if (!internetStatus!!) {
                    showToast.motionWarningToast("Warning", "You are currently offline")
                } else {
                    Log.e("TAG", "Error fetching user details: ${e.message}")
                    showToast.motionErrorToast("Error status", "${e.message}")
                    callback()
                    sendToSignIn()
                }
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val navController = findNavController()
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.stepTwoVerificationFragment) {
                // Handle back press action
                findNavController().navigate(R.id.action_stepTwoVerificationFragment_to_signInFragment)
            } else {
                // Call the super method to allow normal back press behavior
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

}
