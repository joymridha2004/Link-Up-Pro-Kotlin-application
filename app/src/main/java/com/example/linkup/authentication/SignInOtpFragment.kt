package com.example.linkup.authentication

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.navigation.findNavController
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

        addTextChangeListener()

        binding.signInOTPFragmentVerifyButton.setOnClickListener {
            vibrator.vibrate(100)
            val typeOTP = binding.run {
                signInOTPNumber1.text.toString() +
                        signInOTPNumber2.text.toString() +
                        signInOTPNumber3.text.toString() +
                        signInOTPNumber4.text.toString() +
                        signInOTPNumber5.text.toString() +
                        signInOTPNumber6.text.toString()
            }

            if (!verificationInProgress) {
                if (typeOTP.length == 6) {
                    binding.signInOTPFragmentPB.visibility = View.VISIBLE
                    binding.signInOTPFragmentVerifyButton.visibility = View.INVISIBLE
                    val credential = PhoneAuthProvider.getCredential(args.otp, typeOTP)
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
                findNavController().navigate(R.id.action_signInOtpFragment_to_homeFragment)
            } else {
                binding.signInOTPFragmentPB.visibility = View.INVISIBLE
                binding.signInOTPFragmentVerifyButton.visibility = View.VISIBLE
                showToast("Sign in failed: ${task.exception?.message}")
            }
        }
    }

    private fun addTextChangeListener() {
        val editTextList = listOf(
            binding.signInOTPNumber1,
            binding.signInOTPNumber2,
            binding.signInOTPNumber3,
            binding.signInOTPNumber4,
            binding.signInOTPNumber5,
            binding.signInOTPNumber6
        )

        editTextList.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString()

                    if (text.length == 1 && index < editTextList.lastIndex) {
                        editTextList[index + 1].requestFocus()
                    } else if (text.isEmpty() && index > 0) {
                        editTextList[index - 1].requestFocus()
                    }
                }
            })
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

    private fun resendOTPTvVisibility() {
        binding.signInOTPNumber1.setText("")
        binding.signInOTPNumber2.setText("")
        binding.signInOTPNumber3.setText("")
        binding.signInOTPNumber4.setText("")
        binding.signInOTPNumber5.setText("")
        binding.signInOTPNumber6.setText("")
        binding.resendOTPTV.visibility = View.INVISIBLE
        binding.resendOTPTV.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            binding.resendOTPTV.visibility = View.VISIBLE
            binding.resendOTPTV.isEnabled = true
        }, 60000)
    }
}

