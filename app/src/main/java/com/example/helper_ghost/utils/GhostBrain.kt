package com.example.helper_ghost.utils

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object GhostBrain {
    private var llmInference: LlmInference? = null
    private val inferenceMutex = Mutex()
    private var isInitializing = false

    fun initialize(context: Context) {
        if (llmInference != null || isInitializing) return
        isInitializing = true
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val engineOptions = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath("/data/local/tmp/llm/model.bin")
                    .setMaxTokens(512)
                    .build()

                llmInference = LlmInference.createFromOptions(context, engineOptions)
                Log.d("GhostBrain", "LLM Initialized successfully")
            } catch (e: Exception) {
                Log.e("GhostBrain", "Failed to initialize LLM: ${e.message}")
            } finally {
                isInitializing = false
            }
        }
    }

    fun getSmartSuggestion(chatContext: String, onResult: (String) -> Unit) {
        val prompt = """
            You are a witty ghost assistant. Based on this chat context: "$chatContext", 
            suggest one short, clever reply that sounds natural and cool.
            Keep it under 15 words.
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            // Use a Mutex to prevent overlapping JNI calls which cause the MediaPipe crash
            if (inferenceMutex.isLocked) {
                Log.w("GhostBrain", "Inference is busy, skipping this request.")
                return@launch
            }

            inferenceMutex.withLock {
                val inference = llmInference
                if (inference == null) {
                    withContext(Dispatchers.Main) { onResult("Ghost is still waking up... 👻") }
                } else {
                    try {
                        val response = inference.generateResponse(prompt)
                        withContext(Dispatchers.Main) {
                            onResult(response)
                        }
                    } catch (e: Exception) {
                        Log.e("GhostBrain", "Inference error: ${e.message}")
                        withContext(Dispatchers.Main) {
                            onResult("Ghost got a brain freeze! ❄️")
                        }
                    }
                }
            }
        }
    }
}
