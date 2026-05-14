package com.example.kutirakone.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.kutirakone.ui.theme.BackgroundSoft
import com.example.kutirakone.ui.theme.SilkColor
import com.example.kutirakone.viewmodel.MainViewModel
import com.example.kutirakone.utils.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UploadScreen(navController: NavController, viewModel: MainViewModel) {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var size by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("Cotton") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val commonColors = remember {
        listOf("White", "Black", "Red", "Blue", "Green", "Yellow", "Pink", "Purple", "Brown", "Multicolor")
    }

    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var locating by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> 
        if (uri != null) {
            imageUri = uri
            capturedBitmap = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            imageUri = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentUploadLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onLocatingChange = { locating = it },
                onLocationFound = { lat, lng, resolvedAddress ->
                    latitude = lat
                    longitude = lng
                    address = resolvedAddress
                },
                onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSoft)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (capturedBitmap != null) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("No image selected", color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Camera")
            }
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Gallery")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = size,
            onValueChange = { input -> 
                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                size = filtered 
            },
            label = { Text("Size (Numbers Only)") },
            placeholder = { Text("e.g. 2.5") },
            suffix = { Text("metres") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { input ->
                price = input.filter { it.isDigit() || it == '.' || it == ',' }
            },
            label = { Text("Price") },
            placeholder = { Text("e.g. 150") },
            prefix = { Text("₹") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Scrap Color") },
            placeholder = { Text("e.g. Red, Blue, Multicolor") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = SilkColor) },
            singleLine = true
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            commonColors.forEach { item ->
                FilterChip(
                    selected = color.equals(item, ignoreCase = true),
                    onClick = { color = item },
                    label = { Text(item) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Select Material", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Constants.FABRIC_MATERIALS.forEach { item ->
                FilterChip(
                    selected = material == item,
                    onClick = { material = item },
                    label = { Text(item) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Enter Address") },
            placeholder = { Text("Area, City, etc.") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = SilkColor) }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            placeholder = { Text("Enter contact number") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SilkColor) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            )
        )

        Spacer(Modifier.height(16.dp))

        // Location Section
        Button(
            onClick = {
                val hasLocationPermission =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (!hasLocationPermission) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    fetchCurrentUploadLocation(
                        context = context,
                        fusedLocationClient = fusedLocationClient,
                        onLocatingChange = { locating = it },
                        onLocationFound = { lat, lng, resolvedAddress ->
                            latitude = lat
                            longitude = lng
                            address = resolvedAddress
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            enabled = !locating,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.2f), contentColor = Color.DarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    locating -> "Finding device location..."
                    address.isEmpty() -> "Use Current Location"
                    else -> address
                },
                maxLines = 1
            )
        }

        Spacer(Modifier.height(32.dp))

        if (error != null) {
            Text(error!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                if (imageUri == null && capturedBitmap == null) {
                    error = "Please select or take an image"
                    return@Button
                }
                val sanitizedSize = size.replace(',', '.')
                val sizeVal = sanitizedSize.toDoubleOrNull()
                if (sizeVal == null) {
                    error = "Please enter only numbers (e.g. 2.5)"
                    return@Button
                }
                val priceVal = price.replace(',', '.').toDoubleOrNull()
                if (priceVal == null) {
                    error = "Please enter price"
                    return@Button
                }
                if (color.isBlank()) {
                    error = "Please enter scrap color"
                    return@Button
                }
                if (address.isEmpty()) {
                    error = "Please click 'Use Current Location'"
                    return@Button
                }

                if (phone.isEmpty()) {
                    error = "Please enter phone number"
                    return@Button
                }

                loading = true
                error = null

                coroutineScope.launch {
                    val resolvedCoordinates = if (isUsableUploadLocation(latitude, longitude)) {
                        latitude to longitude
                    } else {
                        withContext(Dispatchers.IO) {
                            getCoordinatesFromAddress(context, address)
                        }
                    }

                    if (resolvedCoordinates == null) {
                        loading = false
                        error = "Could not find this address on map. Use current location or enter a clearer area/city."
                        return@launch
                    }

                    viewModel.uploadScrap(
                        imageUri,
                        capturedBitmap,
                        material,
                        sizeVal,
                        priceVal,
                        color.trim(),
                        resolvedCoordinates.first,
                        resolvedCoordinates.second,
                        address,
                        phone
                    ) { success, msg ->
                        loading = false
                        if (success) {
                            viewModel.refreshScrapsAfterUpload()
                            navController.popBackStack()
                        } else {
                            error = msg ?: "Upload failed"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SilkColor),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("UPLOAD SCRAP", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp)) // Extra spacer at bottom to ensure scroll room
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentUploadLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocatingChange: (Boolean) -> Unit,
    onLocationFound: (Double, Double, String) -> Unit,
    onError: (String) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    onLocatingChange(true)
    val tokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
        .addOnSuccessListener { current ->
            val locationTask = if (current != null && isUsableUploadLocation(current.latitude, current.longitude)) {
                null
            } else {
                fusedLocationClient.lastLocation
            }

            if (locationTask == null) {
                onLocatingChange(false)
                onLocationFound(
                    current.latitude,
                    current.longitude,
                    getAddressFromLocation(context, current.latitude, current.longitude)
                )
            } else {
                locationTask
                    .addOnSuccessListener { last ->
                        onLocatingChange(false)
                        if (last != null && isUsableUploadLocation(last.latitude, last.longitude)) {
                            onLocationFound(
                                last.latitude,
                                last.longitude,
                                getAddressFromLocation(context, last.latitude, last.longitude)
                            )
                        } else {
                            val fallback = locationManager.lastKnownUploadLocation()
                            if (fallback != null) {
                                onLocationFound(
                                    fallback.latitude,
                                    fallback.longitude,
                                    getAddressFromLocation(context, fallback.latitude, fallback.longitude)
                                )
                            } else {
                                onError("Could not get device location. Turn on GPS.")
                            }
                        }
                    }
                    .addOnFailureListener {
                        onLocatingChange(false)
                        val fallback = locationManager.lastKnownUploadLocation()
                        if (fallback != null) {
                            onLocationFound(
                                fallback.latitude,
                                fallback.longitude,
                                getAddressFromLocation(context, fallback.latitude, fallback.longitude)
                            )
                        } else {
                            onError("Unable to read device location")
                        }
                    }
            }
        }
        .addOnFailureListener {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { last ->
                    onLocatingChange(false)
                    if (last != null && isUsableUploadLocation(last.latitude, last.longitude)) {
                        onLocationFound(
                            last.latitude,
                            last.longitude,
                            getAddressFromLocation(context, last.latitude, last.longitude)
                        )
                    } else {
                        val fallback = locationManager.lastKnownUploadLocation()
                        if (fallback != null) {
                            onLocationFound(
                                fallback.latitude,
                                fallback.longitude,
                                getAddressFromLocation(context, fallback.latitude, fallback.longitude)
                            )
                        } else {
                            onError("Could not get device location. Turn on GPS.")
                        }
                    }
                }
                .addOnFailureListener {
                    onLocatingChange(false)
                    val fallback = locationManager.lastKnownUploadLocation()
                    if (fallback != null) {
                        onLocationFound(
                            fallback.latitude,
                            fallback.longitude,
                            getAddressFromLocation(context, fallback.latitude, fallback.longitude)
                        )
                    } else {
                        onError("Unable to read device location")
                    }
                }
        }
}

fun getAddressFromLocation(context: Context, lat: Double, lng: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0)
        } else {
            "Unknown Location"
        }
    } catch (e: Exception) {
        "Unknown Location"
    }
}

fun getCoordinatesFromAddress(context: Context, address: String): Pair<Double, Double>? {
    if (address.isBlank()) return null
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val results = geocoder.getFromLocationName(address, 1)
        val bestMatch = results?.firstOrNull() ?: return null
        if (isUsableUploadLocation(bestMatch.latitude, bestMatch.longitude)) {
            bestMatch.latitude to bestMatch.longitude
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun isUsableUploadLocation(lat: Double, lng: Double): Boolean {
    return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
}

@SuppressLint("MissingPermission")
private fun LocationManager.lastKnownUploadLocation(): Location? {
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
        .filter { isUsableUploadLocation(it.latitude, it.longitude) }
        .maxByOrNull { it.time }
}
