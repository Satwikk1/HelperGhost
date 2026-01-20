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
import com.example.helper_ghost.ui.components.GradientText
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.ui.theme.AppGradients

@Composable
fun Header(title: String, subtitle: String, modifier: Modifier = Modifier){
    Column(modifier = modifier.padding(bottom = 28.dp).fillMaxWidth()) {
        GradientText(
            text = title, 
            fontStyle = MaterialTheme.typography.displayLarge, 
            colorsList = AppGradients.title
        )
        Text(
            text = subtitle, 
            modifier = Modifier.padding(top = 8.dp), 
            color = AppColors.Purple.deep,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HeaderPreview(){
    Header(title = "Helper Ghost", subtitle = "Your intelligent companion")
}