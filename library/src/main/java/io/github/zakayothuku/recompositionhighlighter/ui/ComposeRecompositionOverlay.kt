package io.github.zakayothuku.recompositionhighlighter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.recompositionhighlighter.engine.RecompositionSeverity
import io.github.zakayothuku.recompositionhighlighter.repository.ComposableHotspot
import io.github.zakayothuku.recompositionhighlighter.repository.RecompositionRegistry
import io.github.zakayothuku.recompositionhighlighter.repository.RecompositionRegistryState

/**
 * Stateful Container collecting RecompositionRegistry state.
 */
@Composable
fun ComposeRecompositionOverlay(
    modifier: Modifier = Modifier
) {
    val state by RecompositionRegistry.state.collectAsState()

    ComposeRecompositionOverlayContent(
        state = state,
        onGlobalToggle = { RecompositionRegistry.setGlobalEnabled(it) },
        onShowBadgesToggle = { RecompositionRegistry.setShowCountBadges(it) },
        onReset = { RecompositionRegistry.resetAll() },
        onExportCsv = { RecompositionRegistry.exportReportAsCsv() },
        onExportJson = { RecompositionRegistry.exportReportAsJson() },
        modifier = modifier
    )
}

/**
 * Stateless Content Composable adhering to Safaricom Compose Previews & Clean Architecture standards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeRecompositionOverlayContent(
    state: RecompositionRegistryState,
    onGlobalToggle: (Boolean) -> Unit,
    onShowBadgesToggle: (Boolean) -> Unit,
    onReset: () -> Unit,
    onExportCsv: () -> String,
    onExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var exportToast by remember { mutableStateOf<String?>(null) }

    val sortedHotspots = remember(state.hotspots) {
        state.hotspots.values.sortedByDescending { it.totalCount }
    }
    val topHotspot = sortedHotspots.firstOrNull()

    Box(modifier = modifier.fillMaxSize()) {
        // Floating Recomposition HUD Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .clip(CircleShape)
                .background(
                    if (state.globalEnabled) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { isExpanded = true }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔥 Recompositions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (state.globalEnabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                )

                Surface(
                    shape = CircleShape,
                    color = if (state.globalEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                ) {
                    Text(
                        text = "${state.totalRecompositions}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (topHotspot != null && state.globalEnabled) {
                    Text(
                        text = "• ${topHotspot.tag} (${topHotspot.totalCount}x)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Full Screen Inspector Bottom Sheet
        if (isExpanded) {
            ModalBottomSheet(
                onDismissRequest = { isExpanded = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recomposition Heatmap",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Cycles: ${state.totalRecompositions} across ${state.hotspots.size} Nodes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        TextButton(onClick = { isExpanded = false }) {
                            Text("Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Highlighter", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = state.globalEnabled, onCheckedChange = onGlobalToggle)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Count Badges", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(checked = state.showCountBadges, onCheckedChange = onShowBadgesToggle)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Export & Reset Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = {
                                    val csv = onExportCsv()
                                    clipboardManager.setText(AnnotatedString(csv))
                                    exportToast = "CSV copied to clipboard!"
                                }
                            ) {
                                Text("Copy CSV", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = {
                                    val json = onExportJson()
                                    clipboardManager.setText(AnnotatedString(json))
                                    exportToast = "JSON copied to clipboard!"
                                }
                            ) {
                                Text("Copy JSON", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = onReset,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Reset All", fontSize = 12.sp)
                        }
                    }

                    if (exportToast != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = exportToast!!,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Recomposition Hotspots Leaderboard:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Leaderboard
                    HotspotLeaderboardView(
                        hotspots = sortedHotspots,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// PREVIEWS (Following sfc-android-compose-previews standards)
// ============================================================================

@PreviewLightDark
@Composable
private fun ComposeRecompositionOverlay_Populated_Preview() {
    MaterialTheme {
        Surface {
            ComposeRecompositionOverlayContent(
                state = RecompositionRegistryState(
                    globalEnabled = true,
                    showCountBadges = true,
                    totalRecompositions = 28,
                    hotspots = mapOf(
                        "UserProfileCard" to ComposableHotspot("UserProfileCard", 16, 4.2, System.currentTimeMillis(), RecompositionSeverity.CRITICAL),
                        "ProductListRow" to ComposableHotspot("ProductListRow", 8, 1.8, System.currentTimeMillis(), RecompositionSeverity.HIGH),
                        "HeaderTitle" to ComposableHotspot("HeaderTitle", 2, 0.2, System.currentTimeMillis(), RecompositionSeverity.OPTIMAL)
                    )
                ),
                onGlobalToggle = {},
                onShowBadgesToggle = {},
                onReset = {},
                onExportCsv = { "" },
                onExportJson = { "" }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ComposeRecompositionOverlay_Empty_Preview() {
    MaterialTheme {
        Surface {
            ComposeRecompositionOverlayContent(
                state = RecompositionRegistryState(),
                onGlobalToggle = {},
                onShowBadgesToggle = {},
                onReset = {},
                onExportCsv = { "" },
                onExportJson = { "" }
            )
        }
    }
}
