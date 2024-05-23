package com.example.linkup.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.databinding.FragmentForgetPasswordOtpBinding
import com.example.linkup.utility.SendEmail
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordOtpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentForgetPasswordOtpBinding

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Declare nav graph argument
    private val args: ForgetPasswordOtpFragmentArgs by navArgs()

    //Local variable
    private var typeCode: String? = null
    private var verificationCode: String? = null

    private var sendEmail = SendEmail()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordOtpBinding.inflate(inflater, container, false)
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        //Fetch and show email form TV
        binding.forgetPasswordOtpFragmentEmailTV.text = args.email
        //Fetch otp previous fragment
        verificationCode = args.verificationCode

        //Handle action to verify button
        binding.forgetPasswordOtpFragmentVerifyButton.setOnClickListener {
            //Fetch otp form user
            typeCode = binding.forgetPasswordOtpFragmentOtpET.text.toString()
            if (typeCode!!.length == 6) {
                if (typeCode == verificationCode) {
                    workinProgressStart()
                    showToast("Verify successful")
                    sendToForgetPassword()
                    workInProgressEnd()
                } else {
                    showToast("Wrong password!")
                }
            } else {
                showToast("Please enter a 6-digit OTP.")
            }

        }

        //Handle to action resend otp
        binding.resendOTPTV.setOnClickListener {
            workinProgressStart()
            verificationCode = sendEmail.sendEmailOtp(args.email, args.name)
            workInProgressEnd()
        }

        return binding.root
    }

    private fun sendToForgetPassword() {
        val direction =
            ForgetPasswordOtpFragmentDirections.actionForgetPasswordOtpFragmentToForgetPasswordFragment(
                args.phoneNumber,
                args.userUid
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

    //Show text from Toast function
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}