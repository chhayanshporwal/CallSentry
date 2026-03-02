package com.safeguard.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.safeguard.presentation.screens.blocked.BlockedLogScreen
import com.safeguard.presentation.screens.dashboard.DashboardScreen
import com.safeguard.presentation.screens.settings.SettingsScreen
import com.safeguard.presentation.screens.whitelist.WhitelistScreen

@Composable
fun NavGraph(
        navController: NavHostController,
        paddingValues: PaddingValues,
        startDestination: String
) {
    NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
    ) {
        composable(route = Screen.Dashboard.route) {
            DashboardScreen(
                    onNavigateToRecentActivity = {
                        navController.navigate(Screen.RecentActivity.route)
                    },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(route = Screen.Whitelist.route) { WhitelistScreen() }

        composable(route = Screen.BlockedLog.route) { BlockedLogScreen() }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
            )
        }

        composable(route = Screen.Onboarding.route) {
            com.safeguard.presentation.screens.onboarding.OnboardingScreen(
                    navController = navController
            )
        }

        composable(Screen.RecentActivity.route) {
            com.safeguard.presentation.screens.recent.RecentActivityScreen(
                    onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Profile.route) {
            com.safeguard.presentation.screens.profile.ProfileScreen(navController = navController)
        }

        composable(route = Screen.Login.route) {
            com.safeguard.presentation.screens.auth.LoginScreen(navController = navController)
        }
    }
}
