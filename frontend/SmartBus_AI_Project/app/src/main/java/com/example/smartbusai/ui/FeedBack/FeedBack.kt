package com.example.smartbusai.ui.FeedBack

import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeedbackSection(
    modifier: Modifier = Modifier,
    onRatingSelected: (Int) -> Unit = {}
) {
    var selectedRating by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How was your experience with us?",
            fontSize = 20.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                Icon(

                    imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Star $star",
                    tint = if (star <= selectedRating) Color(0xFFFFC107) else Color.Gray,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp)
                        .clickable {
                            selectedRating = star
                            onRatingSelected(star)
                        }
                )
            }
        }
    }
}

@Composable
fun FeedbackScreen(modifier: Modifier= Modifier) {
    var userRating by remember { mutableStateOf(0) }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            FeedbackSection(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onRatingSelected = { rating ->
                    userRating = rating
                    // You can log or send `userRating` to backend here
                }
            )
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color(0xFF008800)
                ),
                shape = RectangleShape,
                onClick = { }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("SUBMIT RESPONSE")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }}
}

