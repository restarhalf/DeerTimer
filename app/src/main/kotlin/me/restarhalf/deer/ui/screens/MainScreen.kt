package me.restarhalf.deer.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.restarhalf.deer.BuildConfig
import me.restarhalf.deer.data.ThemeRepository
import me.restarhalf.deer.ui.BottomNavItem
import me.restarhalf.deer.ui.components.TwoTextButtonsRow
import me.restarhalf.deer.ui.util.UpdateChecker
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarMode
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
private fun BottomNavigationBar(navController: NavController, isFloating: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = BottomNavItem.items
    val navItems = items.map { NavigationItem(it.title, it.icon) }
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }
        .let { if (it >= 0) it else -1 }
    if (!isFloating) {
        NavigationBar(
            items = navItems,
            selected = selectedIndex,
            onClick = { index ->
                navController.navigate(items[index].route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            }
        )
    } else {
        FloatingNavigationBar(
            items = navItems,
            selected = selectedIndex,
            onClick = { index ->
                navController.navigate(items[index].route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                }
            },
            mode = FloatingNavigationBarMode.IconOnly,
            modifier = Modifier
        )
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs by ThemeRepository.themePreferences.collectAsState()

    val backgroundAlpha = prefs.backgroundAlpha
    val backgroundAmbiguity = prefs.backgroundAmbiguity
    val backgroundImageUri = prefs.backgroundImageUri
    val backgroundBitmap by rememberUriImageBitmap(backgroundImageUri)
    val componentsAlpha = prefs.componentsAlpha

    val baseColors = MiuixTheme.colorScheme
    val contentAlpha = componentsAlpha.coerceIn(0f, 1f)
    val contentColors by remember(baseColors, contentAlpha) {
        derivedStateOf {
            if (contentAlpha >= 0.999f) {
                baseColors
            } else {
                baseColors.copy(
                    surfaceContainer = baseColors.surfaceContainer.copy(alpha = contentAlpha),
                    surfaceContainerHigh = baseColors.surfaceContainerHigh.copy(alpha = contentAlpha),
                    surfaceContainerHighest = baseColors.surfaceContainerHighest.copy(alpha = contentAlpha),
                    surfaceVariant = baseColors.surfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
    }

    val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val showNotifyDialog = remember {
        mutableStateOf(!notificationsEnabled)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = BottomNavItem.items
    val selectedIndex =
        items.indexOfFirst { it.route == currentRoute }.let { if (it >= 0) it else -1 }

    val swipeThresholdPx = with(LocalDensity.current) { 80.dp.toPx() }

    fun openNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra("app_uid", context.applicationInfo.uid)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    val owner = "restarhalf"
    val repo = "DeerTimer"

    val showUpdateDialog = remember { mutableStateOf(false) }
    var latestTag by remember { mutableStateOf<String?>(null) }

    fun stripSuffix(version: String): String =
        version.trimStart('v', 'V').substringBefore('-')

    fun parseNumbers(version: String): List<Int> =
        stripSuffix(version)
            .split('.')
            .map { it.toIntOrNull() ?: 0 }
            .let {
                when {
                    it.size >= 3 -> it.take(3)
                    it.size == 2 -> it + listOf(0)
                    it.size == 1 -> it + listOf(0, 0)
                    else -> listOf(0, 0, 0)
                }
            }

    fun isRemoteGreater(local: String, remote: String): Boolean {
        val localNums = parseNumbers(local)
        val remoteNums = parseNumbers(remote)
        for (i in 0..2) {
            if (remoteNums[i] > localNums[i]) return true
            if (remoteNums[i] < localNums[i]) return false
        }
        return false
    }

    LaunchedEffect(Unit) {
        UpdateChecker.fetchLatestVersion(owner, repo)?.let { remoteVer ->
            latestTag = remoteVer
            if (isRemoteGreater(BuildConfig.VERSION_NAME, remoteVer)) {
                showUpdateDialog.value = true
            }
        }
    }

    val isFloating = prefs.miuixBottomBarFloating
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface)
    ) {
        val bitmap = backgroundBitmap
        if (bitmap != null) {
            val blurRadius = (backgroundAmbiguity.coerceIn(0f, 1f) * 32f).dp
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (backgroundAmbiguity > 0f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(blurRadius)
                        } else {
                            Modifier
                        }
                    ),
                contentScale = ContentScale.Crop,
                alpha = backgroundAlpha
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (selectedIndex >= 0)
                    BottomNavigationBar(navController, isFloating)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            MiuixTheme(colors = contentColors) {
                NavHost(
                    navController = navController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .then(
                            if (selectedIndex >= 0) {
                                Modifier.pointerInput(selectedIndex, swipeThresholdPx) {
                                    var totalDragX = 0f
                                    detectHorizontalDragGestures(
                                        onDragStart = { totalDragX = 0f },
                                        onHorizontalDrag = { _, dragAmount ->
                                            totalDragX += dragAmount
                                        },
                                        onDragCancel = { totalDragX = 0f },
                                        onDragEnd = {
                                            val targetIndex = when {
                                                totalDragX <= -swipeThresholdPx -> selectedIndex + 1
                                                totalDragX >= swipeThresholdPx -> selectedIndex - 1
                                                else -> -1
                                            }

                                            if (targetIndex in items.indices) {
                                                navController.navigate(items[targetIndex].route) {
                                                    launchSingleTop = true
                                                    restoreState = true
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                }
                                            }
                                            totalDragX = 0f
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        ),
                    enterTransition = {
                        val initialIndex =
                            BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                        val targetIndex =
                            BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }

                        if (initialIndex >= 0 && targetIndex >= 0) {
                            val forward = targetIndex > initialIndex
                            slideInHorizontally(
                                initialOffsetX = { if (forward) it else -it },
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        }
                    },
                    exitTransition = {
                        val initialIndex =
                            BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                        val targetIndex =
                            BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }

                        if (initialIndex >= 0 && targetIndex >= 0) {
                            val forward = targetIndex > initialIndex
                            slideOutHorizontally(
                                targetOffsetX = { if (forward) -it else it },
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 5 },
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 500),
                                targetAlpha = 0.5f,
                            )
                        }
                    },
                    popEnterTransition = {
                        val initialIndex =
                            BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                        val targetIndex =
                            BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }

                        if (initialIndex >= 0 && targetIndex >= 0) {
                            val forward = targetIndex > initialIndex
                            slideInHorizontally(
                                initialOffsetX = { if (forward) it else -it },
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it / 5 },
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 500),
                                initialAlpha = 0.5f,
                            )
                        }
                    },
                    popExitTransition = {
                        val initialIndex =
                            BottomNavItem.items.indexOfFirst { it.route == initialState.destination.route }
                        val targetIndex =
                            BottomNavItem.items.indexOfFirst { it.route == targetState.destination.route }

                        if (initialIndex >= 0 && targetIndex >= 0) {
                            val forward = targetIndex > initialIndex
                            slideOutHorizontally(
                                targetOffsetX = { if (forward) -it else it },
                                animationSpec = tween(
                                    durationMillis = 260,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                ),
                            )
                        }
                    }
                ) {
                    composable(BottomNavItem.Home.route) { HomeScreen() }
                    composable(BottomNavItem.Statistics.route) { StatisticsScreen() }
                    composable(BottomNavItem.Histories.route) { HistoryScreen() }
                    composable(BottomNavItem.Settings.route) { SettingsScreen(navController) }
                    composable("about") { AboutScreen(navController) }
                    composable("theme") { ThemeScreen(navController) }
                    composable("background") { ChangeBackGroundScreen(navController) }
                    composable("open_source") { OpenSourceScreen(navController) }
                    composable("reward") { RewardScreen(navController) }
                }
            }
        }
    }

    if (showUpdateDialog.value && latestTag != null) {
        SuperDialog(
            title = "检测到新版本",
            summary =
                "当前版本：${BuildConfig.VERSION_NAME}\n" +
                        "最新版本：$latestTag\n\n" +
                        "针对你的牛牛进行了一些优化，是否前往 GitHub 下载？",
            show = showUpdateDialog,
            onDismissRequest = { showUpdateDialog.value = false }
        ) {
            TwoTextButtonsRow(
                leftText = "稍后再说",
                onLeftClick = { showUpdateDialog.value = false },
                rightText = "去下载",
                onRightClick = {
                    showUpdateDialog.value = false
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://github.com/$owner/$repo/releases/latest".toUri()
                    )
                    context.startActivity(intent)
                },
            )
        }
    }

    SuperDialog(
        title = "还未开启通知权限",
        summary = "为确保应用能在后台继续计时，请授予通知权限！",
        show = showNotifyDialog,
        onDismissRequest = { showNotifyDialog.value = false }
    ) {
        TwoTextButtonsRow(
            leftText = "以后再说",
            onLeftClick = { showNotifyDialog.value = false },
            rightText = "去开启",
            onRightClick = {
                openNotificationSettings(context)
                showNotifyDialog.value = false
            },
        )
    }
}

