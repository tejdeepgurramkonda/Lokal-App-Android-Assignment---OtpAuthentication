package com.example.otpauthentication.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otpauthentication.MyApplication
import com.example.otpauthentication.analytics.AnalyticsLogger
import com.example.otpauthentication.data.OtpManager
import com.example.otpauthentication.data.OtpValidationResult
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val otpManager = OtpManager()
    private val analyticsLogger =
        AnalyticsLogger(FirebaseAnalytics.getInstance(MyApplication.appContext))

    private val _authState = MutableStateFlow<AuthState>(AuthState.Login)
    val authState: StateFlow<AuthState> = _authState

    fun sendOtp(email: String) {
        val status = otpManager.generateOtp(email)
        Log.d("OTP_DEBUG", "OTP for $email = ${otpManager.currentStatus(email)}")
        analyticsLogger.logOtpGenerated(email)
        _authState.value = AuthState.Otp(
            email = email,
            expiresAt = status.expiresAt,
            attemptsLeft = status.attemptsLeft,
            canResendAt = status.expiresAt // simple lock until expiry
        )
    }

    fun verifyOtp(email: String, input: String) {
        val outcome = otpManager.validateOtp(email, input)
        when (outcome.result) {
            OtpValidationResult.Success -> {
                analyticsLogger.logOtpSuccess(email)
                _authState.value = AuthState.Session(email, System.currentTimeMillis())
            }
            OtpValidationResult.Expired -> updateOtp("OTP expired", outcome)
            OtpValidationResult.AttemptsExceeded -> updateOtp("Attempts exceeded", outcome)
            OtpValidationResult.Invalid -> updateOtp("Invalid OTP", outcome)
        }
    }

    private fun updateOtp(msg: String, outcome: OtpManager.OtpOutcome) {
        val current = _authState.value as? AuthState.Otp ?: return
        analyticsLogger.logOtpFailure(current.email)
        _authState.value = current.copy(
            attemptsLeft = outcome.attemptsLeft,
            errorMessage = msg,
            expiresAt = outcome.expiresAt ?: current.expiresAt
        )
    }
    fun resendOtp(email: String) {
        viewModelScope.launch {
            val status = otpManager.resendOtp(email)
            analyticsLogger.logOtpGenerated(email)
            _authState.value = AuthState.Otp(
                email = email,
                expiresAt = status.expiresAt,
                attemptsLeft = status.attemptsLeft,
                canResendAt = status.expiresAt,
                errorMessage = null
            )
        }
    }

    fun logout() {
        val email = (_authState.value as? AuthState.Session)?.email ?: return
        analyticsLogger.logLogout(email)
        _authState.value = AuthState.Login
    }
}
