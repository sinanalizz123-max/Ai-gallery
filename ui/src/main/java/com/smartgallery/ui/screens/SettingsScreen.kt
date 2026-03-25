package com.smartgallery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartgallery.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
    val viewModel: SettingsViewModel = viewModel()
    val detectionEnabled by viewModel.detectionEnabled.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable face detection")
            Switch(checked = detectionEnabled, onCheckedChange = viewModel::setDetectionEnabled)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Privacy mode (local only)")
            Switch(checked = privacyMode, onCheckedChange = viewModel::setPrivacyMode)
        }

        Button(onClick = { }) { Text("Re-scan gallery") }
        Button(onClick = { }) { Text("Clear AI data") }
    }
}
