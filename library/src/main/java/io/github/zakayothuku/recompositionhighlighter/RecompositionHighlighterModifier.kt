package io.github.zakayothuku.recompositionhighlighter

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.recompositionhighlighter.engine.HeatmapColorMapper
import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionCounter
import io.github.zakayothuku.recompositionhighlighter.repository.RecompositionRegistry

/**
 * Attaches a visual recomposition heatmap border and badge over any Composable.
 *
 * @param tag Optional identifier for this composable in the global Recomposition Leaderboard.
 * @param enabled Whether highlighting is locally enabled for this node.
 */
fun Modifier.recompositionHighlighter(
    tag: String? = null,
    enabled: Boolean = true
): Modifier = composed {
    val registryState by RecompositionRegistry.state.collectAsState()

    if (!enabled || !registryState.globalEnabled) {
        return@composed this
    }

    val counter = remember { RecompositionCounter() }
    val nodeTag = remember(tag) { tag ?: "Node#${counter.hashCode().toString(16)}" }
    val density = LocalDensity.current

    // Record recomposition on each composition cycle
    SideEffect {
        counter.onRecomposed()
        RecompositionRegistry.recordRecomposition(nodeTag)
    }

    this.drawWithContent {
        drawContent()

        val count = counter.count
        if (count > 0) {
            val style = HeatmapColorMapper.getHighlightStyle(count)
            val strokeWidthPx = with(density) { style.strokeWidth.toPx() }

            // Draw bounding border
            drawRect(
                color = style.color,
                topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                size = Size(
                    size.width - strokeWidthPx,
                    size.height - strokeWidthPx
                ),
                style = Stroke(width = strokeWidthPx)
            )

            // Draw small count badge in top-right corner if enabled
            if (registryState.showCountBadges) {
                val badgeText = "$count"
                val textSizePx = with(density) { 10.sp.toPx() }

                val textPaint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = textSizePx
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }

                val textBounds = Rect()
                textPaint.getTextBounds(badgeText, 0, badgeText.length, textBounds)
                val badgePadding = with(density) { 3.dp.toPx() }
                val badgeWidth = textBounds.width() + (badgePadding * 2)
                val badgeHeight = textBounds.height() + (badgePadding * 2)

                val badgeLeft = size.width - badgeWidth - 2f
                val badgeTop = 2f

                // Badge background
                drawRect(
                    color = style.color,
                    topLeft = Offset(badgeLeft, badgeTop),
                    size = Size(badgeWidth, badgeHeight)
                )

                // Badge text
                drawContext.canvas.nativeCanvas.drawText(
                    badgeText,
                    badgeLeft + (badgeWidth / 2f),
                    badgeTop + badgeHeight - (badgePadding / 2f),
                    textPaint
                )
            }
        }
    }
}
