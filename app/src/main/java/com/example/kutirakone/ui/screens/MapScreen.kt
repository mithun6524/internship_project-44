package com.example.kutirakone.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.kutirakone.model.FabricScrap
import com.example.kutirakone.utils.Constants
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: NavController,
    scraps: List<FabricScrap>,
    userLat: Double,
    userLng: Double,
    radiusKm: Double,
    currentUserId: String?,
    unavailableScrapIds: Set<String>,
    selectedMaterial: String,
    onMaterialSelected: (String) -> Unit,
    onLocationUpdated: (Double, Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    val indiaDefaultLocation = LatLng(12.9716, 77.5946)
    var mapUserLat by remember(userLat, userLng) {
        mutableStateOf(if (isUsableLocation(userLat, userLng)) userLat else 0.0)
    }
    var mapUserLng by remember(userLat, userLng) {
        mutableStateOf(if (isUsableLocation(userLat, userLng)) userLng else 0.0)
    }
    val hasUserLocation = isUsableLocation(mapUserLat, mapUserLng)
    var showNearbyOnly by remember { mutableStateOf(true) }
    var locationStatus by remember { mutableStateOf("Finding your current location...") }
    val geocodedScrapLocations = remember { mutableStateMapOf<String, LatLng>() }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationStatus = if (hasLocationPermission) {
            "Finding your current location..."
        } else {
            "Location permission was denied"
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationStatus = "Allow location to show your position on the map"
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationStatus = "Finding your current location..."
            val tokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { current ->
                    if (current != null && isUsableLocation(current.latitude, current.longitude)) {
                        mapUserLat = current.latitude
                        mapUserLng = current.longitude
                        locationStatus = "Current location found"
                        onLocationUpdated(current.latitude, current.longitude)
                    } else {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (last != null && isUsableLocation(last.latitude, last.longitude)) {
                                    mapUserLat = last.latitude
                                    mapUserLng = last.longitude
                                    locationStatus = "Using last known location"
                                    onLocationUpdated(last.latitude, last.longitude)
                                } else {
                                    locationStatus = "Turn on device location or set emulator location"
                                }
                            }
                            .addOnFailureListener {
                                val fallback = locationManager.lastKnownDeviceLocation()
                                if (fallback != null) {
                                    mapUserLat = fallback.latitude
                                    mapUserLng = fallback.longitude
                                    locationStatus = "Using device provider location"
                                    onLocationUpdated(fallback.latitude, fallback.longitude)
                                } else {
                                    locationStatus = "Unable to read device location"
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null && isUsableLocation(last.latitude, last.longitude)) {
                            mapUserLat = last.latitude
                            mapUserLng = last.longitude
                            locationStatus = "Using last known location"
                            onLocationUpdated(last.latitude, last.longitude)
                        } else {
                            val fallback = locationManager.lastKnownDeviceLocation()
                            if (fallback != null) {
                                mapUserLat = fallback.latitude
                                mapUserLng = fallback.longitude
                                locationStatus = "Using device provider location"
                                onLocationUpdated(fallback.latitude, fallback.longitude)
                            } else {
                                locationStatus = "Turn on device location or set emulator location"
                            }
                        }
                    }.addOnFailureListener {
                        val fallback = locationManager.lastKnownDeviceLocation()
                        if (fallback != null) {
                            mapUserLat = fallback.latitude
                            mapUserLng = fallback.longitude
                            locationStatus = "Using device provider location"
                            onLocationUpdated(fallback.latitude, fallback.longitude)
                        } else {
                            locationStatus = "Unable to read device location"
                        }
                    }
                }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 4000L)
                .setMinUpdateIntervalMillis(1500L)
                .setWaitForAccurateLocation(false)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    if (isUsableLocation(location.latitude, location.longitude)) {
                        mapUserLat = location.latitude
                        mapUserLng = location.longitude
                        locationStatus = "Current location found"
                        onLocationUpdated(location.latitude, location.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener {
                locationStatus = "Using device GPS provider"
            }

            onDispose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val nativeListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (isUsableLocation(location.latitude, location.longitude)) {
                        mapUserLat = location.latitude
                        mapUserLng = location.longitude
                        locationStatus = "Live device location found"
                        onLocationUpdated(location.latitude, location.longitude)
                    }
                }

                @Deprecated("Deprecated in Android API")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }

            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            providers.forEach { provider ->
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.requestLocationUpdates(
                            provider,
                            2000L,
                            1f,
                            nativeListener,
                            Looper.getMainLooper()
                        )
                    }
                } catch (_: IllegalArgumentException) {
                } catch (_: SecurityException) {
                }
            }

            locationManager.lastKnownDeviceLocation()?.let { fallback ->
                mapUserLat = fallback.latitude
                mapUserLng = fallback.longitude
                locationStatus = "Using device provider location"
                onLocationUpdated(fallback.latitude, fallback.longitude)
            }

            onDispose {
                locationManager.removeUpdates(nativeListener)
            }
        }
    }

    val userLocation = LatLng(mapUserLat, mapUserLng)
    val cameraPositionState = rememberCameraPositionState {
        val initialLoc = if (hasUserLocation) userLocation else indiaDefaultLocation
        position = CameraPosition.fromLatLngZoom(initialLoc, 12f)
    }

    LaunchedEffect(mapUserLat, mapUserLng) {
        if (hasUserLocation) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation, 14f),
                durationMs = 1000
            )
        }
    }

    LaunchedEffect(scraps) {
        val missingCoordinateScraps = scraps.filter {
            it.id !in geocodedScrapLocations &&
                it.lat == 0.0 &&
                it.lng == 0.0 &&
                it.address.isNotBlank()
        }
        missingCoordinateScraps.forEach { scrap ->
            val resolvedLocation = withContext(Dispatchers.IO) {
                geocodeAddress(context, scrap.address)
            }
            if (resolvedLocation != null) {
                geocodedScrapLocations[scrap.id] = resolvedLocation
            }
        }
    }

    val markerScraps = scraps
        .asSequence()
        .filterNot { it.id in unavailableScrapIds }
        .mapNotNull { scrap ->
            val scrapLocation = when {
                isUsableLocation(scrap.lat, scrap.lng) -> LatLng(scrap.lat, scrap.lng)
                else -> geocodedScrapLocations[scrap.id]
            } ?: return@mapNotNull null
            val dist = if (hasUserLocation) {
                distanceInKm(mapUserLat, mapUserLng, scrapLocation.latitude, scrapLocation.longitude)
            } else {
                null
            }
            MapScrap(scrap, scrapLocation, dist)
        }
        .filter { mapScrap ->
            val matchesRadius = !showNearbyOnly || mapScrap.distance == null || mapScrap.distance <= radiusKm
            val matchesMaterial = selectedMaterial == "All" || mapScrap.scrap.material == selectedMaterial
            matchesRadius && matchesMaterial
        }
        .toList()
        .sortedBy { it.distance ?: Float.MAX_VALUE }

    val nearbyScraps = markerScraps.filter { mapScrap ->
        mapScrap.distance != null && mapScrap.distance <= radiusKm
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = hasLocationPermission)
        ) {
            if (hasUserLocation) {
                Circle(
                    center = userLocation,
                    radius = radiusKm * 1000,
                    fillColor = Color(0xFF1976D2).copy(alpha = 0.08f),
                    strokeColor = Color(0xFF1976D2).copy(alpha = 0.45f),
                    strokeWidth = 2f
                )
                Marker(
                    state = MarkerState(position = userLocation),
                    title = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            markerScraps.forEach { mapScrap ->
                val scrap = mapScrap.scrap
                val distance = mapScrap.distance
                val isOwnScrap = scrap.ownerUid == currentUserId
                Marker(
                    state = MarkerState(position = mapScrap.location),
                    title = if (isOwnScrap) "Your ${scrap.material} scrap" else scrap.material,
                    snippet = "${scrap.sizeMetres}m | Rs.${scrap.price} | ${distance?.let { "%.1f km away".format(it) } ?: scrap.address}",
                    icon = if (isOwnScrap) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    },
                    onClick = {
                        navController.navigate(scrapDetailRoute(scrap))
                        true
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasUserLocation) Icons.Default.MyLocation else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasUserLocation) "Showing uploaded scraps near you" else "Location needed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasUserLocation) {
                                    "${nearbyScraps.size} within ${radiusKm.toInt()} km, ${markerScraps.size} on map"
                                } else {
                                    locationStatus
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = showNearbyOnly,
                            onCheckedChange = { showNearbyOnly = it },
                            enabled = hasUserLocation
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val materialOptions = listOf("All") + Constants.FABRIC_MATERIALS
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        materialOptions.forEach { mat ->
                            FilterChip(
                                selected = selectedMaterial == mat,
                                onClick = { onMaterialSelected(mat) },
                                label = { Text(mat) }
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.White.copy(alpha = 0.96f),
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nearby scraps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (nearbyScraps.isEmpty()) "No available scraps in this radius yet" else "Tap a scrap to view details",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    AssistChip(
                        onClick = {
                            if (hasUserLocation) {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(userLocation, 14f)
                                )
                            }
                        },
                        label = { Text("My spot") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        enabled = hasUserLocation
                    )
                }

                if (nearbyScraps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(nearbyScraps.take(8)) { mapScrap ->
                            NearbyScrapChip(
                                scrap = mapScrap.scrap,
                                distance = mapScrap.distance,
                                onClick = {
                                    navController.navigate(scrapDetailRoute(mapScrap.scrap))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class MapScrap(
    val scrap: FabricScrap,
    val location: LatLng,
    val distance: Float?
)

private fun geocodeAddress(context: Context, address: String): LatLng? {
    if (address.isBlank()) return null
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(address, 1)
        val bestMatch = results?.firstOrNull() ?: return null
        if (isUsableLocation(bestMatch.latitude, bestMatch.longitude)) {
            LatLng(bestMatch.latitude, bestMatch.longitude)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun NearbyScrapChip(
    scrap: FabricScrap,
    distance: Float?,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.width(190.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = scrap.material,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${scrap.sizeMetres} m | Rs.${scrap.price}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color.Gray
                )
                Text(
                    text = distance?.let { "%.1f km away".format(it) } ?: scrap.address.ifBlank { "Location added" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}

private fun distanceInKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Float {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, result)
    return result[0] / 1000
}

private fun isUsableLocation(lat: Double, lng: Double): Boolean {
    return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
}

private fun scrapDetailRoute(scrap: FabricScrap): String {
    return "detail/${Uri.encode(scrap.material)}/${scrap.sizeMetres}/${Uri.encode(scrap.id)}/${Uri.encode(scrap.color)}"
}

@SuppressLint("MissingPermission")
private fun LocationManager.lastKnownDeviceLocation(): Location? {
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .mapNotNull { provider ->
            try {
                if (isProviderEnabled(provider)) getLastKnownLocation(provider) else null
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        .filter { isUsableLocation(it.latitude, it.longitude) }
        .maxByOrNull { it.time }
}
