package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

fun Modifier.pagerFanTransition(page: Int, pagerState: PagerState) = graphicsLayer {
    cameraDistance = 2000f
    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
    translationX = pageOffset * size.width
    transformOrigin = TransformOrigin(0f, 0.5f)

    if (pageOffset < -1f) {
        alpha = 0f
    } else if (pageOffset <= 0) {
        alpha = 1f
        rotationY = -120f * pageOffset.absoluteValue
    } else if (pageOffset <= 1) {
        alpha = 1f
        rotationY = 120f * pageOffset.absoluteValue
    } else {
        alpha = 0f
    }
}
