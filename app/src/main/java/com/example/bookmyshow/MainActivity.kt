package com.example.bookmyshow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bookmyshow.ui.theme.BookMyShowTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            BookMyShowTheme {
                SplashScreen(
                    onSplashFinished = {
                        val currentUser = auth.currentUser

                        if (currentUser != null) {
                            startActivity(Intent(this, HomeActivity::class.java))
                        } else {
                            startActivity(Intent(this, SignInActivity::class.java))
                        }
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2500) // splash duration
        onSplashFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB71C1C)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Book My Show",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Movie Ticket Booking App",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }

        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = Color.White,
            strokeWidth = 4.dp
        )
    }
}