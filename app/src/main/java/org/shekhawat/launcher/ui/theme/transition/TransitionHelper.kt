package org.shekhawat.launcher.ui.theme.transition

import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier

/**
 * Maps a transition name to the corresponding pager transition modifier.
 */
fun Modifier.applyPagerTransition(
    name: String,
    page: Int,
    pagerState: PagerState
): Modifier = when (name) {
    "Depth" -> this.pagerDepthTransition(page, pagerState)
    "Cube In Depth" -> this.pagerCubeInDepthTransition(page, pagerState)
    "Cube In Rotation" -> this.pagerCubeInRotationTransition(page, pagerState)
    "Cube In Scaling" -> this.pagerCubeInScalingTransition(page, pagerState)
    "Cube Out Depth" -> this.pagerCubeOutDepthTransition(page, pagerState)
    "Cube Out Rotation" -> this.pagerCubeOutRotationTransition(page, pagerState)
    "Fade" -> this.pagerFadeTransition(page, pagerState)
    "Fade Out" -> this.pagerFadeOutTransition(page, pagerState)
    "Fan" -> this.pagerFanTransition(page, pagerState)
    "Fidget Spin" -> this.pagerFidgetSpinTransition(page, pagerState)
    "Gate" -> this.pagerGateTransition(page, pagerState)
    "Hinge" -> this.pagerHingeTransition(page, pagerState)
    "Spinning" -> this.pagerSpinningClockwiseTransition(page, pagerState)
    "None" -> this
    else -> this.pagerDepthTransition(page, pagerState)
}

val TRANSITION_OPTIONS = listOf(
    "Depth",
    "Cube In Depth",
    "Cube In Rotation",
    "Cube In Scaling",
    "Cube Out Depth",
    "Cube Out Rotation",
    "Fade",
    "Fade Out",
    "Fan",
    "Fidget Spin",
    "Gate",
    "Hinge",
    "Spinning",
    "None"
)
