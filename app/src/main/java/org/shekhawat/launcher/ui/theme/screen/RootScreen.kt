package org.shekhawat.launcher.ui.theme.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.SharedPrefManager
import org.shekhawat.launcher.ui.theme.transition.applyPagerTransition

const val DEFAULT_COL = 5

// Page indices: Widgets | Home | App Drawer (single page)
private const val TOTAL_PAGES = 3
private const val WIDGETS_PAGE_INDEX = 0
private const val HOME_PAGE_INDEX = 1
private const val APP_DRAWER_PAGE_INDEX = 2

@Composable
fun RootScreen(appList: List<AppInfo>) {
    val context = LocalContext.current
    val sharedPrefManager = remember(context) { SharedPrefManager(context) }

    // Observe minimalist mode
    val minimalistMode by sharedPrefManager.observeBoolean("minimalist_mode", false)
        .collectAsState(initial = sharedPrefManager.getBoolean("minimalist_mode", false))

    if (minimalistMode) {
        MinimalistHomeScreen(appList = appList)
        return
    }

    // Observe transition setting
    val transition by sharedPrefManager.observeString("pager_transition", "Depth")
        .collectAsState(initial = sharedPrefManager.getString("pager_transition", "Depth"))

    // Observe app sort setting
    val sortOrder by sharedPrefManager.observeString("app_sort", "A-Z")
        .collectAsState(initial = sharedPrefManager.getString("app_sort", "A-Z"))

    val sortedAppList = remember(appList, sortOrder) {
        when (sortOrder) {
            "Z-A" -> appList.sortedByDescending { it.name.lowercase() }
            "Recent" -> appList.reversed()
            else -> appList.sortedBy { it.name.lowercase() }
        }
    }

    val scrollState = rememberPagerState(HOME_PAGE_INDEX) { TOTAL_PAGES }

    // Back button: if not on home page, animate back to home
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = scrollState.currentPage != HOME_PAGE_INDEX) {
        coroutineScope.launch {
            scrollState.animateScrollToPage(HOME_PAGE_INDEX)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .applyPagerTransition(transition, page, scrollState)
            ) {
                when (page) {
                    WIDGETS_PAGE_INDEX -> WidgetsPage()
                    HOME_PAGE_INDEX -> HomeScreen(appList = sortedAppList)
                    APP_DRAWER_PAGE_INDEX -> AppDrawerPage(appList = sortedAppList)
                }
            }
        }

        // Page indicator dots
        PageIndicator(
            pagerState = scrollState,
            totalPages = TOTAL_PAGES,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun PageIndicator(
    pagerState: PagerState,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val maxVisibleDots = 7
    val currentPage = pagerState.currentPage

    val startPage: Int
    val endPage: Int

    if (totalPages <= maxVisibleDots) {
        startPage = 0
        endPage = totalPages - 1
    } else {
        val half = maxVisibleDots / 2
        startPage = (currentPage - half).coerceAtLeast(0)
        endPage = (startPage + maxVisibleDots - 1).coerceAtMost(totalPages - 1)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in startPage..endPage) {
            val isSelected = i == currentPage
            val color by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                animationSpec = tween(300),
                label = "dotColor"
            )
            Box(
                modifier = Modifier
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
