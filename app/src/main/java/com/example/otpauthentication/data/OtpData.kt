package com.example.otpauthentication.data

data class OtpData(
    val otp: String,
    val createdAt: Long,
    var attemptsLeft: Int
)
