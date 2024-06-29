package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerDepthTransition(page: Int, pagerState: PagerState) = graphicsLayer {
    // Calculate the absolute offset for the current page from the
    // scroll position.
    val pageOffset = pagerState.getOffsetFractionForPage(page)

    if (pageOffset < -1f) {
        // page is far off screen
        alpha = 0f
    } else if (pageOffset <= 0) {
        // page is to the right of the selected page or the selected page

        translationX = pageOffset * size.width
        // alpha = 1- abs(pageOffset)
        scaleX = 1 - abs(pageOffset)
        scaleY = 1 - abs(pageOffset)
    } else if (pageOffset <= 1) {
        // page is to the left of the selected page
        alpha = 1f
        scaleX = 1f
        scaleY = 1f
        translationX = 0f

    } else {
        alpha = 0f
    }
}.zIndex(-page.toFloat())