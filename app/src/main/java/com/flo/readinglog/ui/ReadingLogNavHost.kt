package com.flo.readinglog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flo.readinglog.ui.navigation.Screen
import com.flo.readinglog.ui.screens.books.BooksScreen
import com.flo.readinglog.ui.screens.detail.BookDetailScreen
import com.flo.readinglog.ui.screens.digests.DigestsScreen
import com.flo.readinglog.ui.screens.discover.DiscoverScreen
import com.flo.readinglog.ui.screens.entry.AddEntryScreen
import com.flo.readinglog.ui.screens.entry.EditEntryScreen
import com.flo.readinglog.ui.screens.history.HistoryScreen
import com.flo.readinglog.ui.screens.settings.SettingsScreen
import com.flo.readinglog.ui.screens.today.TodayScreen

private data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Today, "Today", Icons.Default.Today),
    BottomNavItem(Screen.History, "History", Icons.Default.History),
    BottomNavItem(Screen.Books, "Books", Icons.Default.MenuBook),
    BottomNavItem(Screen.Discover, "Discover", Icons.Default.Explore),
    BottomNavItem(Screen.Digests, "Digests", Icons.Default.Email),
)

private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun ReadingLogNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavRoutes.any { it == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onAddEntry = { navController.navigate(Screen.AddEntry.createRoute()) },
                    onEditEntry = { entryId -> navController.navigate(Screen.EditEntry.createRoute(entryId)) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onEditEntry = { entryId -> navController.navigate(Screen.EditEntry.createRoute(entryId)) }
                )
            }
            composable(Screen.Books.route) {
                BooksScreen(
                    onBookClick = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) }
                )
            }
            composable(Screen.Discover.route) { DiscoverScreen() }
            composable(Screen.Digests.route) { DigestsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
                BookDetailScreen(
                    bookId = bookId,
                    onNavigateUp = { navController.navigateUp() },
                    onEditEntry = { entryId -> navController.navigate(Screen.EditEntry.createRoute(entryId)) }
                )
            }
            composable(
                route = Screen.AddEntry.route,
                arguments = listOf(navArgument("bookId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getLong("bookId")?.takeIf { it != -1L }
                AddEntryScreen(
                    initialBookId = bookId,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable(
                route = Screen.EditEntry.route,
                arguments = listOf(navArgument("entryId") { type = NavType.LongType })
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
                EditEntryScreen(
                    entryId = entryId,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}
