package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pagerFadeOutTransition(page: Int, pagerState: PagerState) = graphicsLayer {
    // Calculate the absolute offset for the current page from the
    // scroll position. We use the absolute value which allows us to mirror
    // any effects for both directions
    val pageOffset = pagerState.getOffsetFractionForPage(page)

    alpha = lerp(
        start = 0.5f,
        stop = 1f,
        fraction = 1f - pageOffset.coerceIn(0f, 1f)
    )
    translationX = (-page * size.width)
}