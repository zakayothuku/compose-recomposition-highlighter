package io.github.zakayothuku.recompositionhighlighter.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class RecompositionSeverity {
    OPTIMAL,   // 1–2 recompositions
    MODERATE,  // 3–5 recompositions
    HIGH,      // 6–9 recompositions
    CRITICAL   // 10+ recompositions (Jank loop)
}

data class HighlightStyle(
    val color: Color,
    val strokeWidth: Dp,
    val severity: RecompositionSeverity
)

object HeatmapColorMapper {

    private val COLOR_OPTIMAL = Color(0xFF4CAF50)   // Green
    private val COLOR_MODERATE = Color(0xFFFFC107)  // Amber / Yellow
    private val COLOR_HIGH = Color(0xFFFF9800)      // Orange
    private val COLOR_CRITICAL = Color(0xFFF44336)  // Red

    fun getHighlightStyle(recompositionCount: Int): HighlightStyle {
        return when {
            recompositionCount <= 2 -> HighlightStyle(COLOR_OPTIMAL, 1.5.dp, RecompositionSeverity.OPTIMAL)
            recompositionCount <= 5 -> HighlightStyle(COLOR_MODERATE, 2.5.dp, RecompositionSeverity.MODERATE)
            recompositionCount <= 9 -> HighlightStyle(COLOR_HIGH, 3.5.dp, RecompositionSeverity.HIGH)
            else -> HighlightStyle(COLOR_CRITICAL, 4.5.dp, RecompositionSeverity.CRITICAL)
        }
    }

    fun calculateDecayAlpha(elapsedMs: Long, decayDurationMs: Long): Float {
        if (elapsedMs <= 0) return 1f
        if (elapsedMs >= decayDurationMs) return 0f
        return (1f - (elapsedMs.toFloat() / decayDurationMs.toFloat())).coerceIn(0f, 1f)
    }
}
