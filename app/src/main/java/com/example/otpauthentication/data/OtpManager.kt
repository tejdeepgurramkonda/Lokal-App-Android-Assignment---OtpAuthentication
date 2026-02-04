package com.example.otpauthentication.data

class OtpManager {

    private val otpstore: MutableMap<String, OtpData> = mutableMapOf()

    private companion object {
        const val EXPIRY_MS = 60_000L
        const val MAX_ATTEMPTS = 3
    }

    data class OtpStatus(val otp: String, val expiresAt: Long, val attemptsLeft: Int)

    data class OtpOutcome(
        val result: OtpValidationResult,
        val attemptsLeft: Int,
        val expiresAt: Long?
    )

    fun generateOtp(email: String): OtpStatus {
        val otp = (100000..999999).random().toString()
        val otpData = OtpData(
            otp = otp,
            createdAt = System.currentTimeMillis(),
            attemptsLeft = MAX_ATTEMPTS
        )
        otpstore[email] = otpData
        return OtpStatus(otp, otpData.createdAt + EXPIRY_MS, otpData.attemptsLeft)
    }

    fun resendOtp(email: String): OtpStatus = generateOtp(email)

    fun validateOtp(email: String, inputOtp: String): OtpOutcome {
        val otpData = otpstore[email]
            ?: return OtpOutcome(OtpValidationResult.Invalid, 0, null)

        val expiresAt = otpData.createdAt + EXPIRY_MS
        val now = System.currentTimeMillis()

        if (now >= expiresAt) {
            return OtpOutcome(OtpValidationResult.Expired, otpData.attemptsLeft, expiresAt)
        }

        if (otpData.attemptsLeft <= 0) {
            return OtpOutcome(OtpValidationResult.AttemptsExceeded, 0, expiresAt)
        }

        return if (otpData.otp == inputOtp) {
            otpstore.remove(email) // clear on success
            OtpOutcome(OtpValidationResult.Success, otpData.attemptsLeft, expiresAt)
        } else {
            otpData.attemptsLeft--
            val result = if (otpData.attemptsLeft <= 0) {
                OtpValidationResult.AttemptsExceeded
            } else {
                OtpValidationResult.Invalid
            }
            OtpOutcome(result, otpData.attemptsLeft, expiresAt)
        }
    }

    fun currentStatus(email: String): OtpStatus? {
        val otpData = otpstore[email] ?: return null
        return OtpStatus(otpData.otp, otpData.createdAt + EXPIRY_MS, otpData.attemptsLeft)
    }
}