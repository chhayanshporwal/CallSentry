package com.safeguard.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
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

        object RecentActivity :
                Screen(
                        route = "recent_activity",
                        title = "Recent Activity",
                        selectedIcon =
                                Icons.Filled.Info, // Using Info as a placeholder, consider a more
                        // specific icon if available
                        unselectedIcon = Icons.Outlined.Info // Using Info as a placeholder
                )

        object Login :
                Screen(
                        route = "login",
                        title = "Login",
                        selectedIcon = Icons.Filled.People, // Placeholder
                        unselectedIcon = Icons.Outlined.People // Placeholder
                )

        object Profile :
                Screen(
                        route = "profile",
                        title = "Profile",
                        selectedIcon = Icons.Filled.Person, // Placeholder
                        unselectedIcon = Icons.Outlined.Person // Placeholder
                )

        companion object {
                val bottomNavItems get() = listOf(Dashboard, Whitelist, BlockedLog, Settings)
        }
}
