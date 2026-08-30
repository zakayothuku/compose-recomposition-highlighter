package io.github.zakayothuku.recompositionhighlighter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionSeverity
import io.github.zakayothuku.recompositionhighlighter.repository.ComposableHotspot

@Composable
fun HotspotLeaderboardView(
    hotspots: List<ComposableHotspot>,
    modifier: Modifier = Modifier
) {
    if (hotspots.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recomposition hotspots recorded yet. Interact with the app to track rendering cycles.",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(hotspots, key = { it.tag }) { hotspot ->
                HotspotCard(hotspot = hotspot)
            }
        }
    }
}

@Composable
private fun HotspotCard(hotspot: ComposableHotspot) {
    val severityColor = when (hotspot.severity) {
        RecompositionSeverity.OPTIMAL -> Color(0xFF4CAF50)
        RecompositionSeverity.MODERATE -> Color(0xFFFFC107)
        RecompositionSeverity.HIGH -> Color(0xFFFF9800)
        RecompositionSeverity.CRITICAL -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hotspot.tag,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Rate: ${String.format("%.1f", hotspot.recompositionsPerSecond)} recomps/sec",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severityColor
                ) {
                    Text(
                        text = "${hotspot.totalCount}x",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = hotspot.severity.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }
            }
        }
    }
}
