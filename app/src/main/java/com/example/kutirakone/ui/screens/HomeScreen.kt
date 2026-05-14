package com.example.kutirakone.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.ui.theme.BackgroundSoft
import com.example.kutirakone.utils.Constants
import com.example.kutirakone.utils.LocationHelper
import com.example.kutirakone.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var radius by remember { mutableDoubleStateOf(viewModel.radiusLimit) }
    var scrapToDelete by remember { mutableStateOf<FabricScrap?>(null) }

    scrapToDelete?.let { selectedScrap ->
        AlertDialog(
            onDismissRequest = { scrapToDelete = null },
            title = { Text("Delete scrap?") },
            text = { Text("This will remove your uploaded scrap from the app.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val scrap = selectedScrap
                        scrapToDelete = null
                        viewModel.deleteScrap(scrap.id, scrap.photoUrl) { success, msg ->
                            Toast.makeText(
                                context,
                                if (success) "Scrap deleted" else msg ?: "Delete failed",
                                if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFE74C3C))
                }
            },
            dismissButton = {
                TextButton(onClick = { scrapToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadScraps()
        locationHelper.getLastLocation { location ->
            location?.takeIf { isUsableLocation(it.latitude, it.longitude) }?.let {
                viewModel.applyRadius(it.latitude, it.longitude, radius)
            }
        }
    }

    LaunchedEffect(radius, viewModel.selectedMaterial) {
        delay(300)
        locationHelper.getLastLocation { location ->
            val usableLocation = location?.takeIf { isUsableLocation(it.latitude, it.longitude) }
            if (usableLocation != null) {
                viewModel.applyRadius(usableLocation.latitude, usableLocation.longitude, radius)
            } else {
                viewModel.applyRadius(viewModel.userLat, viewModel.userLng, radius)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HomeSearchFilterCard(
                radius = radius,
                onRadiusChange = { radius = it },
                navController = navController,
                viewModel = viewModel
            )
        }

        if (viewModel.filteredScraps.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No scraps found matching criteria", color = Color.Gray)
                }
            }
        } else {
            items(viewModel.filteredScraps) { scrap ->
                ScrapGridCard(
                    scrap = scrap,
                    isOwnScrap = scrap.ownerUid == viewModel.currentUser.value?.uid,
                    onClick = {
                        navController.navigate(scrapDetailRoute(scrap))
                    }
                ) {
                    scrapToDelete = scrap
                }
            }
        }
    }
}

@Composable
private fun HomeSearchFilterCard(
    radius: Double,
    onRadiusChange: (Double) -> Unit,
    navController: NavController,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                placeholder = { Text("Search material, color, location, price") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Radius",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${radius.toInt()} km",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nearby only",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (viewModel.showNearbyOnly) {
                            "Showing scraps within ${radius.toInt()} km"
                        } else {
                            "Showing all uploaded scraps"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = viewModel.showNearbyOnly,
                    onCheckedChange = viewModel::updateShowNearbyOnly
                )
            }

            Slider(
                value = radius.toFloat(),
                onValueChange = { onRadiusChange(it.toDouble()) },
                valueRange = 1f..100f,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Filter by Material",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val materialOptions = listOf("All") + Constants.FABRIC_MATERIALS
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                materialOptions.forEach { mat ->
                    FilterChip(
                        selected = viewModel.selectedMaterial == mat,
                        onClick = { viewModel.updateSelectedMaterial(mat) },
                        label = { Text(mat) }
                    )
                }
            }

            Button(
                onClick = { navController.navigate("map") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Open Map View")
            }
        }
    }
}

@Composable
private fun ScrapGridCard(
    scrap: FabricScrap,
    isOwnScrap: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(scrap.photoUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                if (isOwnScrap) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = Color.White.copy(alpha = 0.9f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete scrap",
                                tint = Color(0xFFE74C3C),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = scrap.material,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                if (scrap.color.isNotBlank()) {
                    Text(
                        text = scrap.color,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "${scrap.sizeMetres} m",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rs.${scrap.price}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun isUsableLocation(lat: Double, lng: Double): Boolean {
    return (lat in -90.0..90.0) && (lng in -180.0..180.0) && (lat != 0.0 || lng != 0.0)
}

private fun scrapDetailRoute(scrap: FabricScrap): String {
    return "detail/${Uri.encode(scrap.material)}/${scrap.sizeMetres}/${Uri.encode(scrap.id)}/${Uri.encode(scrap.color)}"
}
