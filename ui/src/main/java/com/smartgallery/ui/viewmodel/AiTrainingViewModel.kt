package com.smartgallery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartgallery.ai.engine.SmartAiEngine
import com.smartgallery.ai.model.TrainingPair
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiTrainingViewModel(
    private val aiEngine: SmartAiEngine
) : ViewModel() {
    val trainingQueue: StateFlow<List<TrainingPair>> = aiEngine.trainingQueue
    val stats = aiEngine.stats

    fun onAnswer(pair: TrainingPair, samePerson: Boolean) {
        viewModelScope.launch {
            aiEngine.submitFeedback(pair, samePerson)
        }
    }
}
