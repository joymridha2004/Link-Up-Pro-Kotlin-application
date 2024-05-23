package com.example.linkup.authentication

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
import com.example.linkup.databinding.FragmentSignInBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignInFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignInBinding

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Layout functionality variable
    private var phoneIsEmpty: Boolean = true
    private var phoneNumber: String? = null

    //Vibration component
    private lateinit var vibrator: Vibrator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Binding to xml layout
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        //Vibration instance create
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //Country code link to phone number edittext
        binding.signInFragmentCCP.registerCarrierNumberEditText(binding.signInFragmentPhoneNoET)

        //Fetch phone number from edittext
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

        //Handle action to login button
        binding.signInFragmentLoginBT.setOnClickListener {
            vibrator.vibrate(100)
            phoneNumber = "+" + binding.signInFragmentCCP.fullNumber
            binding.signInFragmentPhoneNoET.clearFocus()
            if (!phoneIsEmpty) {
                //Disable all element
                workInProgressStart()
                //Firebase authentication process start
                initiatePhoneNumberVerification()
            } else {
                showToast("Please enter a valid number")
            }
        }
        return binding.root
    }

    //Elements enable and disable function
    private fun workInProgressStart() {
        binding.signInFragmentCCP.isEnabled = false
        binding.signInFragmentPhoneNoET.isEnabled = false
        binding.signInFragmentLoginBT.isEnabled = false
        binding.signInFragmentLoginBT.visibility = View.INVISIBLE
        binding.signInFragmentPB.visibility = View.VISIBLE
    }

    private fun workInProgressEnd() {
        binding.signInFragmentCCP.isEnabled = true
        binding.signInFragmentPhoneNoET.isEnabled = true
        binding.signInFragmentLoginBT.isEnabled = true
        binding.signInFragmentLoginBT.visibility = View.VISIBLE
        binding.signInFragmentPB.visibility = View.INVISIBLE
    }

    //Show text from Toast function
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    //Firebase authentication configuration and code send function
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
                is FirebaseTooManyRequestsException -> showToast("all OTP for this number have use today")
                else -> showToast("Verification failed")
            }
            Log.d(TAG, "onVerificationFailed: ${e.message}")
            workInProgressEnd()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            workInProgressEnd()
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
                workInProgressEnd()
                mAuth.signOut()
            } else {
                showToast("Sign in failed: ${task.exception?.message}")
                workInProgressEnd()
            }
        }
    }

}
