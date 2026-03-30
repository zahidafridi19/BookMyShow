package com.example.bookmyshow

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignInScreen(
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit,
    onGoToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB71C1C),
            Color(0xFFD32F2F),
            Color(0xFFFF5252)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "🎬 Book My Show",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Login to continue",
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Please enter all fields"
                            return@Button
                        }

                        isLoading = true

                        auth.signInWithEmailAndPassword(
                            email.trim(),
                            password.trim()
                        ).addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onLoginSuccess() //  NAVIGATION handled in MainActivity
                            } else {
                                errorMessage =
                                    task.exception?.message ?: "Login failed"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Text("Login")
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        onGoToSignUp() //  navigate to signup
                    }
                ) {
                    Text("New user? Register here")
                }
            }
        }
    }
}