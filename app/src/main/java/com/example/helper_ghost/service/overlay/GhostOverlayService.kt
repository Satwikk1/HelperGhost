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

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        createNotificationChannel()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("GhostAI", "Loading Gemma model... please wait.")
                GhostBrain.initialize(this@GhostOverlayService)
                Log.d("GhostAI", "Gemma is ready to think! 🧠")
            } catch (e: Exception) {
                Log.e("GhostAI", "Failed to load model: ${e.message}")
            }
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

    private fun setupCaptureEngine() {
        val metrics = resources.displayMetrics

        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )

        val callback = object : MediaProjection.Callback() {
            override fun onStop() {
                cleanupCaptureResources()
            }
        }
        mediaProjection?.registerCallback(callback, Handler(Looper.getMainLooper()))

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "GhostCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun processImageWithMLKit(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text
                if (recognizedText.isNotBlank()) {
                    Log.d("GhostAI", "Recognized Text: $recognizedText")

                    GhostBrain.getSmartSuggestion(recognizedText) { suggestion ->
                        updateGhostUI(suggestion)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GhostAI", "Text recognition failed: ${e.message}")
            }
    }

    private fun updateGhostUI(suggestion: String) {
        Log.d("GhostAI", "Suggested Reply: $suggestion")
    }

    private fun captureScreenAndSuggest() {
        Log.d("GhostOverlayService", "captureScreenAndSuggest called")

        val image = imageReader?.acquireLatestImage()
        if (image != null) {
            try {
                val bitmap = imageToBitmap(image)

                processImageWithMLKit(bitmap)

            } catch (e: Exception) {
                Log.e("GhostOverlayService", "Capture processing failed: ${e.message}")
            } finally {
                image.close()
            }
        } else {
            Log.e("GhostOverlayService", "No image available in buffer")
        }
    }

    private fun imageToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun showGhostBubble() {
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        ghostView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@GhostOverlayService)
            setViewTreeSavedStateRegistryOwner(this@GhostOverlayService)

            setContent {
                GhostFloatingBubble(onTriggerScreenshot = { captureScreenAndSuggest() })
            }
        }

        windowManager.addView(ghostView, params)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ghost is Active")
            .setContentText("Tap the ghost for suggestions")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Ghost Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun cleanupCaptureResources() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupCaptureResources()
        mediaProjection?.stop()

        ghostView?.let {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
        }
    }
}