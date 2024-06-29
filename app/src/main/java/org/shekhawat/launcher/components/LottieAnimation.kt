package org.shekhawat.launcher.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import org.shekhawat.launcher.R

@Composable
fun AnimatedPreloader(modifier: Modifier = Modifier) {
    val preloaderLottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            R.raw.batman_lottie_file
        )
    )

    LottieAnimation(
        composition = preloaderLottieComposition,
        modifier = modifier,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        reverseOnRepeat = true,
    )
}