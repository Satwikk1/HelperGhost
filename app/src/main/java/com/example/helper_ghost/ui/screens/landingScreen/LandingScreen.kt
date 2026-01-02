package com.example.helper_ghost.ui.screens.landingScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.R
import com.example.helper_ghost.ui.screens.landingScreen.components.Header
import com.example.helper_ghost.ui.screens.landingScreen.components.ShowStatusCard

@Composable
fun LandingScreen(){
    Column(modifier = Modifier.padding(16.dp)) {
        Header(
            title = stringResource(id = R.string.app_name),
            subtitle = stringResource(id = R.string.landing_subtitle)
        )
        ShowStatusCard()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LandingScreen()
}