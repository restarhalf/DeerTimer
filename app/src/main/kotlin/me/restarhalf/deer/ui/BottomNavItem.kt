package me.restarhalf.deer.ui

import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Stopwatch
import top.yukonga.miuix.kmp.icon.extended.Weeks

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "计时",
        icon = MiuixIcons.Stopwatch
    )

    object Statistics : BottomNavItem(
        route = "statistics",
        title = "统计",
        icon = MiuixIcons.ListView
    )

    object Histories : BottomNavItem(
        route = "history",
        title = "历史",
        icon = MiuixIcons.Weeks
    )

    object Settings : BottomNavItem(
        route = "settings",
        title = "设置",
        icon = MiuixIcons.Settings
    )

    companion object {
        val items = listOf(Home, Statistics, Histories, Settings)
    }
}