package com.example.linkup.authentication

import ShowToast
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
import com.example.linkup.databinding.FragmentForgetPasswordBinding
import com.example.linkup.utils.SendEmail
import com.example.linkup.utils.observeNetworkStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgetPasswordFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentForgetPasswordBinding

    //Declare nav graph argument
    private val args: ForgetPasswordFragmentArgs by navArgs()

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Firebase database
    private lateinit var fStore: FirebaseFirestore

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Local variable
    private var newPassword: String? = null
    private var newConfirmPassword: String? = null

    private val sendEmail = SendEmail()
    private lateinit var showToast: ShowToast
    private var firstTimeCheck: Boolean = false
    private var internetStatus: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
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


        //fetch new password
        binding.ForgetPasswordFragmentNewPasswordTIET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                newPassword = binding.ForgetPasswordFragmentNewPasswordTIET.text.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        //Fetch new confirm password
        binding.ForgetPasswordFragmentNewConfirmPasswordTIET.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                newConfirmPassword =
                    binding.ForgetPasswordFragmentNewConfirmPasswordTIET.text.toString()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        //Handle action to verify Button
        binding.ForgetPasswordFragmentNextBT.setOnClickListener {
            vibrator.vibrate(100)
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                if (newPassword == newConfirmPassword) {
                    if (newPassword!!.length > 6) {
                        workInProgressStart()
                        updatePassword()
                    } else {
                        showToast.warningToast("Minimum password contain 6 character")
                    }
                } else {
                    showToast.warningToast("Password mismatch")
                }
            }

        }
        return binding.root
    }

    private fun workInProgressStart() {
        binding.ForgetPasswordFragmentNewPasswordTIET.isEnabled = false
        binding.ForgetPasswordFragmentNewConfirmPasswordTIET.isEnabled = false
        binding.ForgetPasswordFragmentNextBT.isEnabled = false
        binding.ForgetPasswordFragmentNextBT.visibility = View.INVISIBLE
        binding.step2VerificationFragmentPB.visibility = View.VISIBLE
    }

    private fun workInProgressEnd() {
        binding.ForgetPasswordFragmentNewPasswordTIET.isEnabled = true
        binding.ForgetPasswordFragmentNewConfirmPasswordTIET.isEnabled = true
        binding.ForgetPasswordFragmentNextBT.isEnabled = true
        binding.ForgetPasswordFragmentNextBT.visibility = View.VISIBLE
        binding.step2VerificationFragmentPB.visibility = View.GONE
    }

    private fun updatePassword() {
        val userUid = args.userUid
        val documentReference = fStore.collection("usersDetails").document(userUid)
        documentReference.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val existingData = documentSnapshot.data
                if (existingData != null) {
                    existingData["userPassword"] = newPassword
                    documentReference.set(existingData)
                        .addOnSuccessListener {
                            sendEmail.sendPasswordResetSuccessEmail(args.email, args.name)
                            workInProgressEnd()
                            Log.d("TAG", "Password updated successfully for user $userUid")
                            showToast.motionSuccessToast("Success", "Password update successfully")
                            sendToTwoStepVerification()
                        }
                        .addOnFailureListener { e ->
                            workInProgressEnd()
                            if (!internetStatus!!) {
                                showToast.motionWarningToast("Warning", "You are currently offline")
                            } else {
                                Log.e(
                                    "TAG",
                                    "Failed to update password for user $userUid: ${e.message}"
                                )
                                showToast.motionErrorToast("Error status", e.message.toString())
                                sendToSignIn()
                            }

                        }
                }
            }
        }.addOnFailureListener { e ->
            workInProgressEnd()
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                Log.e("TAG", "Error fetching user data: ${e.message}")
                showToast.motionErrorToast("Error status", e.message.toString())
                sendToSignIn()
            }
        }
    }

    private fun sendToSignIn() {
        SplashFragment.setLoginStatus(requireContext(), false)
        mAuth.signOut()
        findNavController().navigate(R.id.action_forgetPasswordFragment_to_signInFragment)
    }

    private fun sendToTwoStepVerification() {
        SplashFragment.setLoginStatus(requireContext(), true)
        val direction =
            ForgetPasswordFragmentDirections.actionForgetPasswordFragmentToStepTwoVerificationFragment(
                args.phoneNumber,
                args.userUid
            )
        findNavController().navigate(direction)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val navController = findNavController()
            val currentDestination = navController.currentDestination?.id
            if (currentDestination == R.id.forgetPasswordFragment) {
                // Handle back press action
                findNavController().navigate(R.id.action_forgetPasswordFragment_to_signInFragment)
            } else {
                // Call the super method to allow normal back press behavior
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

}