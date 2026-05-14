package com.example.kutirakone.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.kutirakone.ui.theme.*
import com.example.kutirakone.viewmodel.MainViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var isResetLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isResetLoading) showForgotPasswordDialog = false
            },
            title = { Text("Reset password") },
            text = {
                Column {
                    Text("Enter your email address and we will send a password reset link.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it.trim() },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SilkColor) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = !isResetLoading
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isResetLoading,
                    onClick = {
                        if (resetEmail.isBlank()) {
                            Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        isResetLoading = true
                        viewModel.sendPasswordResetEmail(
                            resetEmail,
                            onSuccess = {
                                isResetLoading = false
                                showForgotPasswordDialog = false
                                Toast.makeText(context, "Password reset email sent", Toast.LENGTH_LONG).show()
                            },
                            onError = { error ->
                                isResetLoading = false
                                Toast.makeText(context, resetPasswordErrorMessage(error), Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    if (isResetLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SilkColor)
                    } else {
                        Text("Send")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isResetLoading,
                    onClick = { showForgotPasswordDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icon
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = com.example.kutirakone.R.drawable.app_logo_image),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Text(
                text = "Kutira Kone",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp
                ),
                color = Color(0xFF2D3436),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Giving fabrics a second life",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SilkColor) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SilkColor,
                            unfocusedBorderColor = Color.LightGray,
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SilkColor) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SilkColor,
                            unfocusedBorderColor = Color.LightGray,
                        )
                    )

                    TextButton(
                        onClick = {
                            resetEmail = email.trim()
                            showForgotPasswordDialog = true
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Forgot password?", color = SilkColor, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            android.util.Log.d("LoginScreen", "Login clicked for: $email")
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            viewModel.login(email, password, {
                                android.util.Log.d("LoginScreen", "Login SUCCESS")
                                isLoading = false
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }, { error ->
                                android.util.Log.e("LoginScreen", "Login FAILED: $error")
                                isLoading = false
                                Toast.makeText(context, loginErrorMessage(error), Toast.LENGTH_LONG).show()
                            })
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SilkColor),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("LOG IN", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = {
                navController.navigate("signup")
            }) {
                Text(
                    text = "New here? Create an account",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = SilkColor,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

private fun loginErrorMessage(error: String): String {
    val lowerError = error.lowercase()
    return when {
        "blocked all requests" in lowerError ||
            "unusual activity" in lowerError ||
            "too many requests" in lowerError -> {
            "Too many login attempts. Please wait a few minutes, then try again."
        }
        "password is invalid" in lowerError ||
            "invalid credential" in lowerError ||
            "malformed or has expired" in lowerError -> {
            "Invalid email or password."
        }
        "no user record" in lowerError ||
            "user may have been deleted" in lowerError -> {
            "No account found for this email."
        }
        "network error" in lowerError -> {
            "Network error. Check internet and try again."
        }
        else -> "Login failed. Please try again."
    }
}

private fun resetPasswordErrorMessage(error: String): String {
    val lowerError = error.lowercase()
    return when {
        "badly formatted" in lowerError || "invalid email" in lowerError -> "Enter a valid email address."
        "no user record" in lowerError || "user may have been deleted" in lowerError -> "No account found for this email."
        "network error" in lowerError -> "Network error. Check internet and try again."
        "too many requests" in lowerError -> "Too many attempts. Please wait a few minutes."
        else -> "Could not send reset email. Please try again."
    }
}
