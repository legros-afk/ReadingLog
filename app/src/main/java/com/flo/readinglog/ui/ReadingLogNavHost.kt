package com.flo.readinglog.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flo.readinglog.ui.navigation.Screen
import com.flo.readinglog.ui.screens.auth.SignInScreen
import com.flo.readinglog.ui.screens.books.BooksScreen
import com.flo.readinglog.ui.screens.detail.BookDetailScreen
import com.flo.readinglog.ui.screens.shop.ShopScreen
import com.flo.readinglog.ui.screens.discover.DiscoverScreen
import com.flo.readinglog.ui.screens.entry.AddEntryScreen
import com.flo.readinglog.ui.screens.entry.EditEntryScreen
import com.flo.readinglog.ui.screens.history.HistoryScreen
import com.flo.readinglog.ui.screens.settings.SettingsScreen
import com.flo.readinglog.ui.screens.today.TodayScreen
import kotlinx.coroutines.launch

private data class TabItem(val label: String, val icon: ImageVector)

private val tabItems = listOf(
    TabItem("Today", Icons.Default.AutoStories),
    TabItem("History", Icons.Default.Schedule),
    TabItem("Books", Icons.Default.LibraryBooks),
    TabItem("Discover", Icons.Default.TravelExplore),
    TabItem("Shop", Icons.Default.ShoppingBag),
)

@Composable
fun ReadingLogNavHost() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isSignedIn by authViewModel.isSignedIn.collectAsState()

    when (isSignedIn) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> SignInScreen(onSignedIn = { authViewModel.onSignedIn() })
        true -> MainNavHost()
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tabs") {
        composable("tabs") {
            TabsHost(
                onBookClick = { bookId -> navController.navigate(Screen.BookDetail.createRoute(bookId)) },
                onAddEntry = { navController.navigate(Screen.AddEntry.createRoute()) },
                onEditEntry = { entryId -> navController.navigate(Screen.EditEntry.createRoute(entryId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: return@composable
            BookDetailScreen(
                bookId = bookId,
                onNavigateUp = { navController.navigateUp() },
                onEditEntry = { entryId -> navController.navigate(Screen.EditEntry.createRoute(entryId)) },
            )
        }
        composable(
            route = Screen.AddEntry.route,
            arguments = listOf(navArgument("bookId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId")?.takeIf { it != -1L }
            AddEntryScreen(
                initialBookId = bookId,
                onNavigateUp = { navController.navigateUp() },
            )
        }
        composable(
            route = Screen.EditEntry.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            EditEntryScreen(
                entryId = entryId,
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }
}

@Composable
private fun TabsHost(
    onBookClick: (Long) -> Unit,
    onAddEntry: () -> Unit,
    onEditEntry: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { tabItems.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                tabItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = pagerState.currentPage == index,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                        ),
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
                0 -> TodayScreen(
                    onAddEntry = onAddEntry,
                    onEditEntry = onEditEntry,
                    onNavigateToSettings = onNavigateToSettings,
                )
                1 -> HistoryScreen(onEditEntry = onEditEntry)
                2 -> BooksScreen(onBookClick = onBookClick)
                3 -> DiscoverScreen()
                4 -> ShopScreen()
            }
        }
    }
}
