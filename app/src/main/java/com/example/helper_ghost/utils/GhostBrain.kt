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
        val titles = mutableListOf<String>()
        val descriptions = mutableListOf<String>()

        // Regex to find values for "title" and "description" keys
        // Handles cases where LLM might omit commas or leave JSON unterminated
        val titleRegex = Regex("\"title\"\\s*:\\s*\"([^\"]*)\"")
        val descRegex = Regex("\"description\"\\s*:\\s*\"([^\"]*)\"")

        titleRegex.findAll(rawResponse).forEach { match ->
            titles.add(match.groupValues[1])
        }

        descRegex.findAll(rawResponse).forEach { match ->
            descriptions.add(match.groupValues[1])
        }

        val list = mutableListOf<Suggestion>()
        val count = minOf(titles.size, descriptions.size)
        
        for (i in 0 until count) {
            list.add(
                Suggestion(
                    title = titles[i],
                    description = descriptions[i]
                )
            )
        }

        if (list.isEmpty()) {
            Log.w("GhostBrain", "Regex parsing found no suggestions. Raw response: $rawResponse")
            // Ultimate fallback: treat the whole response as one suggestion description if it's not too long
            val fallbackTitle = "Quick Ghost"
            val fallbackDesc = rawResponse.take(100).replace("\n", " ")
            return listOf(Suggestion(fallbackTitle, fallbackDesc))
        }

        return list.take(3)
    }
}
