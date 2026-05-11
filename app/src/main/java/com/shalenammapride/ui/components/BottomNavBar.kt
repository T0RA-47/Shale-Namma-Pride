package com.shalenammapride.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.shalenammapride.ui.navigation.Screen
import com.shalenammapride.ui.theme.Saffron
import com.shalenammapride.util.AppStrings

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    strings: AppStrings,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Home, Icons.Filled.Home, strings.home),
        BottomNavItem(Screen.Meals, Icons.Filled.Restaurant, strings.meals),
        BottomNavItem(Screen.Facilities, Icons.Filled.School, strings.facilities),
        BottomNavItem(Screen.Achievements, Icons.Filled.Star, strings.achievements),
        BottomNavItem(Screen.Feedback, Icons.Filled.Feedback, strings.feedback),
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                selected = currentRoute == item.screen.route,
                onClick = { onNavigate(item.screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Saffron,
                    selectedTextColor = Saffron,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
