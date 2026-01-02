package com.example.helper_ghost.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helper_ghost.data.remote.DownloadStatus
import com.example.helper_ghost.data.remote.GemmaDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadViewModel(private val downloader: GemmaDownloader) : ViewModel() {
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete = _isComplete.asStateFlow()

    fun startDownload(url: String, file: File) {
        viewModelScope.launch {
            downloader.downloadModel(url, file).collect { status ->
                when (status) {
                    is DownloadStatus.Progress -> _downloadProgress.value = status.percentage
                    is DownloadStatus.Success -> _isComplete.value = true
                    is DownloadStatus.Error -> {  }
                }
            }
        }
    }
}