package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerSpinningClockwiseTransition(page: Int, pagerState: PagerState): Modifier {
    return this.then(graphicsLayer {
        // Calculate the absolute offset for the current page from the
        // scroll position.
        val pageOffset = ((pagerState.currentPage - page) + pagerState
            .currentPageOffsetFraction)
        translationX = pageOffset * size.width

        if (pageOffset < -1f) {
            // page is far off screen
            alpha = 0f
        } else if (pageOffset <= 0) {
            // page is to the right of the selected page or the selected page
            alpha = 1f
            rotationZ = -360f * pageOffset.absoluteValue

        } else if (pageOffset <= 1) {
            // page is to the left of the selected page
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

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerSpinningAntiClockwiseTransition(page: Int, pagerState: PagerState) =
    graphicsLayer {
        // Calculate the absolute offset for the current page from the
        // scroll position.
        val pageOffset = pagerState.getOffsetFractionForPage(page)
        translationX = pageOffset * size.width

        if (pageOffset < -1f) {
            // page is far off screen
            alpha = 0f
        } else if (pageOffset <= 0) {
            // page is to the right of the selected page or the selected page
            alpha = 1f
            rotationZ = 360f * pageOffset.absoluteValue

        } else if (pageOffset <= 1) {
            // page is to the left of the selected page
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