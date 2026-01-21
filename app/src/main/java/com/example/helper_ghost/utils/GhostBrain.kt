package com.example.helper_ghost.utils

import android.content.Context
import android.util.Log
import com.example.helper_ghost.ui.components.Suggestion
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object GhostBrain {
    private var llmInference: LlmInference? = null
    private val inferenceMutex = Mutex()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var isInitializing = false

    fun initialize(context: Context) {
        if (llmInference != null || isInitializing) return
        isInitializing = true
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val engineOptions = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath("/data/local/tmp/llm/model.bin")
                    .setMaxTokens(2048)
                    .build()

                llmInference = LlmInference.createFromOptions(context, engineOptions)
                _isReady.value = true
                Log.d("GhostBrain", "LLM Initialized successfully")
            } catch (e: Exception) {
                Log.e("GhostBrain", "Failed to initialize LLM: ${e.message}")
            } finally {
                isInitializing = false
            }
        }
    }

    fun getSmartSuggestions(
        chatContext: String,
        selectedPersonas: Set<String>,
        onResult: (List<Suggestion>) -> Unit
    ) {
        val personasStr = selectedPersonas.joinToString(", ")
        val prompt = """
            You are a ghost assistant with the following personas: $personasStr. 
            Based on this chat context: "$chatContext", 
            suggest 3 different clever replies. 
            
            Return the output strictly as a JSON array of objects with "title" and "description" keys.
            Example: [{"title": "Quick Reply", "description": "Sounds good!"}, ...]
            
            provide the reply in the same language as the text is in.
            
            Keep descriptions under 15 words.
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            if (inferenceMutex.isLocked) {
                Log.w("GhostBrain", "Inference is busy, skipping this request.")
                return@launch
            }

            inferenceMutex.withLock {
                val inference = llmInference
                if (inference == null) {
                    withContext(Dispatchers.Main) { 
                        onResult(emptyList()) 
                    }
                } else {
                    try {
                        val response = inference.generateResponse(prompt)
                        Log.d("GhostBrain", "LLM RAW Response: $response")
                        
                        val suggestions = parseSuggestions(response)
                        withContext(Dispatchers.Main) {
                            onResult(suggestions)
                        }
                    } catch (e: Exception) {
                        Log.e("GhostBrain", "Inference error: ${e.message}")
                        withContext(Dispatchers.Main) {
                            onResult(emptyList())
                        }
                    }
                }
            }
        }
    }

    private fun parseSuggestions(rawResponse: String): List<Suggestion> {
        return try {
            val cleaned = rawResponse.substringAfter("[").substringBeforeLast("]")
            val jsonStr = "[$cleaned]"
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<Suggestion>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Suggestion(
                        title = obj.optString("title", "Suggestion"),
                        description = obj.optString("description", "")
                    )
                )
            }
            list.take(3)
        } catch (e: Exception) {
            Log.e("GhostBrain", "Parse error: ${e.message}")
            listOf(Suggestion("Quick Ghost", rawResponse.take(50)))
        }
    }
}
