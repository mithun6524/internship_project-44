package com.example.kutirakone.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.kutirakone.ui.theme.GradientEnd
import com.example.kutirakone.ui.theme.GradientStart
import com.example.kutirakone.ui.theme.SilkColor
import com.example.kutirakone.viewmodel.MainViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: MainViewModel) {
    val user = viewModel.currentUser.value
    val profile = viewModel.userProfile.value
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(profile) {
        profile?.let {
            editedName = it.name
            editedPhone = it.phone
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    fun saveProfile() {
        isLoading = true
        if (selectedImageUri != null) {
            viewModel.uploadProfilePhoto(selectedImageUri!!, onSuccess = { photoUrl ->
                viewModel.saveProfile(editedName, editedPhone, photoUrl, onSuccess = {
                    isLoading = false
                    isEditing = false
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }, onError = {
                    isLoading = false
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                })
            }, onError = {
                isLoading = false
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
        } else {
            viewModel.saveProfile(editedName, editedPhone, profile?.photoUrl ?: "", onSuccess = {
                isLoading = false
                isEditing = false
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            }, onError = {
                isLoading = false
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isEditing) {
                    IconButton(onClick = { 
                        isEditing = false
                        profile?.let {
                            editedName = it.name
                            editedPhone = it.phone
                        }
                        selectedImageUri = null
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                    IconButton(onClick = { saveProfile() }, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SilkColor)
                        else Icon(Icons.Default.Save, contentDescription = "Save", tint = SilkColor)
                    }
                } else {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SilkColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // Profile Image
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = SilkColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!profile?.photoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = profile?.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = SilkColor
                            )
                        }
                    }
                }
                
                if (isEditing) {
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SilkColor, CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            } else {
                Text(
                    text = if (profile?.name.isNullOrEmpty()) "User Name" else profile!!.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ProfileItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = profile?.email ?: user?.email ?: "Not available"
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    } else {
                        ProfileItem(
                            icon = Icons.Default.Phone,
                            label = "Phone",
                            value = if (profile?.phone.isNullOrEmpty()) "Not available" else profile!!.phone
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (!isEditing) {
                Button(
                    onClick = {
                        viewModel.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SilkColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D3436))
        }
    }
}
