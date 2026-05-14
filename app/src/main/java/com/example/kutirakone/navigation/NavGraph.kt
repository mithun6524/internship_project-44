package com.example.kutirakone.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.AccountCircle
import com.example.kutirakone.ui.screens.CreateProfileScreen
import com.example.kutirakone.ui.screens.DetailScreen
import com.example.kutirakone.ui.screens.HomeScreen
import com.example.kutirakone.ui.screens.IdeasScreen
import com.example.kutirakone.ui.screens.LoginScreen
import com.example.kutirakone.ui.screens.MapScreen
import com.example.kutirakone.ui.screens.ProfileScreen
import com.example.kutirakone.ui.screens.SignUpScreen
import com.example.kutirakone.ui.screens.SwapScreen
import com.example.kutirakone.ui.screens.UploadScreen
import com.example.kutirakone.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KutiraKoneApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAuthScreen = currentDestination?.route == "login" || 
                       currentDestination?.route == "signup" || 
                       currentDestination?.route == "profile"

    LaunchedEffect(viewModel.currentUser.value) {
        if (viewModel.currentUser.value != null && (currentDestination?.route == "login" || currentDestination?.route == null)) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isAuthScreen) {
                val showBackButton = currentDestination?.route != "home"
                TopAppBar(
                    title = { 
                        Text(when(currentDestination?.route) {
                            "upload" -> "Upload Scrap"
                            "detail" -> "Scrap Details"
                            "swap" -> "Swap Request"
                            "ideas" -> "Design Ideas"
                            "map" -> "Nearby Map"
                            "user_profile" -> "My Profile"
                            else -> "Kutira Kone"
                        })
                    },
                    navigationIcon = {
                        if (showBackButton) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (currentDestination?.route != "user_profile") {
                            IconButton(onClick = { navController.navigate("user_profile") }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = com.example.kutirakone.ui.theme.SilkColor
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isAuthScreen) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", "Home", Icons.Default.Home),
                        Triple("upload", "Upload", Icons.Default.AddCircle),
                        Triple("swap", "Swap", Icons.Default.SwapHoriz),
                        Triple("ideas", "Ideas", Icons.Default.Lightbulb)
                    )
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = {
                                if (route == "swap" && viewModel.pendingSwapsCount > 0) {
                                    BadgedBox(
                                        badge = { Badge { Text(viewModel.pendingSwapsCount.toString()) } }
                                    ) {
                                        Icon(icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = label)
                                }
                            },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(navController, viewModel)
            }
            composable("signup") {
                SignUpScreen(navController)
            }
            composable("profile") {
                CreateProfileScreen(navController, viewModel)
            }
            composable("home") {
                HomeScreen(navController, viewModel)
            }
            composable("map") {
                MapScreen(
                    navController = navController,
                    scraps = viewModel.scraps,
                    userLat = viewModel.userLat,
                    userLng = viewModel.userLng,
                    radiusKm = viewModel.radiusLimit,
                    currentUserId = viewModel.currentUser.value?.uid,
                    unavailableScrapIds = viewModel.acceptedSwapScrapIds,
                    selectedMaterial = viewModel.selectedMaterial,
                    onMaterialSelected = viewModel::updateSelectedMaterial,
                    onLocationUpdated = { lat, lng ->
                        viewModel.applyRadius(lat, lng, viewModel.radiusLimit)
                    }
                )
            }
            composable("upload") {
                UploadScreen(navController, viewModel)
            }
            composable("detail/{material}/{size}/{id}/{color}") { backStackEntry ->
                val material = backStackEntry.arguments?.getString("material") ?: ""
                val size = backStackEntry.arguments?.getString("size") ?: ""
                val id = backStackEntry.arguments?.getString("id") ?: ""
                val color = backStackEntry.arguments?.getString("color") ?: ""
                DetailScreen(navController, viewModel, material, size, id, color)
            }
            composable("swap") {
                SwapScreen(navController, viewModel)
            }
            composable("ideas") {
                IdeasScreen()
            }
            composable("user_profile") {
                ProfileScreen(navController, viewModel)
            }
        }
    }
}
