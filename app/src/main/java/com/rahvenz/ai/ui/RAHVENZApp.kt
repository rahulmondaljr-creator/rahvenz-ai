package com.rahvenz.ai.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rahvenz.ai.assistant.AssistantViewModel
import com.rahvenz.ai.ui.screens.AssistantScreen
import com.rahvenz.ai.ui.screens.ChatScreen
import com.rahvenz.ai.ui.screens.DiscoverScreen
import com.rahvenz.ai.ui.screens.HomeScreen
import com.rahvenz.ai.ui.screens.SettingsScreen
import com.rahvenz.ai.ui.theme.BackgroundBlack
import com.rahvenz.ai.ui.theme.Crimson
import com.rahvenz.ai.ui.theme.RAHVENZTheme

@Composable
fun RAHVENZApp(vm: AssistantViewModel = viewModel()) {
    RAHVENZTheme {
        val navController = rememberNavController()
        val tabs = listOf(
            Triple("home", "Home", Icons.Default.Home),
            Triple("chat", "Chat", Icons.Default.Email),
            Triple("assistant", "Assistant", Icons.Default.Mic),
            Triple("discover", "Discover", Icons.Default.Search),
            Triple("settings", "Settings", Icons.Default.Settings),
        )

        Scaffold(
            containerColor = BackgroundBlack,
            bottomBar = {
                NavigationBar(containerColor = BackgroundBlack) {
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Crimson,
                                selectedTextColor = Crimson,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding),
            ) {
                composable("home") {
                    HomeScreen(vm = vm, onNavigate = { route -> navController.navigate(route) })
                }
                composable("chat") { ChatScreen(vm = vm) }
                composable("assistant") { AssistantScreen(vm = vm) }
                composable("discover") { DiscoverScreen() }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}
