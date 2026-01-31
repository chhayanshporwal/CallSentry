package com.safeguard.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
        val route: String,
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
) {
        object Dashboard :
                Screen(
                        route = "dashboard",
                        title = "Dashboard",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home
                )

        object Whitelist :
                Screen(
                        route = "whitelist",
                        title = "Whitelist",
                        selectedIcon = Icons.Filled.People,
                        unselectedIcon = Icons.Outlined.People
                )

        object BlockedLog :
                Screen(
                        route = "blocked",
                        title = "Blocked",
                        selectedIcon = Icons.Filled.Block,
                        unselectedIcon = Icons.Outlined.Block
                )

        object Settings :
                Screen(
                        route = "settings",
                        title = "Settings",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings
                )

        object Onboarding :
                Screen(
                        route = "onboarding",
                        title = "Onboarding",
                        selectedIcon = Icons.Filled.Info,
                        unselectedIcon = Icons.Outlined.Info
                )

        companion object {
                val bottomNavItems = listOf(Dashboard, Whitelist, BlockedLog, Settings)
        }
}
