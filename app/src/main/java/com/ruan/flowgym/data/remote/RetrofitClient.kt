package com.ruan.flowgym.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 é o IP especial do Emulador Android para acessar o localhost do seu computador
    private const val BASE_URL = "http://127.0.0.1:8080/"

    val apiService: TreinoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TreinoApiService::class.java)
    }
}