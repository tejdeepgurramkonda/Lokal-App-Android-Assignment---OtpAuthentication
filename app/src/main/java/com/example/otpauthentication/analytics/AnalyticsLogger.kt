package com.example.otpauthentication.analytics

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsLogger(private val analytics: FirebaseAnalytics) {

    fun logOtpGenerated(email: String) {
        analytics.logEvent("otp_generated", bundleOf("email" to email))
    }

    fun logOtpSuccess(email: String) {
        analytics.logEvent("otp_success", bundleOf("email" to email))
    }

    fun logOtpFailure(email: String) {
        analytics.logEvent("otp_failure", bundleOf("email" to email))
    }

    fun logLogout(email: String) {
        analytics.logEvent("logout", bundleOf("email" to email))
    }
}

