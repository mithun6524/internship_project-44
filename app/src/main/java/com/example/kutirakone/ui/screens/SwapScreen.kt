package com.example.kutirakone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kutirakone.ui.theme.BackgroundSoft
import com.example.kutirakone.ui.theme.SilkColor
import com.example.kutirakone.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SwapScreen(navController: NavController, viewModel: MainViewModel) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = viewModel.currentUser.value?.uid
    var swaps by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 for Received, 1 for Sent

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("swaps")
                .addSnapshotListener { value, _ ->
                    swaps = value?.documents?.map { 
                        val data = it.data!!.toMutableMap()
                        data["docId"] = it.id
                        data
                    } ?: emptyList()
                }
        }
    }

    val receivedSwaps = swaps.filter { it["toUser"] == currentUserId }
    val sentSwaps = swaps.filter { it["fromUser"] == currentUserId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        Text(
            text = "Swap Inbox",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Received") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sent") }
            )
        }

        val displayList = if (selectedTab == 0) receivedSwaps else sentSwaps

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No requests yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(displayList) { swap ->
                    SwapRequestCard(
                        swap = swap,
                        isReceived = selectedTab == 0,
                        onAccept = { viewModel.acceptSwap(swap["docId"] as String) },
                        onReject = { viewModel.rejectSwap(swap["docId"] as String) }
                    )
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SwapRequestCard(
    swap: Map<String, Any>,
    isReceived: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val status = swap["status"] as String
    val material = swap["scrapMaterial"] as? String ?: "Fabric"
    val photoUrl = swap["scrapPhotoUrl"] as? String ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isReceived) "From: ${swap["fromUser"].toString().take(8)}..." else "To: ${swap["toUser"].toString().take(8)}...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Status: ${status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}",
                    color = when (status) {
                        "pending" -> Color(0xFFF9A825)
                        "accepted" -> Color(0xFF2E7D32)
                        "rejected" -> Color(0xFFC62828)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            if (isReceived && status == "pending") {
                Row {
                    IconButton(onClick = onReject) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color(0xFFC62828))
                    }
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}
