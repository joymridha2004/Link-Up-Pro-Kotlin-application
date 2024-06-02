package com.example.linkup.authentication

import ShowToast
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignUpOtpBinding
import com.example.linkup.utils.SendEmail
import com.example.linkup.utils.observeNetworkStatus
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SignUpOtpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignUpOtpBinding

    //Declare nav graph argument
    private val args: SignUpOtpFragmentArgs by navArgs()

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Local variable
    private var typeCode: String? = null
    private var verificationCode: String? = null
    private var resendOtpProcess = false

    private val sendEmail = SendEmail()
    private lateinit var showToast: ShowToast
    private var internetStatus: Boolean? = null
    private var firstTimeCheck: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpOtpBinding.inflate(inflater, container, false)
        //Fetch and show email form TV
        binding.signUpOTPFragmentEmailTV.text = args.email
        //Fetch otp previous fragment
        verificationCode = args.verificationCode
        //Vibration instance create
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Initialize ShowToast
        showToast = ShowToast(requireContext())
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        //set SharedPreferences
        SplashFragment.setEmailVerificationStatus(requireContext(),false)
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
        binding.signUpOTPFragmentVerifyBT.setOnClickListener {
            vibrator.vibrate(100)
            if (internetStatus == null || !internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                //Fetch otp form user
                typeCode = binding.signUpOTPFragmentOtpET.text.toString()
                if (typeCode!!.length == 6) {
                    if (typeCode == verificationCode) {
                        workInProgressStart()
                        workInProgressEnd()
                        sendToSignUp()
                    } else {
                        showToast.errorToast("Wrong OTP!")
                    }
                } else {
                    showToast.warningToast("Please enter a 6-digit OTP.")
                }
            }
        }


        //Handle action to resend otp button
        binding.resendOTPTV.setOnClickListener {
            vibrator.vibrate(100)
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                if (!resendOtpProcess) {
                    workInProgressStart()
                    verificationCode = sendEmail.sendCreateAccountEmailOtp(args.email, args.name)
                    workInProgressEnd()
                    resendOTPTvVisibility()
                } else {
                    showToast.infoToast("Email already send")
                }
            }
        }

        return binding.root
    }

    //Elements enable and disable function
    private fun workInProgressStart() {
        binding.signUpOTPFragmentOtpET.isEnabled = false
        binding.signUpOTPFragmentVerifyBT.isEnabled = false
        binding.resendOTPTV.isEnabled = false
        binding.signUpOTPFragmentVerifyBT.visibility = View.INVISIBLE
        binding.signUpOTPFragmentPB.visibility = View.VISIBLE
    }

    private fun workInProgressEnd() {
        binding.signUpOTPFragmentOtpET.isEnabled = true
        binding.signUpOTPFragmentVerifyBT.isEnabled = true
        binding.resendOTPTV.isEnabled = true
        binding.signUpOTPFragmentVerifyBT.visibility = View.VISIBLE
        binding.signUpOTPFragmentPB.visibility = View.GONE
    }

    //Resend otp mode ui
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

    private fun sendToSignUp() {
        SplashFragment.setEmailVerificationStatus(requireContext(), true)
        findNavController().popBackStack(R.id.signUpOtpFragment, true)
    }

}