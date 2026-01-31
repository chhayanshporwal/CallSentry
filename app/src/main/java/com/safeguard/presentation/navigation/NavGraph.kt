package com.safeguard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.safeguard.presentation.screens.blocked.BlockedLogScreen
import com.safeguard.presentation.screens.dashboard.DashboardScreen
import com.safeguard.presentation.screens.settings.SettingsScreen
import com.safeguard.presentation.screens.whitelist.WhitelistScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(route = Screen.Dashboard.route) { DashboardScreen() }

        composable(route = Screen.Whitelist.route) { WhitelistScreen() }

        composable(route = Screen.BlockedLog.route) { BlockedLogScreen() }

        composable(route = Screen.Settings.route) { SettingsScreen() }
    }
}
