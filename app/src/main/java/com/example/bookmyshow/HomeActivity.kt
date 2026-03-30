package com.example.bookmyshow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth

data class Movie(
    val id: Int,
    val name: String,
    val genre: String,
    val price: Int
)

@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val movies = remember {
        listOf(
            Movie(1, "Leo", "Action / Thriller", 250),
            Movie(2, "Jawan", "Action / Drama", 300),
            Movie(3, "Kalki 2898 AD", "Sci-Fi / Action", 350)
        )
    }

    NavHost(
        navController = navController,
        startDestination = "movie_list"
    ) {
        composable("movie_list") {
            MovieListScreen(
                auth = auth,
                movies = movies,
                onMovieSelected = { movie ->
                    navController.navigate("seat_selection/${movie.id}")
                },
                onLogout = onLogout
            )
        }

        composable(
            route = "seat_selection/{movieId}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 1
            val selectedMovie = movies.firstOrNull { it.id == movieId } ?: movies.first()

            SeatSelectionScreen(
                movie = selectedMovie,
                onBack = { navController.popBackStack() },
                onProceedToPayment = { movie, seats ->
                    navController.navigate("payment/${movie.id}/$seats")
                }
            )
        }

        composable(
            route = "payment/{movieId}/{seatCount}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType },
                navArgument("seatCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: 1
            val seatCount = backStackEntry.arguments?.getInt("seatCount") ?: 1
            val selectedMovie = movies.firstOrNull { it.id == movieId } ?: movies.first()

            PaymentScreen(
                movie = selectedMovie,
                seatCount = seatCount,
                onBack = { navController.popBackStack() },
                onPaymentSuccess = {
                    navController.navigate("success") {
                        popUpTo("movie_list")
                    }
                }
            )
        }

        composable("success") {
            SuccessScreen(
                onGoHome = {
                    navController.navigate("movie_list") {
                        popUpTo("movie_list") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    auth: FirebaseAuth,
    movies: List<Movie>,
    onMovieSelected: (Movie) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Book My Show")
                        Text(
                            text = auth.currentUser?.email ?: "Guest User",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB71C1C),
                    titleContentColor = Color.White
                ),
                actions = {
                    Text(
                        text = "Logout",
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onLogout() }
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Now Showing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieSelected(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3F3)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFB71C1C), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Genre: ${movie.genre}",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price per seat: ₹${movie.price}",
                    color = Color(0xFFB71C1C),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Book",
                color = Color(0xFFB71C1C),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    movie: Movie,
    onBack: () -> Unit,
    onProceedToPayment: (Movie, Int) -> Unit
) {
    var seatCount by remember { mutableIntStateOf(1) }
    val totalPrice = seatCount * movie.price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Seats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB71C1C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = movie.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Genre: ${movie.genre}")
                    Text("Price per seat: ₹${movie.price}")
                }
            }

            Text(
                text = "Choose Number of Seats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (seatCount > 1) seatCount--
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Text("-")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventSeat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$seatCount Seats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (seatCount < 10) seatCount++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Text("+")
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Booking Summary",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Movie: ${movie.name}")
                    Text("Seats: $seatCount")
                    Text("Price per seat: ₹${movie.price}")
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Price: ₹$totalPrice",
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Button(
                onClick = {
                    onProceedToPayment(movie, seatCount)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C)
                )
            ) {
                Text("Proceed to Payment")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    movie: Movie,
    seatCount: Int,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val totalAmount = seatCount * movie.price

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFB71C1C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Payment Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Movie: ${movie.name}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventSeat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seats: $seatCount")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Total Amount: ₹$totalAmount",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Demo Payment",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This is a sample payment screen.")
                    Text("After clicking the button, payment will be marked as successful.")
                }
            }

            Button(
                onClick = {
                    onPaymentSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C)
                )
            ) {
                Text("Pay ₹$totalAmount")
            }
        }
    }
}

@Composable
fun SuccessScreen(
    onGoHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FFF7)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Payment Successful 🎉",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your movie tickets have been booked successfully.",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Text("Back to Home")
                }
            }
        }
    }
}