package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

fun Modifier.pagerFadeTransition(page: Int, pagerState: PagerState) =
    graphicsLayer {
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
        translationX = pageOffset * size.width
        alpha = 1 - pageOffset.absoluteValue
    }