@Composable
private fun rememberUriImageBitmap(uri: String?): State<ImageBitmap?> {
    val context = LocalContext.current.applicationContext
    val displayMetrics = context.resources.displayMetrics
    val maxWidthPx = displayMetrics.widthPixels.coerceAtLeast(1)
    val maxHeightPx = displayMetrics.heightPixels.coerceAtLeast(1)

    return produceState(initialValue = null, uri) {
        val cleaned = uri?.trim()?.takeIf { it.isNotBlank() }
        if (cleaned == null) {
            value = null
            return@produceState
        }

        val parsed = runCatching { cleaned.toUri() }.getOrNull()
        if (parsed == null) {
            value = null
            return@produceState
        }

        value = withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                resolver.openInputStream(parsed)?.use { input ->
                    BitmapFactory.decodeStream(input, null, bounds)
                }

                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    return@withContext null
                }

                val inSampleSize = calculateInSampleSize(
                    options = bounds,
                    maxWidthPx = maxWidthPx,
                    maxHeightPx = maxHeightPx
                )
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                resolver.openInputStream(parsed)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOptions)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    maxWidthPx: Int,
    maxHeightPx: Int
): Int {
    val width = options.outWidth
    val height = options.outHeight

    val targetWidth = (maxWidthPx * 2).coerceAtLeast(1)
    val targetHeight = (maxHeightPx * 2).coerceAtLeast(1)

    var sampleSize = 1
    while (width / sampleSize > targetWidth || height / sampleSize > targetHeight) {
        sampleSize *= 2
    }
    return sampleSize.coerceAtLeast(1)
}

