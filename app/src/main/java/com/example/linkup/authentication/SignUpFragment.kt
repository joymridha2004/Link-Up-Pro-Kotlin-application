package com.example.linkup.authentication

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.text.Editable
import android.text.TextShaper
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSignUpBinding
import com.example.linkup.utility.SendEmail
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SignUpFragment : Fragment() {
    //Binding to xml layout
    private lateinit var binding: FragmentSignUpBinding

    //Declare nav graph argument
    private val args: SignUpFragmentArgs by navArgs()

    //Firebase authentication
    private lateinit var mAuth: FirebaseAuth

    //Firebase database
    private lateinit var fStore: FirebaseFirestore

    //Vibration component
    private lateinit var vibrator: Vibrator

    //Layout functionality variable
    private var allDetailsAreOk: Boolean = false
    private var nameIsEmpty: Boolean = true
    private var emailIsEmpty: Boolean = true
    private var dobIsEmpty: Boolean = true

    private var name: String? = null
    private var email: String? = null
    private var dob: String? = null
    private var userUid: String? = null
    private var verificationCode: String? = null

    private var rightIcon: Drawable? = null

    private var redColor: Int = 0
    private var blackColor: Int = 0
    private val sendEmail = SendEmail()
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
                if (email.toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex())) {
                    binding.signUpFragmentEmailET.setCompoundDrawables(null, null, rightIcon, null)
                    emailIsEmpty = false
                } else {
                    emailIsEmpty = true
                    binding.signUpFragmentEmailET.setCompoundDrawables(null, null, null, null)
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

        // Handle action on sign up button
        binding.signUpFragmentNextBT.setOnClickListener {
            vibrator.vibrate(100)
            checkAllDetails()
            findWhereIsEmpty()
            allDetailsUpdated()
            if (allDetailsAreOk) {
                workInProgressStart()
                verificationCode = sendEmail.sendEmailOtp(email.toString(), name.toString())
                workInProgressEnd()
                sendToSignUpOtp()
            }
        }

        binding.signUpFragmentSignInTV.setOnClickListener {
            vibrator.vibrate(100)
            workInProgressStart()
            sendToSignIn()
            workInProgressEnd()
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

    //Show text from Toast function
    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
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
                    showToast("Under 18 not allowed!")
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
        if (!allDetailsAreOk) {
            showToast("Fill up all details!")
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

    private fun sendToSignUpOtp() {
        SplashFragment.setLoginStatus(requireContext(), false)
        val direction =
            SignUpFragmentDirections.actionSignUpFragmentToSignUpOtpFragment(
                args.phoneNumber,
                name.toString(),
                email.toString(),
                dob.toString(),
                userUid.toString(),
                verificationCode.toString()
            )
        findNavController().navigate(direction)
    }

}
