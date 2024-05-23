package com.example.linkup.authentication

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignUpBinding
import com.example.linkup.databinding.FragmentSignUpOtpBinding
import com.example.linkup.utility.SendEmail
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpOtpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignUpOtpBinding

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Declare nav graph argument
    private val args: SignUpOtpFragmentArgs by navArgs()

    //Firebase database
    private lateinit var fStore: FirebaseFirestore

    //Local variable
    private val password: String = "admin@1234"
    private var typeCode: String? = null
    private var verificationCode: String? = null

    private val sendEmail = SendEmail()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignUpOtpBinding.inflate(inflater, container, false)
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        //Fetch and show email form TV
        binding.signUpOTPFragmentEmailTV.text = args.email
        //Fetch otp form user
        typeCode = binding.signUpOTPFragmentOtpET.text.toString()
        //Fetch otp previous fragment
        verificationCode = args.verificationCode

        //Handle action to verify button
        binding.signUpOTPFragmentVerifyBT.setOnClickListener {
            typeCode = binding.signUpOTPFragmentOtpET.text.toString()
            if (typeCode!!.length == 6) {
                if (typeCode == verificationCode) {
                    workInProgressStart()
                    updateUserData()
                } else {
                    showToast("Wrong OTP!")
                }
            } else {
                showToast("Please enter a 6-digit OTP.")
            }
        }

        //Handle action to resend otp button
        binding.resendOTPTV.setOnClickListener {
            workInProgressStart()
            verificationCode = sendEmail.sendEmailOtp(args.email, args.name)
            workInProgressEnd()
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

    //Show text from Toast function
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    //Update user data from firebase
    private fun updateUserData() {
        val resourceId: Int = R.drawable.user_profile_icon
        val uri: Uri = resourceIdToUri(requireActivity(), resourceId)
        val usersDetails: MutableMap<String, Any> = HashMap()
        usersDetails["userName"] = args.name
        if (uri != null) {
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    requireActivity().contentResolver,
                    uri
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (bitmap != null) {
                val avatarBase64: String = convertBitmapToBase64ToUpdate(bitmap)!!
                if (avatarBase64 != null) {
                    usersDetails["userPhoto"] = avatarBase64
                }
            }
        }
        usersDetails["accountCreatedTimeAndDate"] = getTimeAndDate()
        usersDetails["userPhoneNumber"] = args.phoneNumber.toString()
        usersDetails["userEmailId"] = args.email.toString()
        usersDetails["userDOB"] = args.dob.toString()
        usersDetails["userPassword"] = password.toString()
        usersDetails["twoStepVerification"] = false
        fStore.collection("usersDetails").document(args.userUid.toString())
            .set(usersDetails)
            .addOnCompleteListener(OnCompleteListener<Void?> {
                workInProgressEnd()
                showToast("Login Successful")
                sendToHome()
            }).addOnFailureListener(OnFailureListener { e ->
                workInProgressEnd()
                showToast("Internal error!")
                Log.d(TAG, "updateUserData: ${e.message}")
                sendToSignIn()
            })
    }

    //Helping function
    private fun convertBitmapToBase64ToUpdate(bitmap: Bitmap): String? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun resourceIdToUri(context: Context, resourceId: Int): Uri {
        return Uri.parse("android.resource://" + context.packageName + "/" + resourceId)
    }

    private fun getTimeAndDate(): String {
        val currentDate = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        return "$currentDate : $currentTime"
    }

    //Coming fragment
    private fun sendToHome() {
        val direction =
            SignUpOtpFragmentDirections.actionSignUpOtpFragmentToHomeFragment(
                args.phoneNumber,
                args.userUid
            )
        findNavController().navigate(direction)
    }

    private fun sendToSignIn() {
        mAuth.signOut()
        findNavController().navigate(R.id.action_signUpOtpFragment_to_signInFragment)
    }

}