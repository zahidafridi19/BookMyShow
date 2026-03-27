package com.example.bookmyshow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.bookmyshow.ui.theme.BookMyShowTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class Movie(
    val id: String = "",
    val name: String = "",
    val genre: String = "",
    val price: Int = 0
)

class HomeActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setContent {
            BookMyShowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeNavGraph(
                        db = db,
                        auth = auth,
                        onLogout = {
                            auth.signOut()
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeNavGraph(
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "movie_list"
    ) {
        composable("movie_list") {
            MovieListScreen(
                db = db,
                auth = auth,
                onMovieSelected = { movie ->
                    navController.navigate(
                        "seat_selection/${movie.id}/${movie.name}/${movie.genre}/${movie.price}"
                    )
                },
                onLogout = onLogout
            )
        }

        composable(
            route = "seat_selection/{movieId}/{movieName}/{movieGenre}/{moviePrice}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("movieName") { type = NavType.StringType },
                navArgument("movieGenre") { type = NavType.StringType },
                navArgument("moviePrice") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movieName = backStackEntry.arguments?.getString("movieName") ?: ""
            val movieGenre = backStackEntry.arguments?.getString("movieGenre") ?: ""
            val moviePrice = backStackEntry.arguments?.getInt("moviePrice") ?: 0

            SeatSelectionScreen(
                movie = Movie(
                    id = movieId,
                    name = movieName,
                    genre = movieGenre,
                    price = moviePrice
                ),
                onBack = { navController.popBackStack() },
                onProceedToPayment = { movie, seats ->
                    navController.navigate(
                        "payment/${movie.id}/${movie.name}/${movie.genre}/${movie.price}/$seats"
                    )
                }
            )
        }

        composable(
            route = "payment/{movieId}/{movieName}/{movieGenre}/{moviePrice}/{seatCount}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.StringType },
                navArgument("movieName") { type = NavType.StringType },
                navArgument("movieGenre") { type = NavType.StringType },
                navArgument("moviePrice") { type = NavType.IntType },
                navArgument("seatCount") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            val movieName = backStackEntry.arguments?.getString("movieName") ?: ""
            val movieGenre = backStackEntry.arguments?.getString("movieGenre") ?: ""
            val moviePrice = backStackEntry.arguments?.getInt("moviePrice") ?: 0
            val seatCount = backStackEntry.arguments?.getInt("seatCount") ?: 1

            PaymentScreen(
                db = db,
                auth = auth,
                movie = Movie(
                    id = movieId,
                    name = movieName,
                    genre = movieGenre,
                    price = moviePrice
                ),
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
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    onMovieSelected: (Movie) -> Unit,
    onLogout: () -> Unit
) {
    val movies = remember { mutableStateListOf<Movie>() }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun loadMovies() {
        isLoading = true
        db.collection("movies")
            .get()
            .addOnSuccessListener { result ->
                movies.clear()
                for (doc in result.documents) {
                    val movie = Movie(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        genre = doc.getString("genre") ?: "",
                        price = (doc.getLong("price") ?: 0L).toInt()
                    )
                    movies.add(movie)
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to load movies")
                }
            }
    }

    LaunchedEffect(Unit) {
        loadMovies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Book My Show")
                        Text(
                            text = auth.currentUser?.email ?: "Guest",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        addSampleMovies(db) {
                            loadMovies()
                            scope.launch {
                                snackbarHostState.showSnackbar("Sample movies added")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Sample Movies")
                }

                OutlinedButton(
                    onClick = {
                        deleteAllMovies(db) {
                            loadMovies()
                            scope.launch {
                                snackbarHostState.showSnackbar("All movies deleted")
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Now Showing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (movies.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No movies found. Click 'Add Sample Movies'.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(movie = movie) {
                            onMovieSelected(movie)
                        }
                    }
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
                OutlinedButton(
                    onClick = {
                        if (seatCount > 1) seatCount--
                    }
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

                OutlinedButton(
                    onClick = {
                        if (seatCount < 10) seatCount++
                    }
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
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Price: ₹$totalPrice",
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    movie: Movie,
    seatCount: Int,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val totalAmount = seatCount * movie.price
    var isPaying by remember { mutableStateOf(false) }

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
                    Text("Click the button below to complete payment.")
                }
            }

            Button(
                onClick = {
                    isPaying = true

                    val bookingData = hashMapOf(
                        "movieId" to movie.id,
                        "movieName" to movie.name,
                        "genre" to movie.genre,
                        "seatCount" to seatCount,
                        "pricePerSeat" to movie.price,
                        "totalAmount" to totalAmount,
                        "userEmail" to (auth.currentUser?.email ?: "Guest"),
                        "timestamp" to Timestamp.now(),
                        "paymentStatus" to "Paid"
                    )

                    db.collection("bookings")
                        .add(bookingData)
                        .addOnSuccessListener {
                            isPaying = false
                            onPaymentSuccess()
                        }
                        .addOnFailureListener {
                            isPaying = false
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C)
                ),
                enabled = !isPaying
            ) {
                if (isPaying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Pay ₹$totalAmount")
                }
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

fun addSampleMovies(
    db: FirebaseFirestore,
    onDone: () -> Unit
) {
    val sampleMovies = listOf(
        hashMapOf("name" to "Leo", "genre" to "Action", "price" to 250),
        hashMapOf("name" to "Salaar", "genre" to "Action/Drama", "price" to 300),
        hashMapOf("name" to "Jawan", "genre" to "Thriller", "price" to 280),
        hashMapOf("name" to "Animal", "genre" to "Crime/Drama", "price" to 320),
        hashMapOf("name" to "Kalki 2898 AD", "genre" to "Sci-Fi", "price" to 350),
        hashMapOf("name" to "Pushpa 2", "genre" to "Action", "price" to 290)
    )

    var completed = 0
    for (movie in sampleMovies) {
        db.collection("movies")
            .add(movie)
            .addOnSuccessListener {
                completed++
                if (completed == sampleMovies.size) {
                    onDone()
                }
            }
            .addOnFailureListener {
                completed++
                if (completed == sampleMovies.size) {
                    onDone()
                }
            }
    }
}

fun deleteAllMovies(
    db: FirebaseFirestore,
    onDone: () -> Unit
) {
    db.collection("movies")
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                onDone()
                return@addOnSuccessListener
            }

            var deleted = 0
            for (doc in result.documents) {
                db.collection("movies")
                    .document(doc.id)
                    .delete()
                    .addOnSuccessListener {
                        deleted++
                        if (deleted == result.size()) {
                            onDone()
                        }
                    }
                    .addOnFailureListener {
                        deleted++
                        if (deleted == result.size()) {
                            onDone()
                        }
                    }
            }
        }
        .addOnFailureListener {
            onDone()
        }
}