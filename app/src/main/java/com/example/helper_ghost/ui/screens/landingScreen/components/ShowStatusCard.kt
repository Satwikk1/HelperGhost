package com.example.helper_ghost.ui.screens.landingScreen.components

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsPower
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.utils.GhostServiceManager

@Composable
fun ShowStatusCard(modifier: Modifier = Modifier) {
    var checked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val projectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val captureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            GhostServiceManager.startService(context = context, resultCode = result.resultCode, data = result.data!!)
            checked = true
        }else{
            GhostServiceManager.stopService(context)
            checked = false
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Service Status", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Active and monitoring", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.SettingsPower,
                        contentDescription = "Power",
                        tint = Color(0xFF008000)
                    )
                    Switch(
                        checked = checked,
                        onCheckedChange = {isChecked ->
                            if(isChecked){
                                if (!GhostServiceManager.hasOverlayPermission(context)) {
                                    GhostServiceManager.requestOverlayPermission(context)
                                    checked = false
                                }else{
                                    captureLauncher.launch(projectionManager.createScreenCaptureIntent())
                                }
                            }else{
                                checked = false
                                GhostServiceManager.stopService(context)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF008000))
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Listening in background", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
