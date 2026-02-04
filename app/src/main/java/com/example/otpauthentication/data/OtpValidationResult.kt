package com.example.otpauthentication.data

sealed class OtpValidationResult {
    object Success : OtpValidationResult()
    object Expired : OtpValidationResult()
    object AttemptsExceeded : OtpValidationResult()
    object Invalid : OtpValidationResult()
}