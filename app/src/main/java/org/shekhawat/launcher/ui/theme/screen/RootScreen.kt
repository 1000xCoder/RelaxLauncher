package org.shekhawat.launcher.ui.theme.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import org.shekhawat.launcher.AppInfo
import org.shekhawat.launcher.R
import org.shekhawat.launcher.ui.theme.transition.pagerFadeTransition
import kotlin.math.ceil

const val row = 6
const val col = 5

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RootScreen(appList: List<AppInfo>) {
    val appPageSize = getAppPageSize(appList.size)
    val scrollState = rememberPagerState(1) { appPageSize + 2 }

    HorizontalPager(
        state = scrollState,
    ) {
//        Box(
//            modifier = (if (it > 1) Modifier.pagerFadeTransition(
//                page = it,
//                pagerState = scrollState
//            ) else Modifier)
//        ) {
        when (it) {
            0 -> TimeScreen()
            1 -> HomeScreen(appList = appList)
            else -> AppListScreen(appList = appList, pageNo = it - 2, row = row, col = col)
        }
//        }
    }
}

fun getAppPageSize(size: Int): Int {
    return ceil(size.div((row * col).toDouble())).toInt()
}
