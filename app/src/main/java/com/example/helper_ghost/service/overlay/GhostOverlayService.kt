package com.example.helper_ghost.service.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.helper_ghost.R
import com.example.helper_ghost.ui.components.GhostFloatingBubble
import com.example.helper_ghost.utils.GhostBrain
import com.example.helper_ghost.utils.GhostServiceManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GhostOverlayService : LifecycleService(), SavedStateRegistryOwner {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var ghostView: ComposeView? = null
    private val CHANNEL_ID = "ghost_service_channel"
    private val NOTIFICATION_ID = 1

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    
    // UI State
    private var isExpanded by mutableStateOf(false)
    
    // Window Position (Actual coordinates of the Overlay Window)
    private var windowX by mutableStateOf(0)
    private var windowY by mutableStateOf(0)
    
    // Bubble Position (Screen coordinates - used as anchor)
    private var bubbleX by mutableStateOf(0f)
    private var bubbleY by mutableStateOf(0f)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        createNotificationChannel()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val metrics = resources.displayMetrics
        val marginPx = (16 * metrics.density).toInt()
        val bWidth = (140 * metrics.density).toInt()
        val bHeight = (48 * metrics.density).toInt()
        
        // Start bottom-right
        bubbleX = (metrics.widthPixels - bWidth - marginPx).toFloat()
        bubbleY = (metrics.heightPixels - bHeight - marginPx).toFloat()
        
        windowX = bubbleX.toInt()
        windowY = bubbleY.toInt()

        CoroutineScope(Dispatchers.IO).launch {
            GhostBrain.initialize(this@GhostOverlayService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val resultCode = intent?.getIntExtra(GhostServiceManager.EXTRA_RESULT_CODE, -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>(GhostServiceManager.EXTRA_DATA)

        if (resultCode == Activity.RESULT_OK && data != null) {
            startForeground(NOTIFICATION_ID, createNotification())
            val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            setupCaptureEngine()
            showGhostBubble()
        } else {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showGhostBubble() {
        val metrics = resources.displayMetrics
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = windowX
            y = windowY
        }

        ghostView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GhostOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GhostOverlayService)

            setContent {
                GhostFloatingBubble(
                    bubbleX = bubbleX,
                    bubbleY = bubbleY,
                    windowX = windowX,
                    windowY = windowY,
                    screenWidth = metrics.widthPixels,
                    screenHeight = metrics.heightPixels,
                    isExpandedProp = isExpanded,
                    onExpandChanged = { expanded -> toggleExpansion(expanded) },
                    onPositionUpdate = { bX, bY, _, _ ->
                        if (!isExpanded) {
                            bubbleX = bX.toFloat()
                            bubbleY = bY.toFloat()
                            windowX = bX
                            windowY = bY
                            params.x = windowX
                            params.y = windowY
                            windowManager.updateViewLayout(this@apply, params)
                        }
                    },
                    onTriggerScreenshot = { captureScreenAndSuggest() }
                )
            }
        }
        windowManager.addView(ghostView, params)
    }

    private fun toggleExpansion(expanded: Boolean) {
        if (expanded) {
            // Expand window to full screen
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.x = 0
            params.y = 0
            windowX = 0
            windowY = 0
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND
            params.dimAmount = 0.7f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                params.blurBehindRadius = 70
            }
        } else {
            // Shrink window back to bubble size
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            windowX = bubbleX.toInt()
            windowY = bubbleY.toInt()
            params.x = windowX
            params.y = windowY
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            params.dimAmount = 0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) params.blurBehindRadius = 0
        }
        
        isExpanded = expanded
        windowManager.updateViewLayout(ghostView, params)
    }

    private fun setupCaptureEngine() {
        val metrics = resources.displayMetrics
        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
        val callback = object : MediaProjection.Callback() { override fun onStop() { cleanupCaptureResources() } }
        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "GhostCapture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader?.surface, null, null
        )
    }

    private fun captureScreenAndSuggest() {
        val image = imageReader?.acquireLatestImage() ?: return
        try {
            val bitmap = imageToBitmap(image)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(InputImage.fromBitmap(bitmap, 0)).addOnSuccessListener { visionText ->
                if (visionText.text.isNotBlank()) {
                    GhostBrain.getSmartSuggestion(visionText.text) { suggestion ->
                        Log.d("GhostAI", "Suggested: $suggestion")
                    }
                }
            }
        } finally { image.close() }
    }

    private fun imageToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ghost is Active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Ghost Service Channel", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(serviceChannel)
        }
    }

    private fun cleanupCaptureResources() {
        virtualDisplay?.release()
        imageReader?.close()
        virtualDisplay = null
        imageReader = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupCaptureResources()
        mediaProjection?.stop()
        ghostView?.let { windowManager.removeView(it) }
    }
}
