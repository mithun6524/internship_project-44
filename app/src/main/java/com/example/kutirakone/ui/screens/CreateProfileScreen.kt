package com.example.kutirakone.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kutirakone.repository.ScrapRepository
import com.example.kutirakone.viewmodel.MainViewModel

@Composable
fun CreateProfileScreen(navController: NavController, viewModel: MainViewModel) {

    val repo = remember { ScrapRepository() }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = name, 
            onValueChange = { name = it }, 
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone, 
            onValueChange = { phone = it }, 
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                repo.saveProfile(name, phone) { success ->
                    if (success) {
                        navController.navigate("home")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}
