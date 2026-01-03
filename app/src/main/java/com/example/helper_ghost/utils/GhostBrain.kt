package com.example.helper_ghost.utils

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object GhostBrain {
    private var llmInference: LlmInference? = null
    private var session: LlmInferenceSession? = null

    fun initialize(context: Context) {
        val engineOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath("/data/local/tmp/llm/model.bin")
            .setMaxTokens(512)
            .build()

        llmInference = LlmInference.createFromOptions(context, engineOptions)

        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTemperature(0.7f)
            .build()

        session = LlmInferenceSession.createFromOptions(llmInference!!, sessionOptions)
    }

    fun getSmartSuggestion(chatContext: String, onResult: (String) -> Unit) {
        val prompt = """
            You are a witty ghost assistant. Based on this chat: "$chatContext", 
            suggest one short, clever reply that sounds natural and cool.
        """.trimIndent()

        CoroutineScope(Dispatchers.IO).launch {
            val response = llmInference?.generateResponse(prompt) ?: "👻 I'm thinking..."
            withContext(Dispatchers.Main) {
                onResult(response)
            }
        }
    }
}