package com.example.linkup.authentication

import ShowToast
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignUpBinding
import com.example.linkup.utils.SendEmail
import com.example.linkup.utils.observeNetworkStatus
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SignUpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignUpBinding

    //Declare nav graph argument
    private val args: SignUpFragmentArgs by navArgs()

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Firebase database
    private lateinit var fStore: FirebaseFirestore

    private lateinit var fStorage: FirebaseStorage

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Material
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder

    //Layout functionality variable
    private var allDetailsAreOk: Boolean = false
    private var nameIsEmpty: Boolean = true
    private var emailIsEmpty: Boolean = true
    private var dobIsEmpty: Boolean = true
    private var termsAccepted: Boolean = false

    private var name: String? = null
    private var email: String? = null
    private var dob: String? = null
    private var userUid: String? = null
    private var verificationCode: String? = null
    private val password: String = "admin@1234"

    private var rightIcon: Drawable? = null

    private var redColor: Int = 0
    private var blackColor: Int = 0
    private val sendEmail = SendEmail()
    private var firstTimeCheck: Boolean = false

    private lateinit var showToast: ShowToast
    private var internetStatus: Boolean? = null
    private var emailVerificationStatus: Boolean? = false
    private var verificationSuccessfulEmail: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Binding to xml layout
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //Fetch and show phone number from previous fragment
        binding.signUpFragmentPhoneNumberTV.text = args.phoneNumber
        //Firebase instance create
        mAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        fStorage = FirebaseStorage.getInstance()
        //UserUid get from previous fragment
        userUid = args.userUid
        //Set drawable
        rightIcon = ContextCompat.getDrawable(requireActivity(), R.drawable.right_icon)
        rightIcon!!.setBounds(0, 0, rightIcon!!.intrinsicWidth, rightIcon!!.intrinsicHeight)
        //Vibration instance create
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //Set color
        redColor = ContextCompat.getColor(requireActivity(), R.color.red20)
        blackColor = ContextCompat.getColor(requireActivity(), R.color.black)
        // Initialize ShowToast
        showToast = ShowToast(requireContext())
        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        //Fetch email verification status and email from previous fragment
        emailVerificationStatus = SplashFragment.getEmailVerificationStatus(requireContext())
        if (emailVerificationStatus!!) {
            verificationSuccessfulEmail = email.toString()
            binding.signUpFragmentNextBT.text = "Next"
        }
        //set all details
        binding.signUpFragmentNameET.setText(
            SplashFragment.getUserName(requireContext()).toString()
        )
        binding.signUpFragmentPhoneNumberTV.text =
            SplashFragment.getUserPhoneNumber(requireContext()).toString()
        binding.signUpFragmentEmailET.setText(
            SplashFragment.getUserEmailId(requireContext()).toString()
        )
        binding.signUpFragmentDOBTV.text = SplashFragment.getUserDOB(requireContext()).toString()

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

        // Fetch user name from the EditText
        binding.signUpFragmentNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.signUpFragmentNameET.setHintTextColor(blackColor)
                binding.signUpFragmentNameET.setTextColor(blackColor)
                name = binding.signUpFragmentNameET.text.toString()
                if (!name.equals("")) {
                    nameIsEmpty = false
                    binding.signUpFragmentNameET.setCompoundDrawables(null, null, rightIcon, null)
                } else {
                    nameIsEmpty = true
                    binding.signUpFragmentNameET.setCompoundDrawables(null, null, null, null)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Fetch user email from the EditText
        binding.signUpFragmentEmailET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.signUpFragmentEmailET.setHintTextColor(blackColor)
                binding.signUpFragmentEmailET.setTextColor(blackColor)
                email = binding.signUpFragmentEmailET.text.toString()
                if (email.toString()
                        .matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())
                ) {
                    if (email == verificationSuccessfulEmail && email != "" && verificationSuccessfulEmail != "") {
                        binding.signUpFragmentEmailET.setCompoundDrawables(
                            null,
                            null,
                            rightIcon,
                            null
                        )
                        emailIsEmpty = false
                        emailVerificationStatus = true
                        binding.signUpFragmentNextBT.text = "Next"
                    } else {
                        binding.signUpFragmentEmailET.setCompoundDrawables(
                            null,
                            null,
                            null,
                            null
                        )
                        emailIsEmpty = false
                        emailVerificationStatus = false
                        binding.signUpFragmentNextBT.text = "Verify"
                    }

                } else {
                    emailIsEmpty = true
                    binding.signUpFragmentEmailET.setCompoundDrawables(null, null, null, null)
                    emailVerificationStatus = false
                    binding.signUpFragmentNextBT.text = "Verify"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Fetch user DOB from the TextView
        binding.signUpFragmentDOBTV.setOnClickListener {
            binding.signUpFragmentDOBTV.setHintTextColor(blackColor)
            binding.signUpFragmentDOBTV.setTextColor(blackColor)
            getDOB()
            dob = binding.signUpFragmentDOBTV.text.toString()
        }

        // Check if terms are already accepted
        termsAccepted = SplashFragment.isTermsAccepted(requireContext())
        binding.signUpPageTermsAndConditionsCB.isChecked = termsAccepted

        binding.signUpPageTermsAndConditionsCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !termsAccepted) {
                materialAlertDialogBuilder.setTitle(R.string.terms_and_conditions)
                materialAlertDialogBuilder.setMessage(R.string.terms_and_conditions_content)
                materialAlertDialogBuilder.setPositiveButton("Accept") { dialogInterface, which ->
                    termsAccepted = true
                    SplashFragment.saveTermsAccepted(requireContext(), true)
                    binding.signUpPageTermsAndConditionsCB.isChecked = true
                    dialogInterface.dismiss()
                }
                materialAlertDialogBuilder.setNegativeButton("Decline") { dialogInterface, which ->
                    termsAccepted = false
                    SplashFragment.saveTermsAccepted(requireContext(), false)
                    binding.signUpPageTermsAndConditionsCB.isChecked = false
                    dialogInterface.dismiss()
                }
                materialAlertDialogBuilder.show()
            } else {
                termsAccepted = isChecked
                SplashFragment.saveTermsAccepted(requireContext(), isChecked)
            }
        }


        // Handle action on sign up button
        binding.signUpFragmentNextBT.setOnClickListener {
            vibrator.vibrate(100)
            checkAllDetails()
            findWhereIsEmpty()
            allDetailsUpdated()
            if (!internetStatus!!) {
                showToast.motionWarningToast("Warning", "You are currently offline")
            } else {
                if (allDetailsAreOk && termsAccepted) {
                    if (emailVerificationStatus!!) {
                        workInProgressStart()
                        val resourceId: Int = R.drawable.user_profile_icon
                        val uri: Uri = resourceIdToUri(requireActivity(), resourceId)
                        if (uri != null) {
                            uploadImage(uri, { imageUrl ->
                                updateUserData(imageUrl.toString())
                            }, { e ->
                                workInProgressEnd()
                                showToast.motionErrorToast("Failed to upload image", "${e.message}")
                            })
                        } else {
                            updateUserData("")  // Handle case where URI is null
                        }
                    } else {
                        workInProgressStart()
                        SplashFragment.setUserName(requireContext(), name.toString())
                        SplashFragment.setUserPhoneNumber(requireContext(), args.phoneNumber)
                        SplashFragment.setUserEmailId(requireContext(), email.toString())
                        SplashFragment.setUserPassword(requireContext(), password)
                        SplashFragment.setUserUid(requireContext(), mAuth.uid.toString())
                        SplashFragment.setUserDOB(requireContext(), dob.toString())
                        SplashFragment.setTwoStepVerification(requireContext(), false)
                        SplashFragment.setEmailVerificationStatus(requireContext(), false)
                        verificationCode =
                            sendEmail.sendCreateAccountEmailOtp(email.toString(), name.toString())
                        workInProgressEnd()
                        sendToSignUpOtp()
                    }
                } else if (!termsAccepted && allDetailsAreOk) {
                    showToast.warningToast("You must accept the terms and conditions to proceed!")
                } else {
                    showToast.warningToast("Fill up all details!")
                }
            }
        }

        binding.signUpFragmentSignInTV.setOnClickListener {
            vibrator.vibrate(100)
            workInProgressStart()
            workInProgressEnd()
            sendToSignIn()
        }

        return binding.root
    }

    //Elements enable and disable function
    private fun workInProgressStart() {
        binding.signUpFragmentNameET.isEnabled = false
        binding.signUpFragmentPhoneNumberTV.isEnabled = false
        binding.signUpFragmentEmailET.isEnabled = false
        binding.signUpFragmentDOBTV.isEnabled = false
        binding.signUpFragmentNextBT.isEnabled = false
        binding.signUpFragmentNextBT.visibility = View.INVISIBLE
        binding.signUpActivityPB.visibility = View.VISIBLE
        binding.signUpFragmentSignInTV.isEnabled = false
    }

    private fun workInProgressEnd() {
        binding.signUpFragmentNameET.isEnabled = true
        binding.signUpFragmentPhoneNumberTV.isEnabled = true
        binding.signUpFragmentEmailET.isEnabled = true
        binding.signUpFragmentDOBTV.isEnabled = true
        binding.signUpFragmentNextBT.isEnabled = true
        binding.signUpFragmentNextBT.visibility = View.VISIBLE
        binding.signUpActivityPB.visibility = View.GONE
        binding.signUpFragmentSignInTV.isEnabled = true
    }

    //Get DOB from Edittext
    private fun getDOB() {
        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)
        val currentMonth = c.get(Calendar.MONTH)
        val currentDay = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog =
            DatePickerDialog(requireActivity(), { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                if (currentYear - selectedCalendar.get(Calendar.YEAR) < 18) {
                    val selectedDate = "$dayOfMonth - ${month + 1} - $year"
                    binding.signUpFragmentDOBTV.text = selectedDate
                    dobIsEmpty = true
                    binding.signUpFragmentDOBTV.setCompoundDrawables(null, null, null, null)
                    binding.signUpFragmentDOBTV.setTextColor(redColor)
                    showToast.warningToast("Under 18 not allowed!")
                } else {
                    val selectedDate = "$dayOfMonth - ${month + 1} - $year"
                    binding.signUpFragmentDOBTV.text = selectedDate
                    dobIsEmpty = false
                    binding.signUpFragmentDOBTV.setCompoundDrawables(null, null, rightIcon, null)
                }
            }, currentYear, currentMonth, currentDay)
        datePickerDialog.datePicker.maxDate = c.timeInMillis
        datePickerDialog.show()
    }

    //Update user data from firebase
    private fun updateUserData(imageUrl: String) {
        val usersDetails: MutableMap<String, Any> = HashMap()
        usersDetails["userName"] = name.toString()
        usersDetails["userPhoto"] = imageUrl
        usersDetails["onlineStatus"] = "default"
        usersDetails["accountCreatedTimeAndDate"] = getTimeAndDate()
        usersDetails["userPhoneNumber"] = args.phoneNumber
        usersDetails["userEmailId"] = verificationSuccessfulEmail.toString()
        usersDetails["userDOB"] = dob.toString()
        usersDetails["userPassword"] = password.toString()
        usersDetails["twoStepVerification"] = false

        fStore.collection("usersDetails").document(args.userUid.toString())
            .set(usersDetails)
            .addOnCompleteListener(OnCompleteListener<Void?> {
                sendEmail.sendWelcomeEmail(verificationSuccessfulEmail.toString(), name.toString())
                workInProgressEnd()
                showToast.motionSuccessToast("Success", "Login Successful")
                sendToHome()
            }).addOnFailureListener(OnFailureListener { e ->
                workInProgressEnd()
                if (internetStatus!!) {
                    showToast.motionErrorToast("Error status", "Internal error!")
                    Log.d(ContentValues.TAG, "updateUserData: ${e.message}")
                    sendToSignIn()
                } else {
                    showToast.motionWarningToast("Warning", "You are currently offline")
                }
            })
    }

    //Helping function
    private fun resourceIdToUri(context: Context, resourceId: Int): Uri {
        return Uri.parse("android.resource://" + context.packageName + "/" + resourceId)
    }

    private fun getTimeAndDate(): String {
        val currentDate = SimpleDateFormat("dd - MM - yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        return "$currentDate : $currentTime"
    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = fStorage.reference
        val imageRef = storageRef.child("profilePictures/${mAuth.currentUser?.uid}")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
        }.addOnFailureListener(onFailure)
    }

    //User details are filled properly or not
    private fun checkAllDetails() {
        allDetailsAreOk = !nameIsEmpty && !emailIsEmpty && !dobIsEmpty
    }

    //User details where are missing
    private fun findWhereIsEmpty() {
        if (nameIsEmpty) {
            binding.signUpFragmentNameET.setHintTextColor(redColor)
            binding.signUpFragmentNameET.setTextColor(redColor)
        }
        if (emailIsEmpty) {
            binding.signUpFragmentEmailET.setHintTextColor(redColor)
            binding.signUpFragmentEmailET.setTextColor(redColor)
        }
        if (dobIsEmpty) {
            binding.signUpFragmentDOBTV.setHintTextColor(redColor)
            binding.signUpFragmentDOBTV.setTextColor(redColor)
        }
        if (!termsAccepted) {
            binding.signUpPageTermsAndConditionsCB.isChecked = false
        }
    }

    //User details update in local variable
    private fun allDetailsUpdated() {
        name = binding.signUpFragmentNameET.text.toString()
        email = binding.signUpFragmentEmailET.text.toString()
        dob = binding.signUpFragmentDOBTV.text.toString()
    }

    // Coming fragment
    private fun sendToSignIn() {
        SplashFragment.setLoginStatus(requireContext(), false)
        mAuth.signOut()
        findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
    }

    private fun sendToHome() {
        SplashFragment.setLoginStatus(requireContext(), true)
        findNavController().navigate(R.id.action_signUpFragment_to_homeFragment)
    }


    private fun sendToSignUpOtp() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            SignUpFragmentDirections.actionSignUpFragmentToSignUpOtpFragment(
                name.toString(),
                email.toString(),
                verificationCode.toString()
            )
        findNavController().navigate(direction)
    }

}
