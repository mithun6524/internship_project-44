package com.example.kutirakone.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kutirakone.viewmodel.MainViewModel

@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: MainViewModel,
    material: String,
    size: String,
    scrapId: String,
    uploadedColor: String = ""
) {
    val scrap = viewModel.scraps.find { it.id == scrapId }
    val isOwnScrap = scrap?.ownerUid == viewModel.currentUser.value?.uid
    val isAvailable = scrapId !in viewModel.acceptedSwapScrapIds
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    val detailColor = scrap?.color?.takeIf { it.isNotBlank() } ?: uploadedColor

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete scrap?") },
            text = { Text("This will remove your uploaded scrap from the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scrap?.let {
                            viewModel.deleteScrap(it.id, it.photoUrl) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, "Scrap deleted", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(context, msg ?: "Delete failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.kutirakone.ui.theme.BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (scrap?.photoUrl?.isNotEmpty() == true) {
            AsyncImage(
                model = scrap.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(com.example.kutirakone.ui.theme.SilkColor.copy(alpha = 0.1f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Material: $material", fontSize = 22.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text("Size: $size m", fontSize = 18.sp)
        if (detailColor.isNotBlank()) {
            Text("Color: $detailColor", fontSize = 18.sp)
        }
        scrap?.let {
            Text("Price: Rs.${it.price}", fontSize = 18.sp)
            val locationText = when {
                it.address.isNotBlank() -> it.address
                it.lat != 0.0 || it.lng != 0.0 -> "${it.lat}, ${it.lng}"
                else -> ""
            }
            if (locationText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location: $locationText",
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
            }
            if (it.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Owner Phone: ${it.phone}", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (!isOwnScrap && scrap?.phone?.isNotBlank() == true) {
            OutlinedButton(
                onClick = { openDialer(context, scrap.phone) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Contact Owner")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { openBuyMessage(context, scrap.phone, scrap.material, scrap.sizeMetres, scrap.price, scrap.color) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Buy Now", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isOwnScrap && isAvailable) {
            Button(
                onClick = {
                    scrap?.let {
                        viewModel.sendSwapRequest(it) {
                            navController.navigate("swap")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.kutirakone.ui.theme.SilkColor)
            ) {
                Text("Request Swap", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        } else if (!isOwnScrap) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(
                    "This scrap has already been swapped",
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text("Delete Scrap", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(
                    "This is your uploaded scrap",
                    modifier = Modifier.padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scrap != null && (scrap.lat != 0.0 || scrap.lng != 0.0)) {
            OutlinedButton(
                onClick = { openDirections(context, scrap.lat, scrap.lng) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Navigate")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedButton(
            onClick = { navController.navigate("ideas") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Text("Get Design Ideas")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun openDirections(context: Context, lat: Double, lng: Double) {
    val uri = Uri.parse("google.navigation:q=$lat,$lng")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

fun openDialer(context: Context, phone: String) {
    val uri = Uri.parse("tel:$phone")
    context.startActivity(Intent(Intent.ACTION_DIAL, uri))
}

fun openBuyMessage(
    context: Context,
    phone: String,
    material: String,
    sizeMetres: Double,
    price: Double,
    color: String
) {
    val colorText = if (color.isNotBlank()) ", Color: $color" else ""
    val message = Uri.encode("Hi, I want to buy your $material scrap listed on Kutira Kone. Size: ${sizeMetres}m, Price: Rs.$price$colorText")
    val uri = Uri.parse("smsto:$phone")
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra("sms_body", Uri.decode(message))
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show()
    }
}
