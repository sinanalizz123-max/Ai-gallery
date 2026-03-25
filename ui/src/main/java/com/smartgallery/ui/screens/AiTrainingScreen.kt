package com.smartgallery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.ui.components.AiStatsRow
import com.smartgallery.ui.components.GlassSurface
import com.smartgallery.ui.components.TrainingCard
import com.smartgallery.ui.viewmodel.AiTrainingViewModel

@Composable
fun AiTrainingScreen(
    paddingValues: PaddingValues,
    aiEngine: SmartAiEngine,
    onOpenSettings: () -> Unit
) {
    val viewModel = viewModel(factory = AiTrainingViewModelFactory(aiEngine))
    val queue by viewModel.trainingQueue.collectAsState()
    val stats by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "AI Training", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onOpenSettings) { Text("Settings") }
        }

        AiStatsRow(stats = stats)

        GlassSurface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Improve accuracy while you relax",
                    style = MaterialTheme.typography.bodyLarge
                )
                val current = queue.firstOrNull()
                if (current != null) {
                    TrainingCard(
                        pair = current,
                        onYes = { viewModel.onAnswer(current, true) },
                        onNo = { viewModel.onAnswer(current, false) },
                        onSkip = { viewModel.onAnswer(current, false) }
                    )
                } else {
                    Text(text = "No training tasks right now.")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

private class AiTrainingViewModelFactory(
    private val aiEngine: SmartAiEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiTrainingViewModel(aiEngine) as T
    }
}
