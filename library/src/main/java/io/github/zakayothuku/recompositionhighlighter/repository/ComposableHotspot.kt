package io.github.zakayothuku.recompositionhighlighter.repository

import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionSeverity

data class ComposableHotspot(
    val tag: String,
    val totalCount: Int,
    val recompositionsPerSecond: Double,
    val lastTimestampMs: Long,
    val severity: RecompositionSeverity
)
