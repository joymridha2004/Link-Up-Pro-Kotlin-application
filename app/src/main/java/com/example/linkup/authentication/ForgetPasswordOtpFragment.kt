package com.example.linkup.authentication

import ShowToast
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentForgetPasswordOtpBinding
import com.example.linkup.utils.SendEmail
import com.example.linkup.utils.observeNetworkStatus
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordOtpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentForgetPasswordOtpBinding

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Declare nav graph argument
    private val args: ForgetPasswordOtpFragmentArgs by navArgs()

    //Local variable
    private var typeCode: String? = null
    private var verificationCode: String? = null
    private var resendOtpProcess = false

    private var sendEmail = SendEmail()
    private lateinit var showToast: ShowToast
    private var internetStatus: Boolean? = null
    private var firstTimeCheck: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordOtpBinding.inflate(inflater, container, false)
        //Fetch and show email form TV
        binding.forgetPasswordOtpFragmentEmailTV.text = args.email
        //Fetch otp previous fragment
        verificationCode = args.verificationCode
        //Vibration instance create
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


        //Handle action to verify button
        binding.forgetPasswordOtpFragmentVerifyButton.setOnClickListener {
            vibrator.vibrate(100)
            if (!internetStatus!!){
                showToast.motionWarningToast("Warning", "You are currently offline")
            }else{
                //Fetch otp form user
                typeCode = binding.forgetPasswordOtpFragmentOtpET.text.toString()
                if (typeCode!!.length == 6) {
                    if (typeCode == verificationCode) {
                        workinProgressStart()
                        showToast.successToast("Verify successful")
                        sendToForgetPassword()
                        workInProgressEnd()
                    } else {
                        showToast.errorToast("Wrong OTP!")
                    }
                } else {
                    showToast.infoToast("Please enter a 6-digit OTP.")
                }
            }


        }

        //Handle to action resend otp
        binding.resendOTPTV.setOnClickListener {
            vibrator.vibrate(100)
            if (!resendOtpProcess) {
                workinProgressStart()
                verificationCode = sendEmail.sendAccountVerifyEmailOtp(args.email, args.name)
                workInProgressEnd()
                resendOTPTvVisibility()
            } else {
                showToast.infoToast("Email already send")
            }
        }

        return binding.root
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

    private fun sendToForgetPassword() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            ForgetPasswordOtpFragmentDirections.actionForgetPasswordOtpFragmentToForgetPasswordFragment(
                args.phoneNumber,
                args.userUid,
                args.email,
                args.name
            )
        findNavController().navigate(direction)
    }

    private fun workInProgressEnd() {
        binding.forgetPasswordOtpFragmentOtpET.isEnabled = true
        binding.forgetPasswordOtpFragmentVerifyButton.isEnabled = true
        binding.forgetPasswordOtpFragmentVerifyButton.visibility = View.VISIBLE
        binding.forgetPasswordOtpFragmentPB.visibility = View.GONE
        binding.resendOTPTV.isEnabled = true
    }

    private fun workinProgressStart() {
        binding.forgetPasswordOtpFragmentOtpET.isEnabled = false
        binding.forgetPasswordOtpFragmentVerifyButton.isEnabled = false
        binding.forgetPasswordOtpFragmentVerifyButton.visibility = View.INVISIBLE
        binding.forgetPasswordOtpFragmentPB.visibility = View.VISIBLE
        binding.resendOTPTV.isEnabled = false
    }

}