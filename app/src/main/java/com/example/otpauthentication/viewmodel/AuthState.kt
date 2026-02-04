package com.example.otpauthentication.viewmodel

sealed class AuthState {

    // state 1: User is on login screen
    object Login : AuthState()

    // state 2: user is on otp verification screen
    data class Otp(
        val email: String,
        val expiresAt: Long,
        val attemptsLeft: Int,
        val errorMessage: String? = null,
        val canResendAt: Long = expiresAt // controls resend lock
    ): AuthState()

    // state 3: User is logged in and session is active
    data class Session(
        val email: String,
        val sessionStartTime: Long
    ): AuthState()
}