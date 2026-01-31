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
        composable(route = Screen.Dashboard.route) { DashboardScreen() }

        composable(route = Screen.Whitelist.route) { WhitelistScreen() }

        composable(route = Screen.BlockedLog.route) { BlockedLogScreen() }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) }
            )
        }

        composable(route = Screen.Onboarding.route) {
            com.safeguard.presentation.screens.onboarding.OnboardingScreen(
                    navController = navController
            )
        }
    }
}
