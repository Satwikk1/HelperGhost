package com.example.helper_ghost.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class GemmaDownloader(private val client: OkHttpClient) {
    fun downloadModel(url: String, targetFile: File): Flow<DownloadStatus> = flow {
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Failed to download")

        val body = response.body ?: throw IOException("Empty body")
        val totalBytes = body.contentLength()
        var downloadedBytes = 0L

        body.byteStream().use { input ->
            targetFile.outputStream().use { output ->
                val buffer = ByteArray(1024 * 8)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedBytes += bytesRead
                    emit(DownloadStatus.Progress(downloadedBytes.toFloat() / totalBytes))
                }
            }
        }
        emit(DownloadStatus.Success(targetFile))
    }.flowOn(Dispatchers.IO)
}

sealed class DownloadStatus {
    data class Progress(val percentage: Float) : DownloadStatus()
    data class Success(val file: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}