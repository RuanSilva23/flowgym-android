package com.ruan.flowgym.data.remote

import android.util.Log
import com.ruan.flowgym.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.obterToken()

        Log.d("AUTH_INTERCEPTOR", "Token lido do SessionManager: $token")

        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrEmpty()) {
            val bearerToken = "Bearer $token"
            requestBuilder.header("Authorization", bearerToken)
            Log.d("AUTH_INTERCEPTOR", "Header Authorization anexado com sucesso.")
        } else {
            Log.w("AUTH_INTERCEPTOR", "Nenhum token encontrado no SessionManager.")
        }

        return chain.proceed(requestBuilder.build())
    }
}