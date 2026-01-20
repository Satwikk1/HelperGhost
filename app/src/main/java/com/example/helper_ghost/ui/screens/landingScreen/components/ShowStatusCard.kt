package com.example.helper_ghost.ui.screens.landingScreen.components

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsPower
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.ui.theme.AppGradients
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

    Card(modifier = modifier.fillMaxWidth(), border = BorderStroke(1.dp, AppColors.Orange.lighter), shape = RoundedCornerShape(24.dp)) {
      Box(
          modifier = Modifier.background(
              brush = Brush.linearGradient(
                  colors = AppGradients.serviceCardBackground,
                  start = Offset(10f, 10f),
                  end = Offset.Infinite
              )
          )
      ){
          Column(modifier = Modifier.padding(24.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
              ) {
                  Column {
                      Text(text = "Service Status", style = MaterialTheme.typography.titleLarge, color = AppColors.white, modifier=Modifier.padding(bottom = 6.dp))
                      Text(text = if (checked) "Active and monitoring" else "Service is inactive", style = MaterialTheme.typography.bodyMedium, color = AppColors.white.copy(alpha = 0.8f))
                  }
                  Row(verticalAlignment = Alignment.CenterVertically) {
                      if(checked){
                          Icon(
                              Icons.Filled.SettingsPower,
                              contentDescription = "Power",
                              tint = AppColors.Emerald.vibrant,
                              modifier = Modifier.padding(end = 8.dp )
                          )
                      }
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
                          },
                          colors = SwitchDefaults.colors(
                              checkedThumbColor = AppColors.Emerald.vibrant,
                              checkedTrackColor = AppColors.Emerald.light.copy(alpha = 0.5f),
                              checkedBorderColor = Color.Transparent,
                              uncheckedThumbColor = AppColors.white.copy(alpha = 0.9f),
                              uncheckedTrackColor = AppColors.white.copy(alpha = 0.2f),
                              uncheckedBorderColor = AppColors.white.copy(alpha = 0.4f)
                          )
                      )
                  }
              }
              Spacer(modifier = Modifier.size(16.dp))
              if(checked){
                  Box(modifier = Modifier
                      .border(width = 1.dp, color = AppColors.Emerald.vibrant, shape = RoundedCornerShape(25))
                      .clip(RoundedCornerShape(25))
                  ) {
                      Box(modifier=Modifier.background(AppColors.Emerald.vibrant.copy(0.3f))){
                          Row(verticalAlignment = Alignment.CenterVertically, modifier=Modifier.padding(horizontal = 12.dp, vertical = 6.dp)){
                              Box(
                                  modifier = Modifier
                                      .size(8.dp)
                                      .clip(CircleShape)
                                      .background(AppColors.Emerald.vibrant)
                              )
                              Spacer(modifier = Modifier.size(8.dp))
                              Text(text = "Listening in background", style = MaterialTheme.typography.bodySmall, color = AppColors.Emerald.medium)
                          }
                      }

                  }
              }
          }
      }
    }
}
