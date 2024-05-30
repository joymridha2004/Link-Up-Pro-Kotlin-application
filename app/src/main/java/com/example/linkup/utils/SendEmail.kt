package com.example.linkup.utils

import android.os.AsyncTask
import java.security.SecureRandom
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SendEmail {
    fun sendEmailOtp(receiverEmail: String, receiverName: String): String {
        val stringSenderEmail = "linkuppro2024@gmail.com"
        val stringPasswordSenderEmail = "dbfnbqktghijajtj"
        val stringReceiverEmail =
            receiverEmail // Assuming you want to send an email to the entered email address
        val stringSubject = "Email Verification - Link Up Pro"

        // Generate random 6-digit code
        var verificationCode = generateRandomCode()

        val stringBody = """
                    Hello $receiverName,
                    
                    Thank you for signing up for Link Up Pro! To complete your registration, please verify your email address by entering the following verification code in the app:
                    
                    $verificationCode
                    
                    If you did not create an account, please ignore this email.
                    
                    Best regards,
                    The Link Up Pro Team
                """.trimIndent()
        SendMail(
            stringSenderEmail,
            stringPasswordSenderEmail,
            stringReceiverEmail,
            stringSubject,
            stringBody
        ).execute()
        return verificationCode
    }

    fun sendWelcomeEmail(receiverEmail: String, receiverName: String) {
        val stringSenderEmail = "linkuppro2024@gmail.com"
        val stringPasswordSenderEmail = "dbfnbqktghijajtj"
        val stringReceiverEmail =
            receiverEmail // Assuming you want to send an email to the entered email address
        val stringSubject = "Welcome to Link Up Pro, $receiverName!"

        val stringBody = """
        Hello $receiverName,

        Welcome to Link Up Pro! We're thrilled to have you join our community. Link Up Pro is your new go-to app for chatting with friends and connecting with people just like you do on social media.

        Here’s how you can make the most out of Link Up Pro:
        
        - **Chat with Friends:** Start private or group chats, share media, and stay connected with your friends and family.
        - **Connect and Share:** Create your profile, post updates, share photos and videos, and connect with new people.
        - **Discover Content:** Explore posts, join groups, and follow topics that interest you.

        We're here to make sure you have the best experience. If you have any questions or need help, our support team is just a message away.

        Get ready to discover, connect, and share with Link Up Pro!

        Best regards,
        The Link Up Pro Team
    """.trimIndent()

        SendMail(
            stringSenderEmail,
            stringPasswordSenderEmail,
            stringReceiverEmail,
            stringSubject,
            stringBody
        ).execute()
    }

    fun sendPasswordResetSuccessEmail(receiverEmail: String, receiverName: String) {
        val stringSenderEmail = "linkuppro2024@gmail.com"
        val stringPasswordSenderEmail = "dbfnbqktghijajtj"
        val stringReceiverEmail =
            receiverEmail // Assuming you want to send an email to the entered email address
        val stringSubject = "Password Reset Successful - Link Up Pro"

        val stringBody = """
        Hello $receiverName,

        Your password for Link Up Pro has been successfully reset. You can now log in to your account using your new password.

        If you didn't request this change, please contact us immediately to secure your account.

        If you have any questions or need further assistance, feel free to reach out to our support team.

        Best regards,
        The Link Up Pro Team
    """.trimIndent()

        SendMail(
            stringSenderEmail,
            stringPasswordSenderEmail,
            stringReceiverEmail,
            stringSubject,
            stringBody
        ).execute()
    }


    private class SendMail(
        private val stringSenderEmail: String,
        private val stringPasswordSenderEmail: String,
        private val stringReceiverEmail: String?,
        private val stringSubject: String,
        private val stringBody: String
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val properties = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "465")
                    put("mail.smtp.ssl.enable", "true")
                    put("mail.smtp.auth", "true")
                }

                val session = Session.getInstance(properties, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(stringSenderEmail, stringPasswordSenderEmail)
                    }
                })

                val mimeMessage = MimeMessage(session).apply {
                    setFrom(InternetAddress(stringSenderEmail))
                    addRecipient(Message.RecipientType.TO, InternetAddress(stringReceiverEmail))
                    subject = stringSubject
                    setText(stringBody)
                }

                Transport.send(mimeMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

    private fun generateRandomCode(): String {
        val random = SecureRandom()
        val code = random.nextInt(900000) + 100000 // Generates a random 6-digit number
        return code.toString()
    }
}