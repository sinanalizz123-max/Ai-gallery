package com.smartgallery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartgallery.data.model.AiStats

@Composable
fun AiStatsRow(stats: AiStats) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatChip(label = "Accuracy", value = "${stats.accuracyPercent}%")
        StatChip(label = "Learned", value = stats.learnedFaces.toString())
        StatChip(label = "Corrections", value = stats.recentCorrections.toString())
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = "$label: ", style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.labelMedium)
        }
    }
}
