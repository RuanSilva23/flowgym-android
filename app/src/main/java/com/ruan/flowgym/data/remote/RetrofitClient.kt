package com.ruan.flowgym.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 💡 Se estiver no Wi-Fi local, use o IP da sua máquina (ex: 192.168.31.161)
    // 💡 Se estiver no cabo USB com 'adb reverse tcp:8080 tcp:8080', altere para: "http://127.0.0.1:8080/"
    private const val BASE_URL = "http://192.168.31.161:8080/"

    var userToken: String? = null

    // 1. Interceptor de Log
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }


    // 2. OkHttpClient com Logging e Timeout adequado para Wi-Fi (3s)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // 👈 Agora o log está vinculado!
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiService: TreinoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TreinoApiService::class.java)
    }
}