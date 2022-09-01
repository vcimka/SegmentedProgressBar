package com.vcimka.segmentedprogressbar

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * @param segmentCount The number of segments displayed on the progress bar
 * @param currentSegmentIndex Index of currently active segment
 * @param isPaused Pause state flag
 * @param gap The spacing between segments
 * @param progressColor color for the progress segment
 * @param segmentColor color for segments background
 * @param cornerRadius segments corner radius
 * @param segmentDuration animation duration of each segment
 * @param onSegmentFilled  The callback triggered when a segment animation ends
 * @param onFinished The callback triggered when a all segments fully filled
 * */
@Composable
fun SegmentedProgressBar(
    modifier: Modifier,
    segmentCount: Int,
    currentSegmentIndex: Int = 0,
    isPaused: Boolean = false,
    @FloatRange(from = 0.0) gap: Dp = 0.dp,
    progressColor: Color = Color.White,
    segmentColor: Color = progressColor.copy(alpha = 0.5f),
    @FloatRange(from = 0.0) cornerRadius: Dp = 0.dp,
    segmentDuration: Long = 1_000,
    onSegmentFilled: (Int) -> Unit = {},
    onFinished: () -> Unit = {}
) {

    require(currentSegmentIndex < segmentCount) {
        "currentSegmentIndex can't be more than segmentCount"
    }

    val gapPx = LocalDensity.current.run { gap.toPx() }
    val cornerRadiusPx = LocalDensity.current.run { cornerRadius.toPx() }
    val requiredForGapsPx = remember(segmentCount) { gapPx * segmentCount.dec() }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val progress = remember(currentSegmentIndex) { Animatable(0f) }
    val prevProgress = remember(currentSegmentIndex) { mutableStateOf(0f) }
    val segments = remember(segmentCount) { 0 until segmentCount }

    LaunchedEffect(isPaused, currentSegmentIndex, segmentDuration) {
        when (isPaused) {
            true -> {
                prevProgress.value = progress.value
                progress.stop()
            }
            false -> progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (segmentDuration * (1 - prevProgress.value)).roundToInt(),
                    easing = LinearEasing
                ),
                block = {
                    if (this.value == this.targetValue) {
                        onSegmentFilled(currentSegmentIndex)
                        if (currentSegmentIndex.inc() == segmentCount) onFinished()
                    }
                }
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        onDraw = {

            val width = (size.width - requiredForGapsPx) / segmentCount

            segments.forEach { position ->
                drawSegment(
                    index = position,
                    progress = 1f,
                    width = width,
                    height = size.height,
                    gap = gapPx,
                    color = segmentColor,
                    cornerRadius = cornerRadiusPx,
                    isRtl = isRtl
                )

                val curProgress = when (isRtl) {
                    true -> when {
                        segmentCount.dec() - position == currentSegmentIndex -> progress.value
                        segmentCount.dec() - position < currentSegmentIndex -> 1f
                        else -> 0f
                    }
                    false -> when {
                        position == currentSegmentIndex -> progress.value
                        position < currentSegmentIndex -> 1f
                        else -> 0f
                    }
                }

                drawSegment(
                    index = position,
                    progress = curProgress,
                    width = width,
                    height = size.height,
                    gap = gapPx,
                    color = progressColor,
                    cornerRadius = cornerRadiusPx,
                    isRtl = isRtl
                )
            }
        }
    )
}

private fun DrawScope.drawSegment(
    index: Int,
    progress: Float,
    width: Float,
    height: Float,
    gap: Float,
    color: Color,
    cornerRadius: Float,
    isRtl: Boolean
) {

    val x1 = when (isRtl) {
        true -> index * width + index * gap + width
        false -> index * width + index * gap
    }

    val x2 = when (isRtl) {
        true -> -(width * progress)
        false -> width * progress
    }

    drawRoundRect(
        topLeft = Offset(x = x1, y = 0f),
        size = Size(width = x2, height = height),
        color = color,
        cornerRadius = CornerRadius(x = cornerRadius, y = cornerRadius)
    )
}