package io.github.zakayothuku.recompositionhighlighter.repository

import io.github.zakayothuku.recompositionhighlighter.engine.HeatmapColorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecompositionRegistryState(
    val globalEnabled: Boolean = true,
    val decayDurationMs: Long = 1500L,
    val showCountBadges: Boolean = true,
    val hotspots: Map<String, ComposableHotspot> = emptyMap(),
    val totalRecompositions: Int = 0
)

object RecompositionRegistry {

    private val _state = MutableStateFlow(RecompositionRegistryState())
    val state: StateFlow<RecompositionRegistryState> = _state.asStateFlow()

    private val timestampsMap = mutableMapOf<String, MutableList<Long>>()

    fun recordRecomposition(tag: String) {
        if (!_state.value.globalEnabled) return

        val now = System.currentTimeMillis()
        val timestamps = timestampsMap.getOrPut(tag) { mutableListOf() }
        timestamps.add(now)

        // Keep timestamps within last 3 seconds to calculate velocity (recomps/sec)
        timestamps.removeAll { now - it > 3000 }
        val velocity = if (timestamps.size > 1) {
            val windowSeconds = (now - timestamps.first()).coerceAtLeast(100) / 1000.0
            timestamps.size / windowSeconds
        } else {
            0.0
        }

        _state.update { current ->
            val existing = current.hotspots[tag]
            val newTotal = (existing?.totalCount ?: 0) + 1
            val style = HeatmapColorMapper.getHighlightStyle(newTotal)

            val updatedHotspot = ComposableHotspot(
                tag = tag,
                totalCount = newTotal,
                recompositionsPerSecond = velocity,
                lastTimestampMs = now,
                severity = style.severity
            )

            current.copy(
                hotspots = current.hotspots + (tag to updatedHotspot),
                totalRecompositions = current.totalRecompositions + 1
            )
        }
    }

    fun setGlobalEnabled(enabled: Boolean) {
        _state.update { it.copy(globalEnabled = enabled) }
    }

    fun setDecayDuration(durationMs: Long) {
        _state.update { it.copy(decayDurationMs = durationMs) }
    }

    fun setShowCountBadges(show: Boolean) {
        _state.update { it.copy(showCountBadges = show) }
    }

    fun resetAll() {
        timestampsMap.clear()
        _state.update {
            it.copy(
                hotspots = emptyMap(),
                totalRecompositions = 0
            )
        }
    }

    fun exportReportAsCsv(): String {
        val hotspots = _state.value.hotspots.values.sortedByDescending { it.totalCount }
        val sb = StringBuilder("Tag,Total Recompositions,Velocity (per sec),Severity\n")
        hotspots.forEach {
            sb.append("\"${it.tag}\",${it.totalCount},${String.format("%.1f", it.recompositionsPerSecond)},${it.severity.name}\n")
        }
        return sb.toString()
    }

    fun exportReportAsJson(): String {
        val hotspots = _state.value.hotspots.values.sortedByDescending { it.totalCount }
        val sb = StringBuilder("[\n")
        hotspots.forEachIndexed { index, h ->
            sb.append("  {\n")
            sb.append("    \"tag\": \"${h.tag}\",\n")
            sb.append("    \"totalRecompositions\": ${h.totalCount},\n")
            sb.append("    \"velocity\": ${String.format("%.1f", h.recompositionsPerSecond)},\n")
            sb.append("    \"severity\": \"${h.severity.name}\"\n")
            sb.append("  }")
            if (index < hotspots.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }
}
