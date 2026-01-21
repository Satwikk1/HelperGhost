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
import java.io.File
import java.io.FileOutputStream

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
                val modelDir = File(context.filesDir, "llm")
                if (!modelDir.exists()) modelDir.mkdirs()
                val modelFile = File(modelDir, "gemma-2b-model.bin")
                if (!modelFile.exists()) {
                    copyModelFromAssets(context, "gemma-2b-model.bin", modelFile)
                }

                val engineOptions = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
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

    private fun copyModelFromAssets(context: Context, assetName: String, targetFile: File) {
        context.assets.open(assetName).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getSmartSuggestions(
        chatContext: String,
        selectedPersonas: Set<String>,
        onResult: (List<Suggestion>) -> Unit
    ) {
        val cleanContext = sanitizeContext(chatContext)
        if (cleanContext.isBlank()) {
            onResult(emptyList())
            return
        }

        val personasStr = selectedPersonas.joinToString(", ")
        val prompt = """
            Personas: $personasStr
            Chat Context: "$cleanContext"
            
            Task: Suggest 3 clever replies in the same language as the context.
            Structure: Return ONLY a JSON array of 3 objects with "title" and "description".
            Constraint: Description < 15 words. No markdown.
            
            Response:
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            if (inferenceMutex.isLocked) return@launch

            inferenceMutex.withLock {
                val inference = llmInference
                if (inference == null) {
                    withContext(Dispatchers.Main) { onResult(emptyList()) }
                } else {
                    try {
                        val response = inference.generateResponse(prompt)
                        Log.d("GhostBrain", "LLM RAW: $response")
                        val suggestions = parseSuggestions(response)
                        withContext(Dispatchers.Main) { onResult(suggestions) }
                    } catch (e: Exception) {
                        Log.e("GhostBrain", "Inference error: ${e.message}")
                        withContext(Dispatchers.Main) { onResult(emptyList()) }
                    }
                }
            }
        }
    }

    private fun sanitizeContext(raw: String): String {
        // Simple heuristic: remove lines that look like timestamps or single icons
        return raw.lines()
            .map { it.trim() }
            .filter { line ->
                line.length > 2 && 
                !line.contains(Regex("^\\d{1,2}:\\d{2}")) && // Remove 12:09
                !line.contains(Regex("^\\d+%$")) // Remove 80%
            }
            .joinToString(" ")
            .takeLast(500) // Keep only recent history
    }

    private fun parseSuggestions(rawResponse: String): List<Suggestion> {
        val titles = mutableListOf<String>()
        val descriptions = mutableListOf<String>()

        val titleRegex = Regex("\"title\"\\s*:\\s*\"([^\"]*)\"")
        val descRegex = Regex("\"description\"\\s*:\\s*\"([^\"]*)\"")

        titleRegex.findAll(rawResponse).forEach { titles.add(it.groupValues[1]) }
        descRegex.findAll(rawResponse).forEach { descriptions.add(it.groupValues[1]) }

        val list = mutableListOf<Suggestion>()
        val count = minOf(titles.size, descriptions.size)
        for (i in 0 until count) {
            list.add(Suggestion(title = titles[i], description = descriptions[i]))
        }

        if (list.isEmpty() && rawResponse.isNotBlank()) {
            return listOf(Suggestion("Ghost Reply", rawResponse.take(100).replace("\n", " ")))
        }
        return list.take(3)
    }
}
