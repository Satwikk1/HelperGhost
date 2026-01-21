package com.example.helper_ghost.ui.screens.landingScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.R
import com.example.helper_ghost.ui.screens.landingScreen.components.Header
import com.example.helper_ghost.ui.screens.landingScreen.components.PersonaSection
import com.example.helper_ghost.ui.screens.landingScreen.components.ShowStatusCard
import com.example.helper_ghost.ui.screens.landingScreen.components.StatsSection
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.ui.theme.AppGradients

@Composable
fun LandingScreen() {
    var selectedPersonas by remember { mutableStateOf(setOf("Executive")) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(AppGradients.pageBackground))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Header(
                title = stringResource(id = R.string.app_name),
                subtitle = stringResource(id = R.string.landing_subtitle)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShowStatusCard(selectedPersonas = selectedPersonas)
            Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
                Text(
                    text = stringResource(id = R.string.select_personas_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.Purple.deep,
                    modifier = Modifier.padding(top = 28.dp)
                )
                Text(
                    text = stringResource(id = R.string.active_personas_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Purple.medium,
                    modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                )
            }
            PersonaSection(
                selectedPersonas = selectedPersonas,
                onPersonaToggle = { persona ->
                    selectedPersonas = if (selectedPersonas.contains(persona)) {
                        selectedPersonas - persona
                    } else {
                        selectedPersonas + persona
                    }
                }
            )
            StatsSection(modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LandingScreen()
}
