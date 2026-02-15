package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlin.math.abs

fun Modifier.pagerDepthTransition(page: Int, pagerState: PagerState) = graphicsLayer {
    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)

    if (pageOffset < -1f) {
        alpha = 0f
    } else if (pageOffset <= 0) {
        translationX = pageOffset * size.width
        scaleX = 1 - abs(pageOffset)
        scaleY = 1 - abs(pageOffset)
    } else if (pageOffset <= 1) {
        alpha = 1f
        scaleX = 1f
        scaleY = 1f
        translationX = 0f
    } else {
        alpha = 0f
    }
}.zIndex(-page.toFloat())
