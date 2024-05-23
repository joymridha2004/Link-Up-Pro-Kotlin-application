package com.example.linkup.utility

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
    fun sendEmailOtp(receiverEmail: String,receiverName: String): String{
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