package com.example.smartbusai.ui.bookings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
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
                        Icon(
                            Icons.Default.DirectionsBus,
                            null,
                            tint = NavyBlue,
                            modifier = Modifier.size(20.dp)
                        )
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
                    color = if (occupancyRate > 0.9f) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (occupancyRate > 0.9f) "FULL" else "VACANT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (occupancyRate > 0.9f) Color.Red else SuccessGreen
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Row 2: Occupancy Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (occupancyRate > 0.8f) GoldenYellow else SuccessGreen,
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
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = MutedText,
                    modifier = Modifier.size(14.dp)
                )
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

    val priorityCount = booking.assignments.count { it.seatType == "Priority" }
    // Count unique groups (excluding null/default)
    val groupCount = booking.assignments
        .mapNotNull { it.groupId }
        .filter { it != "GRP-001" }
        .distinct()
        .count()

    // Tab State
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Seat Map", "Passenger List")

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Column(Modifier.background(CardBg)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Trip Details", fontWeight = FontWeight.Bold, color = NavyBlue)
                            Text(
                                "ID: ${booking.tripId.takeLast(8).uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedText
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

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBg,
                    contentColor = NavyBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = NavyBlue
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) },
                            icon = {
                                Icon(
                                    if (index == 0) Icons.Default.Map else Icons.AutoMirrored.Default.List,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Stats Summary (Always Visible)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Passengers",
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

            // 2. Tab Content
            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    // --- MAP TAB ---
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ReadOnlySeatGrid(booking)
                        }
                    }
                } else {
                    // --- LIST TAB ---
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(booking.assignments) { assignment ->
                            PassengerManifestItem(assignment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerManifestItem(assignment: com.example.smartbusai.util.SeatAssignment) {
    val isPriority = assignment.seatType == "Priority"

    // Clean up explanation text
    // Remove "Passenger ID (Age: X) -> Seat Y" prefix using Regex
    val rawExplanation = assignment.explanation ?: ""
    val cleanedExplanation = remember(rawExplanation) {
        // Regex looks for "Passenger ... -> Seat X" and replaces it with empty string
        val prefixRegex = Regex("Passenger.*?-> Seat.*?(\\n|$)")
        var text = rawExplanation.replace(prefixRegex, "").trim()

        // Remove bullet points for cleaner look if needed, or keep them
        text = text.replace("•", "").trim()
        if (text.isEmpty()) "Standard Allocation" else text
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Match height of children
        ) {
            // Left: Seat Number
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .background(
                        color = if (isPriority) GoldenYellow.copy(alpha = 0.2f) else NavyBlue.copy(
                            alpha = 0.1f
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = "SEAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontSize = 8.sp
                )
                Text(
                    text = assignment.seatLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPriority) Color(0xFFF57F17) else NavyBlue
                )
            }

            Spacer(Modifier.width(16.dp))

            // Middle: Details + AI Reason
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // ID / Name
                Text(
                    text = "Passenger ${assignment.passengerId.take(5).uppercase()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )

                Spacer(Modifier.height(6.dp))

                // AI Explanation Chip
                Surface(
                    color = Color(0xFFF1F8E9), // Light Green
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFFC8E6C9))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFF2E7D32)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = cleanedExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = NavyBlue
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                        color = if (isOccupied) if (isPriority) NavyBlue else Color.White else MutedText,
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
                Icon(
                    Icons.Default.DirectionsBus,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "No trips history found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
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
