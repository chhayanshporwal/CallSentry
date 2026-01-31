package com.safeguard.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.safeguard.presentation.navigation.NavGraph
import com.safeguard.presentation.navigation.Screen
import com.safeguard.presentation.theme.SafeGuardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var settingsDataStore: com.safeguard.data.preferences.SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SafeGuardTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Check if first launch
                val isFirstLaunch by settingsDataStore.isFirstLaunch.collectAsState(initial = true)
                val startDestination =
                        if (isFirstLaunch) Screen.Onboarding.route else Screen.Dashboard.route

                // Hide bottom bar on Onboarding screen
                val showBottomBar = currentDestination?.route != Screen.Onboarding.route

                Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = androidx.compose.ui.unit.Dp(0f)
                                ) {
                                    Screen.bottomNavItems.forEach { screen ->
                                        val selected =
                                                currentDestination?.hierarchy?.any {
                                                    it.route == screen.route
                                                } == true

                                        NavigationBarItem(
                                                icon = {
                                                    Icon(
                                                            imageVector =
                                                                    if (selected)
                                                                            screen.selectedIcon
                                                                    else screen.unselectedIcon,
                                                            contentDescription = screen.title
                                                    )
                                                },
                                                label = { Text(screen.title) },
                                                selected = selected,
                                                onClick = {
                                                    navController.navigate(screen.route) {
                                                        popUpTo(
                                                                navController.graph
                                                                        .findStartDestination()
                                                                        .id
                                                        ) { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                },
                                                colors =
                                                        NavigationBarItemDefaults.colors(
                                                                selectedIconColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                selectedTextColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary,
                                                                indicatorColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primaryContainer
                                                        )
                                        )
                                    }
                                }
                            }
                        }
                ) { innerPadding ->
                    NavGraph(
                            navController = navController,
                            paddingValues = innerPadding,
                            startDestination = startDestination
                    )
                }
            }
        }
    }
}
