package com.example.helper_ghost.data.remote

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkModule {
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Longer for large AI models
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}