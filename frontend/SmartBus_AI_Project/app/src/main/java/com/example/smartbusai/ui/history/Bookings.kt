package com.example.smartbusai.ui.bookings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smartbusai.util.BookingHistoryItem
import com.example.smartbusai.viewmodels.BookingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// --- Enhanced Theme Colors ---
private val NavyBlue = Color(0xFF0B1D39)
private val LightBlueAcc = Color(0xFFE3F2FD)
private val GoldenYellow = Color(0xFFFFC107)
private val SuccessGreen = Color(0xFF4CAF50)
private val MutedText = Color(0xFF757575)
private val CardBg = Color.White
private val BackgroundColor = Color(0xFFF4F6F9)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    bookingViewModel: BookingViewModel
) {
    val bookings by bookingViewModel.bookings.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()

    // Group bookings by Date Header (Today, Yesterday, etc.)
    val groupedBookings = remember(bookings) {
        bookings.groupBy { getRelativeDateLabel(it.createdAt) }
    }

    LaunchedEffect(Unit) {
        bookingViewModel.fetchBookings()
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Trip History", fontWeight = FontWeight.Bold, color = NavyBlue) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NavyBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { bookingViewModel.fetchBookings() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = NavyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NavyBlue
                )
            } else if (bookings.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedBookings.forEach { (header, items) ->
                        stickyHeader {
                            Surface(
                                color = BackgroundColor,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MutedText,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(items) { booking ->
                            BookingCard(booking = booking) {
                                bookingViewModel.selectBooking(booking)
                                navController.navigate("booking_details")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: BookingHistoryItem, onClick: () -> Unit) {
    // Occupancy Calculation
    val totalSeats = booking.vehicle.rows * booking.vehicle.cols
    val occupied = booking.assignments.size
    val occupancyRate = if (totalSeats > 0) occupied.toFloat() / totalSeats else 0f

    // Animation for progress bar
    val animatedProgress by animateFloatAsState(targetValue = occupancyRate, label = "occupancy")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Row 1: Icon + ID + Vehicle Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = LightBlueAcc,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsBus, null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.vehicle.vehicleType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Trip #${booking.tripId.takeLast(6).uppercase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }

                // Status Chip
                Surface(
                    color = if(occupancyRate > 0.9f) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if(occupancyRate > 0.9f) "FULL" else "OPEN",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if(occupancyRate > 0.9f) Color.Red else SuccessGreen
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Row 2: Occupancy Bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if(occupancyRate > 0.8f) GoldenYellow else SuccessGreen,
                    trackColor = Color(0xFFF0F0F0),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$occupied/$totalSeats",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            // Row 3: Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MutedText, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatTime(booking.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    navController: NavController,
    bookingViewModel: BookingViewModel
) {
    val booking = bookingViewModel.selectedBooking.value

    if (booking == null) {
        LaunchedEffect(Unit) { navController.navigateUp() }
        return
    }

    val totalSeats = booking.vehicle.rows * booking.vehicle.cols
    val priorityCount = booking.assignments.count { it.seatType == "Priority" }
    val groupCount = booking.assignments.count { !it.groupId.isNullOrEmpty() && it.groupId != "GRP-001" }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trip Details", fontWeight = FontWeight.Bold, color = NavyBlue)
                        Text(
                            "ID: ${booking.tripId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NavyBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Stats Summary Row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Total Pax",
                    value = "${booking.assignments.size}",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Priority",
                    value = "$priorityCount",
                    icon = Icons.Default.Accessible,
                    modifier = Modifier.weight(1f),
                    color = GoldenYellow
                )
                StatCard(
                    label = "Groups",
                    value = "$groupCount",
                    icon = Icons.Default.Link,
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. Visual Layout
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Seat Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyBlue)
                    Spacer(Modifier.height(8.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))

                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        ReadOnlySeatGrid(booking)
                    }
                }
            }

            // 3. Manifest List
            Text("Passenger Manifest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyBlue)

            LazyColumn(
                modifier = Modifier.weight(0.45f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(booking.assignments) { assignment ->
                    PassengerManifestItem(assignment)
                }
            }
        }
    }
}

@Composable
fun PassengerManifestItem(assignment: com.example.smartbusai.util.SeatAssignment) {
    val isPriority = assignment.seatType == "Priority"

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seat Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isPriority) GoldenYellow else NavyBlue,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        assignment.seatLabel,
                        color = if (isPriority) NavyBlue else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Passenger ${assignment.passengerId.take(5).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NavyBlue
                )

                if (!assignment.explanation.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "AI: ${assignment.explanation.replace("\n", " ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier, color: Color = NavyBlue) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedText)
        }
    }
}

@Composable
fun ReadOnlySeatGrid(booking: BookingHistoryItem) {
    val rows = booking.vehicle.rows
    val cols = booking.vehicle.cols
    val visualCols = cols + 1
    val aisleIndex = cols / 2

    // Color Logic Helper
    val assignments = booking.assignments
    fun getAssignment(label: String) = assignments.find { it.seatLabel == label }

    LazyVerticalGrid(
        columns = GridCells.Fixed(visualCols),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(rows * visualCols) { index ->
            val visualCol = index % visualCols
            val row = index / visualCols

            if (visualCol == aisleIndex) {
                Spacer(Modifier.aspectRatio(1f))
            } else {
                val logicalCol = if (visualCol < aisleIndex) visualCol else visualCol - 1
                val label = "${row + 1}${('A' + logicalCol)}"
                val assignment = getAssignment(label)
                val isOccupied = assignment != null
                val isPriority = assignment?.seatType == "Priority"

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                isPriority -> GoldenYellow
                                isOccupied -> NavyBlue
                                else -> Color(0xFFEEEEEE)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 10.sp,
                        color = if (isOccupied) if(isPriority) NavyBlue else Color.White else MutedText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(100.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.DirectionsBus, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("No trips history found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NavyBlue)
        Text("Allocated trips will appear here.", color = MutedText)
    }
}

// Helper Utilities
fun getRelativeDateLabel(dateString: String): String {
    try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString) ?: return "Unknown"

        val now = Date()
        val diff = now.time - date.time
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days < 1 -> "Today"
            days < 2 -> "Yesterday"
            else -> SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        return "Recent"
    }
}

fun formatTime(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date ?: "")
    } catch (e: Exception) {
        ""
    }
}
