package com.example.helper_ghost.data.remote

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextExtractor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun extractTextFromBitmap (bitmap: Bitmap, onResult: (String)->Unit){
        val image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image).addOnSuccessListener { visionText ->
            val resultText = visionText.textBlocks.joinToString(separator = "\n"){ it.text }
            onResult(resultText);
        }.addOnFailureListener { e ->
            onResult("Error: ${e.message}")
        }
    }
}