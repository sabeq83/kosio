package com.kosio.app.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kosio.app.domain.ocr.ParsedReceiptResult
import com.kosio.app.domain.ocr.ReceiptTextParser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ReceiptOcrScanner {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun scanReceiptFromUri(context: Context, imageUri: Uri): ParsedReceiptResult {
        val inputImage = InputImage.fromFilePath(context, imageUri)
        return processInputImage(inputImage)
    }

    suspend fun scanReceiptFromBitmap(bitmap: Bitmap): ParsedReceiptResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return processInputImage(inputImage)
    }

    private suspend fun processInputImage(inputImage: InputImage): ParsedReceiptResult =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val lines = mutableListOf<String>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            lines.add(line.text)
                        }
                    }
                    val parsed = ReceiptTextParser.parseReceiptLines(lines)
                    continuation.resume(parsed)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
}
