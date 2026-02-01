package com.safeguard.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
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
                        val themeMode by settingsDataStore.themeMode.collectAsState(initial = 0)

                        val isDarkTheme =
                                when (themeMode) {
                                        1 -> false // Light
                                        2 -> true // Dark
                                        else -> isSystemInDarkTheme() // System
                                }

                        SafeGuardTheme(darkTheme = isDarkTheme) {
                                val navController = rememberNavController()
                                val navBackStackEntry by
                                        navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination

                                // Check if first launch
                                val isFirstLaunchState by
                                        settingsDataStore.isFirstLaunch.collectAsState(
                                                initial = null
                                        )

                                if (isFirstLaunchState == null) {
                                        // Show a blank screen or a splash screen while loading
                                        // preference
                                        androidx.compose.foundation.layout.Box(
                                                modifier =
                                                        androidx.compose.ui.Modifier.fillMaxSize(),
                                                contentAlignment =
                                                        androidx.compose.ui.Alignment.Center
                                        ) { androidx.compose.material3.CircularProgressIndicator() }
                                } else {
                                        // Check if user is logged in
                                        val isUserLoggedIn =
                                                com.google.firebase.auth.FirebaseAuth.getInstance()
                                                        .currentUser != null

                                        val startDestination =
                                                when {
                                                        // If user is logged in, go to Dashboard
                                                        // (skip onboarding)
                                                        isUserLoggedIn -> Screen.Dashboard.route
                                                        // If first launch, go to onboarding
                                                        isFirstLaunchState == true ->
                                                                Screen.Onboarding.route
                                                        // Otherwise, go to login
                                                        else -> Screen.Login.route
                                                }

                                        // Hide bottom bar on Onboarding screen
                                        val showBottomBar =
                                                currentDestination?.route !in
                                                        listOf(
                                                                Screen.Onboarding.route,
                                                                Screen.Login.route,
                                                                Screen.Profile.route
                                                        )

                                        Scaffold(
                                                bottomBar = {
                                                        if (showBottomBar) {
                                                                NavigationBar(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .surface,
                                                                        tonalElevation =
                                                                                androidx.compose.ui
                                                                                        .unit.Dp(0f)
                                                                ) {
                                                                        Screen.bottomNavItems
                                                                                .forEach { screen ->
                                                                                        val selected =
                                                                                                currentDestination
                                                                                                        ?.hierarchy
                                                                                                        ?.any {
                                                                                                                it.route ==
                                                                                                                        screen.route
                                                                                                        } ==
                                                                                                        true

                                                                                        NavigationBarItem(
                                                                                                icon = {
                                                                                                        Icon(
                                                                                                                imageVector =
                                                                                                                        if (selected
                                                                                                                        )
                                                                                                                                screen.selectedIcon
                                                                                                                        else
                                                                                                                                screen.unselectedIcon,
                                                                                                                contentDescription =
                                                                                                                        screen.title
                                                                                                        )
                                                                                                },
                                                                                                label = {
                                                                                                        Text(
                                                                                                                screen.title
                                                                                                        )
                                                                                                },
                                                                                                selected =
                                                                                                        selected,
                                                                                                onClick = {
                                                                                                        navController
                                                                                                                .navigate(
                                                                                                                        screen.route
                                                                                                                ) {
                                                                                                                        popUpTo(
                                                                                                                                navController
                                                                                                                                        .graph
                                                                                                                                        .findStartDestination()
                                                                                                                                        .id
                                                                                                                        ) {
                                                                                                                                saveState =
                                                                                                                                        true
                                                                                                                        }
                                                                                                                        launchSingleTop =
                                                                                                                                true
                                                                                                                        restoreState =
                                                                                                                                true
                                                                                                                }
                                                                                                },
                                                                                                colors =
                                                                                                        NavigationBarItemDefaults
                                                                                                                .colors(
                                                                                                                        selectedIconColor =
                                                                                                                                MaterialTheme
                                                                                                                                        .colorScheme
                                                                                                                                        .primary,
                                                                                                                        selectedTextColor =
                                                                                                                                MaterialTheme
                                                                                                                                        .colorScheme
                                                                                                                                        .primary,
                                                                                                                        indicatorColor =
                                                                                                                                MaterialTheme
                                                                                                                                        .colorScheme
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
}
