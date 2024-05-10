package com.example.linkup.authentication

import android.annotation.SuppressLint
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignInOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SignInOtpFragment : Fragment() {
    private lateinit var binding: FragmentSignInOtpBinding

    private val args: SignInOtpFragmentArgs by navArgs()
    private lateinit var vibrator: Vibrator
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationInProgress = false
    private var userUid: String? = null
    private var email: String? = null
    private var twoStepVerification: Boolean? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInOtpBinding.inflate(inflater, container, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        binding.signInOTPFragmentPhoneNoTV.text = args.phoneNumber

        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()

        binding.signInOTPFragmentVerifyButton.setOnClickListener {
            vibrator.vibrate(100)
            val typeOTP = binding.signInOTPFragmentOtpET.text
            if (!verificationInProgress) {
                if (typeOTP.length == 6) {
                    binding.signInOTPFragmentPB.visibility = View.VISIBLE
                    binding.signInOTPFragmentVerifyButton.visibility = View.INVISIBLE
                    val credential = PhoneAuthProvider.getCredential(args.otp, typeOTP.toString())
                    signInWithPhoneAuthCredential(credential)
                } else {
                    showToast("Please enter a 6-digit OTP.")
                }
            }
        }

        binding.resendOTPTV.setOnClickListener {
            if (!verificationInProgress) {
                binding.signInOTPFragmentPB.visibility = View.VISIBLE
                binding.signInOTPFragmentVerifyButton.visibility = View.INVISIBLE
                vibrator.vibrate(100)
                resendVerificationCode()
                resendOTPTvVisibility()
            }
        }
        return binding.root
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(args.phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callBacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(args.resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        verificationInProgress = true
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            verificationInProgress = false
            if (task.isSuccessful) {
                binding.signInOTPFragmentPB.visibility = View.INVISIBLE
                binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
                findNextActivity()
            } else {
                binding.signInOTPFragmentPB.visibility = View.INVISIBLE
                binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
                showToast("Sign in failed: ${task.exception?.message}")
            }
        }
    }

    private val callBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            verificationInProgress = false
            showToast("Verification failed: ${e.message}")
            binding.signInOTPFragmentPB.visibility = View.INVISIBLE
            binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            verificationInProgress = false
            binding.signInOTPFragmentPB.visibility = View.INVISIBLE
            binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
            resendToken = token
            showToast("Verification code resent.")
        }
    }

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
                            // User data exists, proceed to twoStepVerification activity
                            binding.signInOTPFragmentPB.visibility = View.INVISIBLE
                            binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
                            sendToTwoStepVerification()
                            return@addOnCompleteListener
                        }
                        // User data exists, proceed to main activity
                        binding.signInOTPFragmentPB.visibility = View.INVISIBLE
                        binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
                        showToast("Authentication successful")
                        sendToHome()
                    } else {
                        // User data doesn't exist, proceed to sign-up activity
                        sendToSignUp()
                        return@addOnCompleteListener
                    }
                } else {
                    showToast("Error fetching user data")
                    Log.d("TAG", "Task is unsuccessful: ${task.exception.toString()}")
                }
            }
        } else {
            showToast("Internal error!")
            Log.d("TAG", "UserUid is null")
        }
    }

    private fun sendToSignUp() {
        val direction =
            SignInOtpFragmentDirections.actionSignInOtpFragmentToSignUpFragment(args.phoneNumber)
        findNavController().navigate(direction)
    }

    private fun sendToHome() {
        findNavController().navigate(R.id.action_signInOtpFragment_to_homeFragment)
    }

    private fun sendToTwoStepVerification() {
        showToast("Go to two step verification Fragment")
    }

    @SuppressLint("ResourceAsColor")
    private fun resendOTPTvVisibility() {
        binding.resendOTPTV.isEnabled = false
        binding.resendOTPTV.setTextColor(Color.RED)
        binding.resendOTPTV.setText("Waiting for 1 minute")
        binding.resendOTPTV.isEnabled = false
        Handler(Looper.myLooper()!!).postDelayed({
            binding.resendOTPTV.setTextColor(Color.WHITE)
            binding.resendOTPTV.isEnabled = true
            binding.resendOTPTV.setText(R.string.resend_otp)
            binding.resendOTPTV.isEnabled = true
        }, 60000)
    }
}

