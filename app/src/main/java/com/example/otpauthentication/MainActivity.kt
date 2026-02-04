package com.example.otpauthentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.otpauthentication.viewmodel.AuthState
import com.example.otpauthentication.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.authState.collectAsState()

            when (state) {
                is AuthState.Login ->
                    LoginScreen { viewModel.sendOtp(it) }

                is AuthState.Otp -> {
                    val s = state as AuthState.Otp
                    OtpScreen(
                        s,
                        onVerify = { viewModel.verifyOtp(s.email, it) },
                        onResend = { viewModel.resendOtp(s.email) }
                    )
                }

                is AuthState.Session -> {
                    val s = state as AuthState.Session
                    SessionScreen(s) { viewModel.logout() }
                }
            }
        }
    }
}


@Composable
fun AppScaffold(
    icon: ImageVector,
    iconBg: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(iconBg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }

        Spacer(Modifier.height(12.dp))
        Text("OTP AUTH DEMO", color = Color.Gray)

        Spacer(Modifier.height(32.dp))


        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = { content() }
            )
        }
    }
}


@Composable
fun LoginScreen(onSendOtp: (String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }

    AppScaffold(
        icon = Icons.Default.Email,
        iconBg = Color(0xFF2962FF)
    ) {
        Text("Login", style = MaterialTheme.typography.headlineSmall)
        Text("Enter your email to receive an OTP", color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        Text("Email address", fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("you@example.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSendOtp(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank()
        ) {
            Text("Send OTP")
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "A 6-digit OTP will be sent and is valid for 60 seconds",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}


@Composable
fun OtpScreen(
    state: AuthState.Otp,
    onVerify: (String) -> Unit,
    onResend: () -> Unit
) {
    var otp by rememberSaveable { mutableStateOf("") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(state.expiresAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val remainingSeconds = ((state.expiresAt - now) / 1000).coerceAtLeast(0)
    val resendLocked = now < state.canResendAt

    AppScaffold(
        icon = Icons.Default.Lock,
        iconBg = Color(0xFF2962FF)
    ) {
        Text("Verify OTP", style = MaterialTheme.typography.headlineSmall)
        Text("Enter the 6-digit code sent to", color = Color.Gray)
        Text(state.email, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it.take(6) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                letterSpacing = 8.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("______", textAlign = TextAlign.Center) }
        )

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Expires in ${remainingSeconds}s")
            Text("Attempts left: ${state.attemptsLeft}")
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onVerify(otp) },
            modifier = Modifier.fillMaxWidth(),
            enabled = otp.length == 6
        ) {
            Text("Verify")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onResend, enabled = !resendLocked) {
            Text(if (resendLocked) "Resend available soon" else "Resend OTP")
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "For demo purposes, use OTP from Logcat",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun SessionScreen(
    state: AuthState.Session,
    onLogout: () -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.sessionStartTime) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val durationSeconds = ((now - state.sessionStartTime) / 1000).coerceAtLeast(0)
    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    val formattedStartTime = remember {
        SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())
            .format(Date(state.sessionStartTime))
    }

    AppScaffold(
        icon = Icons.Default.CheckCircle,
        iconBg = Color(0xFF00C853)
    ) {
        Text("Active Session", style = MaterialTheme.typography.headlineSmall)
        Text("You are logged in as", color = Color.Gray)
        Text(state.email, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(24.dp))

        SessionInfoCard("Session start time", formattedStartTime)
        Spacer(Modifier.height(12.dp))
        SessionInfoCard(
            "Session duration",
            "%02d:%02d".format(minutes, seconds)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text("Logout", color = Color.White)
        }
    }
}

@Composable
fun SessionInfoCard(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F4F6)
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}
