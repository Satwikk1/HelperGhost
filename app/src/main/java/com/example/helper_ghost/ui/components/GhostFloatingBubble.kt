package com.example.helper_ghost.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helper_ghost.ui.theme.AppColors
import com.example.helper_ghost.utils.GhostBrain
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class Suggestion(
    val title: String,
    val description: String,
    val tag: String = "Auto-Decide"
)

@Composable
fun GhostFloatingBubble(
    bubbleX: Float,
    bubbleY: Float,
    windowX: Int,
    windowY: Int,
    screenWidth: Int,
    screenHeight: Int,
    isExpandedProp: Boolean,
    selectedPersonas: Set<String> = setOf("Witty"),
    onExpandChanged: (Boolean) -> Unit,
    onPositionUpdate: (x: Int, y: Int, isRight: Boolean, isBottom: Boolean) -> Unit,
    onTriggerScreenshot: (onTextRecognized: (String) -> Unit) -> Unit
) {
    val density = LocalDensity.current
    val bubbleWidthPx = with(density) { 140.dp.toPx() }
    val bubbleHeightPx = with(density) { 48.dp.toPx() }
    val marginPx = with(density) { 16.dp.toPx() }

    val offset = remember { Animatable(Offset(bubbleX, bubbleY), Offset.VectorConverter) }
    val velocityTracker = remember { VelocityTracker() }
    val scope = rememberCoroutineScope()
    
    var popupHeight by remember { mutableStateOf(0) }
    
    // LLM Related State
    var isLLMLoading by remember { mutableStateOf(false) }
    var suggestionList by remember { mutableStateOf<List<Suggestion>>(emptyList()) }

    LaunchedEffect(bubbleX, bubbleY) {
        if (!offset.isRunning) offset.snapTo(Offset(bubbleX, bubbleY))
    }

    LaunchedEffect(offset.value) {
        val isRight = offset.value.x > (screenWidth / 2)
        val isBottom = offset.value.y > (screenHeight / 2)
        onPositionUpdate(offset.value.x.roundToInt(), offset.value.y.roundToInt(), isRight, isBottom)
    }

    val isSnappedToRight by remember(isExpandedProp) { 
        mutableStateOf(offset.value.x > screenWidth / 2) 
    }
    val isSnappedToBottom by remember(isExpandedProp) { 
        mutableStateOf(offset.value.y > screenHeight / 2) 
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isExpandedProp) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).clickable { onExpandChanged(false) })
        }

        // POSITIONED Popup
        Box(
            modifier = Modifier
                .offset {
                    val bX = offset.value.x
                    val bY = offset.value.y
                    if (isExpandedProp) {
                        val pX = if (isSnappedToRight) bX - (340.dp.toPx() - bubbleWidthPx) else bX
                        val pY = if (isSnappedToBottom) bY - popupHeight - with(density) { 16.dp.toPx() } else bY + bubbleHeightPx + with(density) { 16.dp.toPx() }
                        IntOffset((pX - windowX).roundToInt(), (pY - windowY).roundToInt())
                    } else {
                        IntOffset((bX - windowX).roundToInt(), (bY - windowY).roundToInt())
                    }
                }
                .onGloballyPositioned { popupHeight = it.size.height }
        ) {
            AnimatedVisibility(
                visible = isExpandedProp,
                enter = fadeIn() + scaleIn(
                    initialScale = 0f,
                    transformOrigin = TransformOrigin(if (isSnappedToRight) 1f else 0f, if (isSnappedToBottom) 1f else 0f),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut() + scaleOut(
                    targetScale = 0f,
                    transformOrigin = TransformOrigin(if (isSnappedToRight) 1f else 0f, if (isSnappedToBottom) 1f else 0f),
                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            ) {
                ExpandedSuggestionView(
                    suggestions = suggestionList,
                    isLoading = isLLMLoading,
                    onClose = { onExpandChanged(false) }
                )
            }
        }

        // FIXED Bubble
        Box(
            modifier = Modifier
                .offset {
                    IntOffset((offset.value.x - windowX).roundToInt(), (offset.value.y - windowY).roundToInt())
                }
                .pointerInput(isExpandedProp) {
                    if (!isExpandedProp) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                velocityTracker.addPointerInputChange(change)
                                scope.launch {
                                    val newX = (offset.value.x + dragAmount.x).coerceIn(0f, screenWidth - bubbleWidthPx)
                                    val newY = (offset.value.y + dragAmount.y).coerceIn(0f, screenHeight - bubbleHeightPx)
                                    offset.snapTo(Offset(newX, newY))
                                }
                            },
                            onDragEnd = {
                                val velocity = velocityTracker.calculateVelocity()
                                val targetX = if (offset.value.x + bubbleWidthPx / 2 < screenWidth / 2) marginPx else screenWidth - bubbleWidthPx - marginPx
                                val targetY = if (offset.value.y + bubbleHeightPx / 2 < screenHeight / 2) marginPx else screenHeight - bubbleHeightPx - marginPx
                                scope.launch {
                                    offset.animateTo(
                                        targetValue = Offset(targetX, targetY),
                                        initialVelocity = Offset(velocity.x, velocity.y),
                                        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                                    )
                                }
                                velocityTracker.resetTracking()
                            }
                        )
                    }
                }
                .clip(CircleShape)
                .background(brush = Brush.linearGradient(listOf(AppColors.Purple.medium, AppColors.Purple.deep)))
                .clickable {
                    if (!isExpandedProp) {
                        // Start LLM Loading
                        isLLMLoading = true
                        suggestionList = emptyList()
                        onTriggerScreenshot { recognizedText ->
                            GhostBrain.getSmartSuggestions(recognizedText, selectedPersonas) { results ->
                                suggestionList = results
                                isLLMLoading = false
                            }
                        }
                    }
                    onExpandChanged(!isExpandedProp)
                }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("Ghost", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(AppColors.Pink.hot), contentAlignment = Alignment.Center) {
                    Text("${if (suggestionList.isNotEmpty()) suggestionList.size else 3}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExpandedSuggestionView(
    suggestions: List<Suggestion>,
    isLoading: Boolean,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.width(340.dp).wrapContentHeight(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Smart Suggestions", style = MaterialTheme.typography.titleLarge, color = AppColors.Purple.deep, fontWeight = FontWeight.Bold)
                    Text("Review and approve actions", style = MaterialTheme.typography.bodyMedium, color = AppColors.Purple.light)
                }
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = AppColors.Purple.light) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ShimmerSuggestionCard() }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 400.dp)) {
                    items(suggestions) { SuggestionCard(it) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("View all activity →", modifier = Modifier.align(Alignment.CenterHorizontally), color = AppColors.Purple.medium, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun ShimmerSuggestionCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = alpha))
    ) {}
}

@Composable
fun SuggestionCard(suggestion: Suggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBFF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Purple.lightest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(color = AppColors.Purple.tint, shape = RoundedCornerShape(8.dp)) {
                Text(suggestion.tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = AppColors.Purple.medium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(suggestion.title, fontWeight = FontWeight.Bold, color = AppColors.Purple.deep)
            Text(suggestion.description, style = MaterialTheme.typography.bodySmall, color = AppColors.Purple.medium, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1.5f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple.medium), shape = RoundedCornerShape(12.dp)) { Text("✓ Accept", fontSize = 12.sp) }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Purple.lightest)) { Text("Dismiss", fontSize = 12.sp, color = AppColors.Purple.medium) }
            }
        }
    }
}
