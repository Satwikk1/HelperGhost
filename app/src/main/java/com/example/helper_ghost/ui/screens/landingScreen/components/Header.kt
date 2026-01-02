package com.example.helper_ghost.ui.screens.landingScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun Header(title: String, subtitle: String, modifier: Modifier = Modifier){
    Column(modifier = modifier.padding(16.dp).fillMaxWidth()) {
        Text(text = title, modifier = modifier, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
        Text(text = subtitle, modifier = modifier, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
    }
}


@Preview(showBackground = true)
@Composable
fun HeaderPreview(){
    Header(title = "Title", subtitle = "Subtitle")
}