package com.example.linkup.authentication

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.example.linkup.R
import com.example.linkup.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        //Night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        workInProgressStart()
        Handler(Looper.myLooper()!!).postDelayed({
            if (getLoginStatus(requireContext())) {
                workInProgressEnd()
                sendToHome()
            } else {
                workInProgressEnd()
                findNavController().navigate(R.id.action_splashFragment_to_signInFragment)
            }
        }, 1000)
        return binding.root
    }

    private fun sendToHome() {
        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
    }

    // UI changes
    private fun workInProgressStart() {
        binding.splashActivityPB.visibility = View.VISIBLE
    }

    private fun workInProgressEnd() {
        binding.splashActivityPB.visibility = View.GONE
    }

    // Function to edit the SharedPreferences value from other activities
    companion object {
        private const val PREFS_NAME = "MyPrefsFile"
        private const val LOGIN_STATUS = "login_status"
        private const val EMAIL_VERIFICATION_STATUS = "EmailVerificationStatus"
        private const val TWO_STEP_VERIFICATION = "TwoStepVerification"

        fun setLoginStatus(context: Context, status: Boolean) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putBoolean(LOGIN_STATUS, status)
                apply()
            }
        }

        fun getLoginStatus(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getBoolean(LOGIN_STATUS, false)
        }

        fun setEmailVerificationStatus(context: Context, emailStatus: Boolean) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putBoolean(EMAIL_VERIFICATION_STATUS, emailStatus)
                apply()
            }
        }

        fun getEmailVerificationStatus(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getBoolean(EMAIL_VERIFICATION_STATUS, false)
        }

        fun setUserName(context: Context, userName: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userName", userName)
                apply()
            }
        }

        fun getUserName(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userName", null)
        }

        fun setUserPhoneNumber(context: Context, userPhoneNumber: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userPhoneNumber", userPhoneNumber)
                apply()
            }
        }

        fun getUserPhoneNumber(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userPhoneNumber", null)
        }

        fun setUserEmailId(context: Context, userEmailId: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userEmailId", userEmailId)
                apply()
            }
        }

        fun getUserEmailId(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userEmailId", null)
        }

        fun setUserPassword(context: Context, userPassword: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userPassword", userPassword)
                apply()
            }
        }

        fun getUserPassword(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userPassword", null)
        }

        fun setUserUid(context: Context, userUid: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userUid", userUid)
                apply()
            }
        }

        fun getUserUid(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userUid", null)
        }

        fun setUserDOB(context: Context, userDOB: String) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("userDOB", userDOB)
                apply()
            }
        }

        fun getUserDOB(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString("userDOB", null)
        }

        fun setTwoStepVerification(context: Context, twoStepVerification: Boolean) {
            val sharedPref =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putBoolean(TWO_STEP_VERIFICATION, twoStepVerification)
                apply()
            }
        }

        fun getTwoStepVerification(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPref.getBoolean(TWO_STEP_VERIFICATION, false)
        }

        //acceptance status in shared preferences:
        fun saveTermsAccepted(context: Context, accepted: Boolean) {
            val sharedPref = context.getSharedPreferences("SignUpPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("termsAccepted", accepted)
                apply()
            }
        }

        fun isTermsAccepted(context: Context): Boolean {
            val sharedPref = context.getSharedPreferences("SignUpPrefs", Context.MODE_PRIVATE)
            return sharedPref.getBoolean("termsAccepted", false)
        }
    }
}
