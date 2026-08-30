package io.github.zakayothuku.recompositionhighlighter.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zakayothuku.recompositionhighlighter.recompositionHighlighter
import io.github.zakayothuku.recompositionhighlighter.ui.ComposeRecompositionOverlay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SampleRecompositionContent()

                        // Attach floating Recomposition Highlighter Overlay
                        ComposeRecompositionOverlay()
                    }
                }
            }
        }
    }
}

@Composable
fun SampleRecompositionContent() {
    var counterA by remember { mutableIntStateOf(0) }
    var counterB by remember { mutableIntStateOf(0) }
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🔥 Recomposition Highlighter",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.recompositionHighlighter("HeaderTitle")
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Borders flash 🟢 Green (1-2x), 🟡 Yellow (3-5x), 🟠 Orange (6-9x), 🔴 Red (10x+).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Scenario 1: Stable Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .recompositionHighlighter("OptimizedStableCard"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🟢 1. Isolated State Composable",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Only this card recomposes when button is tapped.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Taps: $counterA", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Button(
                        onClick = { counterA++ },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+1 Increment", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scenario 2: Unstable Parent/Child Cascade
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .recompositionHighlighter("CascadeRecompCard"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🟡 2. Parent-Child Cascade Recompositions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Typing in textfield triggers frequent child recompositions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Type here to test typing recompositions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .recompositionHighlighter("TextFieldNode"),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scenario 3: High Recomposition Spikes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .recompositionHighlighter("RapidSpikeCard"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🔴 3. Rapid Recomposition Stress Spikes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Simulates high-velocity recomposition burst (flashes red!).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Burst Count: $counterB", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Button(
                        onClick = { counterB += 12 },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("⚡ Burst 12x", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}
