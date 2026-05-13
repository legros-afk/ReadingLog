package com.flo.readinglog.ui.navigation

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object History : Screen("history")
    object Books : Screen("books")
    object Discover : Screen("discover")
    object Digests : Screen("digests")
    object Settings : Screen("settings")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object AddEntry : Screen("add_entry?bookId={bookId}") {
        fun createRoute(bookId: Long? = null) = if (bookId != null) "add_entry?bookId=$bookId" else "add_entry"
    }
    object EditEntry : Screen("edit_entry/{entryId}") {
        fun createRoute(entryId: Long) = "edit_entry/$entryId"
    }
}
