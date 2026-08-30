package io.github.zakayothuku.recompositionhighlighter

import io.github.zakayothuku.recompositionhighlighter.engine.HeatmapColorMapper
import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionCounter
import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionSeverity
import io.github.zakayothuku.recompositionhighlighter.repository.RecompositionRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecompositionEngineTest {

    @Before
    fun setup() {
        RecompositionRegistry.resetAll()
    }

    @After
    fun teardown() {
        RecompositionRegistry.resetAll()
    }

    @Test
    fun `test HeatmapColorMapper assigns correct severity levels`() {
        assertEquals(RecompositionSeverity.OPTIMAL, HeatmapColorMapper.getHighlightStyle(1).severity)
        assertEquals(RecompositionSeverity.OPTIMAL, HeatmapColorMapper.getHighlightStyle(2).severity)
        assertEquals(RecompositionSeverity.MODERATE, HeatmapColorMapper.getHighlightStyle(4).severity)
        assertEquals(RecompositionSeverity.HIGH, HeatmapColorMapper.getHighlightStyle(8).severity)
        assertEquals(RecompositionSeverity.CRITICAL, HeatmapColorMapper.getHighlightStyle(15).severity)
    }

    @Test
    fun `test RecompositionCounter increments on recomposition cycle`() {
        val counter = RecompositionCounter()
        assertEquals(0, counter.count)

        counter.onRecomposed()
        assertEquals(1, counter.count)

        counter.onRecomposed()
        assertEquals(2, counter.count)
        assertTrue(counter.lastRecomposedTimestampMs > 0)

        counter.reset()
        assertEquals(0, counter.count)
    }

    @Test
    fun `test RecompositionRegistry aggregates counts per tag`() {
        RecompositionRegistry.recordRecomposition("UserCard")
        RecompositionRegistry.recordRecomposition("UserCard")
        RecompositionRegistry.recordRecomposition("ProductRow")

        val state = RecompositionRegistry.state.value
        assertEquals(3, state.totalRecompositions)
        assertEquals(2, state.hotspots["UserCard"]?.totalCount)
        assertEquals(1, state.hotspots["ProductRow"]?.totalCount)
    }

    @Test
    fun `test RecompositionRegistry exportReportAsCsv produces valid CSV`() {
        RecompositionRegistry.recordRecomposition("ProfilePhoto")
        val csv = RecompositionRegistry.exportReportAsCsv()

        assertTrue(csv.contains("Tag,Total Recompositions,Velocity (per sec),Severity"))
        assertTrue(csv.contains("\"ProfilePhoto\",1"))
    }

    @Test
    fun `test RecompositionRegistry exportReportAsJson produces valid JSON`() {
        RecompositionRegistry.recordRecomposition("HeaderBar")
        val json = RecompositionRegistry.exportReportAsJson()

        assertTrue(json.contains("\"tag\": \"HeaderBar\""))
        assertTrue(json.contains("\"totalRecompositions\": 1"))
    }
}
