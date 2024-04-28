package com.example.linkup.authentication

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.example.linkup.databinding.FragmentSignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private var phoneIsEmpty: Boolean = true
    private var phoneNumber: String? = null
    private lateinit var vibrator: Vibrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding.signInFragmentCCP.registerCarrierNumberEditText(binding.signInFragmentPhoneNoET)

        binding.signInFragmentPhoneNoET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                phoneIsEmpty = s.isNullOrEmpty() || s.length <= 7
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.signInFragmentLoginButton.setOnClickListener {
            phoneNumber = "+" + binding.signInFragmentCCP.fullNumber
            binding.signInFragmentPhoneNoET.clearFocus()
            vibrator.vibrate(100)
            if (!phoneIsEmpty) {
                initiatePhoneNumberVerification()
                binding.signInFragmentLoginButton.visibility = View.INVISIBLE
                binding.signInFragmentPB.visibility = View.VISIBLE
            } else {
                showToast("Please enter a valid number")
            }
        }
        return binding.root
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun initiatePhoneNumberVerification() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber!!) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Use Fragment's Activity
            .setCallbacks(callBacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private var callBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> showToast("Invalid phone number")
                is FirebaseTooManyRequestsException -> showToast("Quota exceeded")
                else -> showToast("Verification failed: ${e.message}")
            }
            binding.signInFragmentLoginButton.visibility = View.VISIBLE
            binding.signInFragmentPB.visibility = View.INVISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            binding.signInFragmentLoginButton.visibility = View.VISIBLE
            binding.signInFragmentPB.visibility = View.INVISIBLE
            val direction = SignInFragmentDirections.actionSignInFragmentToSignInOtpFragment(
                phoneNumber!!,
                verificationId,
                token!!
            )
            findNavController().navigate(direction)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                showToast("Something wrong")
                binding.signInFragmentLoginButton.visibility = View.VISIBLE
                binding.signInFragmentPB.visibility = View.INVISIBLE
            } else {
                showToast("Sign in failed: ${task.exception?.message}")
                binding.signInFragmentLoginButton.visibility = View.VISIBLE
                binding.signInFragmentPB.visibility = View.INVISIBLE
            }
        }
    }
}
