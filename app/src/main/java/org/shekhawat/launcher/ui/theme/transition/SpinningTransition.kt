package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

fun Modifier.pagerSpinningClockwiseTransition(page: Int, pagerState: PagerState): Modifier {
    return this.then(graphicsLayer {
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
        translationX = pageOffset * size.width

        if (pageOffset < -1f) {
            alpha = 0f
        } else if (pageOffset <= 0) {
            alpha = 1f
            rotationZ = -360f * pageOffset.absoluteValue
        } else if (pageOffset <= 1) {
            alpha = 1f
            rotationZ = 360f * pageOffset.absoluteValue
        } else {
            alpha = 0f
        }

        if (pageOffset.absoluteValue <= 0.5) {
            alpha = 1f
            scaleX = (1 - pageOffset.absoluteValue)
            scaleY = (1 - pageOffset.absoluteValue)
        } else if (pageOffset.absoluteValue > 0.5) {
            alpha = 0f
        }
    })
}

fun Modifier.pagerSpinningAntiClockwiseTransition(page: Int, pagerState: PagerState) =
    graphicsLayer {
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
        translationX = pageOffset * size.width

        if (pageOffset < -1f) {
            alpha = 0f
        } else if (pageOffset <= 0) {
            alpha = 1f
            rotationZ = 360f * pageOffset.absoluteValue
        } else if (pageOffset <= 1) {
            alpha = 1f
            rotationZ = -360f * pageOffset.absoluteValue
        } else {
            alpha = 0f
        }

        if (pageOffset.absoluteValue <= 0.5) {
            alpha = 1f
            scaleX = (1 - pageOffset.absoluteValue)
            scaleY = (1 - pageOffset.absoluteValue)
        } else if (pageOffset.absoluteValue > 0.5) {
            alpha = 0f
        }
    }
