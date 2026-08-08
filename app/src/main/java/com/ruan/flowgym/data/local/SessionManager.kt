package com.ruan.flowgym.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("flowgym_sessiom", Context.MODE_PRIVATE)

    companion object{
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME= "user_name"
        private const val KEY_USERNAME = "username"
    }

    fun salvarSessao(token: String, userId: Long, name: String, username: String) {
        prefs.edit().apply(){
            putString(KEY_TOKEN, token)
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, name)
            putString(KEY_USERNAME, username)
        }.commit()
    }

    fun obterToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun obterUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun estaLogado(): Boolean = !obterToken().isNullOrEmpty()

    fun obterNome(): String = prefs.getString(KEY_USER_NAME, "Usuário") ?: "Usuário"

    fun obterUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun limparSessao() {
        prefs.edit().clear().commit()
    }
}