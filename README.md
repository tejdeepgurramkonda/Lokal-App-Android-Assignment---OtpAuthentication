# OTP Authentication Demo

A passwordless email + OTP demo built with Kotlin, Jetpack Compose, ViewModel, and coroutines. Firebase Analytics is integrated to log key auth events. OTP logic is fully local; no backend required.

## Features
- Email capture, local 6-digit OTP generation.
- OTP expiry after 60 seconds; max 3 attempts per email.
- Resend regenerates OTP, resets attempts, invalidates previous code.
- Session screen shows start time and live duration timer; logout ends session.
- Firebase Analytics events: `otp_generated`, `otp_success`, `otp_failure`, `logout`.

## OTP Logic
- OTP data stored per email in a Map (`OtpManager`).
- Each entry holds `otp`, `createdAt`, `attemptsLeft`.
- Expiry: 60,000 ms from `createdAt`. Validation returns `Expired` if now >= expiresAt.
- Attempts: start at 3; decremented on invalid entry; lockout when 0.
- Resend: calls `generateOtp`, replacing the email’s entry and resetting attempts/expiry.

## Data Structures
- `MutableMap<String, OtpData>` keyed by email for OTP state. Fast O(1) lookups and simple replacement on resend.
- `AuthState` sealed class drives UI: `Login`, `Otp(email, expiresAt, attemptsLeft, errorMessage, canResendAt)`, `Session(email, sessionStartTime)`.
- ViewModel exposes `StateFlow<AuthState>` for one-way data flow to Compose.

## SDK Choice: Firebase Analytics
- Already present via BOM; initialized in `MyApplication` using `FirebaseApp.initializeApp`.
- Logged events: `otp_generated`, `otp_success`, `otp_failure`, `logout` with `email` param for demo observability.
- Chosen because Firebase is common in Android apps and lightweight for event logging.

## How State & Timers Survive Recomposition
- `StateFlow` in `AuthViewModel` holds screen state; Compose collects it.
- OTP screen uses `remember` + `LaunchedEffect` to tick `now` every second; countdown derived from `expiresAt` in state.
- Session screen uses the same pattern to show live duration; timers are driven by `LaunchedEffect` so recomposition doesn’t reset them.

## Using the App
1. Enter an email and tap **Send OTP**. Check Logcat (`OTP_DEBUG`) to copy the generated OTP for testing.
2. On the OTP screen, enter the 6-digit code. Countdown shows remaining validity; attempts remaining are displayed.
3. If expired/invalid/exceeded, an error is shown and attempts decrement; resend regenerates a new OTP and resets attempts/expiry.
4. After success, the Session screen shows start time and live duration. Tap **Logout** to return to login.

## Build & Run
```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```
Open in Android Studio and run on an emulator or device. Firebase Analytics uses the bundled `google-services.json`.

## GPT Usage
- GPT assisted in outlining the structure, ViewModel/state shaping, Compose timer patterns, and writing this README.
- Implementation details (data structures, logging points, state wiring) were reviewed and understood before coding.

