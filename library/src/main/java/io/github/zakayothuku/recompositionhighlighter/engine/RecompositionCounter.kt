package io.github.zakayothuku.recompositionhighlighter.engine

class RecompositionCounter {
    var count: Int = 0
        private set

    var lastRecomposedTimestampMs: Long = 0L
        private set

    fun onRecomposed(): Int {
        count++
        lastRecomposedTimestampMs = System.currentTimeMillis()
        return count
    }

    fun reset() {
        count = 0
        lastRecomposedTimestampMs = 0L
    }
}
