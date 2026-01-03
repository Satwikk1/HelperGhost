package com.example.helper_ghost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.helper_ghost.ui.screens.landingScreen.LandingScreen
import com.example.helper_ghost.ui.theme.GhostTheme
import com.example.helper_ghost.utils.GhostServiceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhostTheme {
                LandingScreen()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        GhostServiceManager.stopService(this)
    }
}