package com.example.linkup.authentication

import ShowToast
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignInOtpBinding
import com.example.linkup.utils.observeNetworkStatus
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SignInOtpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignInOtpBinding

    //Declare nav graph argument
    private val args: SignInOtpFragmentArgs by navArgs()

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Firebase database
    private lateinit var fStore: FirebaseFirestore

    //Layout functionality variable
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var resendOtpProcess = false
    private var userUid: String? = null
    private var email: String? = null
    private var twoStepVerification: Boolean? = null
    private lateinit var showToast: ShowToast
    private var firstTimeCheck: Boolean = false
    private var internetStatus: Boolean? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Binding to xml layout
        binding = FragmentSignInOtpBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //Vibration instance create
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //Fetch and show phone number from previous fragment
        binding.signInOTPFragmentPhoneNoTV.text = args.phoneNumber
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
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

        //Handle action to verify button
        binding.signInOTPFragmentVerifyBT.setOnClickListener {
            vibrator.vibrate(100)
            //Fetch otp from user
            val typeOTP = binding.signInOTPFragmentOtpET.text
            if (!internetStatus!!){
                showToast.motionWarningToast("Warning", "You are currently offline")
            }else{
                if (typeOTP.length == 6) {
                    //Disable all element
                    workInProgressStart()
                    val credential = PhoneAuthProvider.getCredential(args.otp, typeOTP.toString())
                    signInWithPhoneAuthCredential(credential)
                } else {
                    showToast.infoToast("Please enter a 6-digit OTP.")
                }
            }
        }

        //Handle action to resend otp
        binding.resendOTPTV.setOnClickListener {
            vibrator.vibrate(100)
            if (!internetStatus!!){
                showToast.motionWarningToast("Warning", "You are currently offline")
            }else{
                if (!resendOtpProcess) {
                    //Disable all element
                    workInProgressStart()
                    //Resend otp
                    resendVerificationCode()
                    resendOTPTvVisibility()
                } else {
                    showToast.infoToast("Otp already send")
                }
            }
        }
        return binding.root
    }

    //Elements enable and disable function
    private fun workInProgressStart() {
        binding.signInOTPFragmentOtpET.isEnabled = false
        binding.signInOTPFragmentVerifyBT.isEnabled = false
        binding.signInOTPFragmentVerifyBT.visibility = View.INVISIBLE
        binding.signInOTPFragmentPB.visibility = View.VISIBLE
        binding.resendOTPTV.isEnabled = false
    }

    private fun workInProgressEnd() {
        binding.signInOTPFragmentOtpET.isEnabled = true
        binding.signInOTPFragmentVerifyBT.isEnabled = true
        binding.signInOTPFragmentVerifyBT.visibility = View.VISIBLE
        binding.signInOTPFragmentPB.visibility = View.GONE
        binding.resendOTPTV.isEnabled = true
    }

    //Resend otp mode ui
    @SuppressLint("ResourceAsColor")
    private fun resendOTPTvVisibility() {
        resendOtpProcess = true
        binding.resendOTPTV.setTextColor(Color.RED)
        binding.resendOTPTV.setText("Waiting for 1 minute")
        Handler(Looper.myLooper()!!).postDelayed({
            binding.resendOTPTV.setTextColor(Color.BLACK)
            binding.resendOTPTV.setText(R.string.resend_otp)
            resendOtpProcess = false
        }, 60000)
    }

    //Firebase authentication configuration and code send function
    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(args.phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callBacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(args.resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            showToast.errorToast("Verification failed")
            Log.d(TAG, "onVerificationFailed: ${e.message}")
            workInProgressEnd()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            workInProgressEnd()
            resendToken = token
            showToast.successToast("Verification code resent.")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                findNextActivity()
            } else {
                showToast.errorToast("Sign in failed:${task.exception?.message}")
                Log.d(TAG, "signInWithPhoneAuthCredential:${task.exception?.message}")
                workInProgressEnd()
            }
        }
    }

    //Find Which fragment are next to come
    private fun findNextActivity() {
        // Fetch userUid from Current user details
        userUid = mAuth.currentUser?.uid
        if (userUid != null) {
            val documentReference = fStore.collection("usersDetails").document(userUid!!)
            documentReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        twoStepVerification = document.getBoolean("twoStepVerification")
                        email = document.getString("userEmailId")
                        if (twoStepVerification!!) {
                            workInProgressEnd()
                            // User data exists, proceed to twoStepVerification activity
                            sendToTwoStepVerification()
                            return@addOnCompleteListener
                        }
                        // User data exists, proceed to main activity
                        showToast.motionSuccessToast("Success","Login successful")
                        workInProgressEnd()
                        sendToHome()
                    } else {
                        // User data doesn't exist, proceed to sign-up activity
                        workInProgressEnd()
                        sendToSignUp()
                        return@addOnCompleteListener
                    }
                } else {
                    workInProgressEnd()
                    if (!internetStatus!!){
                        showToast.motionWarningToast("Warning", "You are currently offline")
                    }else{ showToast.errorToast("Error fetching user data!")
                        Log.d("TAG", "Task is unsuccessful: ${task.exception.toString()}")
                        sendToSignIn()
                    }
                }
            }
        } else {
            workInProgressEnd()
            if (!internetStatus!!){
                showToast.motionWarningToast("Warning", "You are currently offline")
            }else{showToast.errorToast("Internal error!")
                Log.d("TAG", "UserUid is null")
                sendToSignIn()
            }
        }
    }

    // Coming fragment
    private fun sendToSignUp() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            SignInOtpFragmentDirections.actionSignInOtpFragmentToSignUpFragment(
                args.phoneNumber,
                userUid.toString()
            )
        findNavController().navigate(direction)
    }

    private fun sendToHome() {
        SplashFragment.setLoginStatus(requireContext(), true)
        val direction =
            SignInOtpFragmentDirections.actionSignInOtpFragmentToHomeFragment(
                args.phoneNumber,
                userUid.toString()
            )
        findNavController().navigate(direction)
    }

    private fun sendToSignIn() {
        mAuth.signOut()
        SplashFragment.setLoginStatus(requireContext(), false)
        findNavController().navigate(R.id.action_signInOtpFragment_to_signInFragment)
    }

    private fun sendToTwoStepVerification() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            SignInOtpFragmentDirections.actionSignInOtpFragmentToStepTwoVerificationFragment(
                args.phoneNumber,
                userUid.toString()
            )
        findNavController().navigate(direction)
    }
}

