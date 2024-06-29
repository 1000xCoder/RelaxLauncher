package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerFanTransition(page: Int, pagerState: PagerState) = graphicsLayer {
    cameraDistance = 2000f
    // Calculate the absolute offset for the current page from the
    // scroll position.
    val pageOffset = pagerState.getOffsetFractionForPage(page)
    translationX = pageOffset * size.width
    transformOrigin = TransformOrigin(0f, 0.5f)

    if (pageOffset < -1f) {
        // page is far off screen
        alpha = 0f
    } else if (pageOffset <= 0) {
        // page is to the right of the selected page or the selected page
        alpha = 1f
        rotationY = -120f * pageOffset.absoluteValue

    } else if (pageOffset <= 1) {
        // page is to the left of the selected page
        alpha = 1f
        rotationY = 120f * pageOffset.absoluteValue
    } else {
        alpha = 0f
    }
}