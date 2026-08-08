package com.ruan.flowgym.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.SessionManager
import com.ruan.flowgym.data.model.LoginDTO
import com.ruan.flowgym.data.model.UsuarioDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import com.ruan.flowgym.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val mensagem: String) : AuthState()
    data class Error(val mensagem: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: TreinoApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        val tokenSalvo = sessionManager.obterToken()

        if (!tokenSalvo.isNullOrEmpty()) {
            RetrofitClient.userToken = tokenSalvo
        }
    }

    fun login(usuario: String, password: String) {
        if (usuario.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = api.login(LoginDTO(usuario, password))
                if (response.isSuccessful && response.body() != null) {
                    val tokenData = response.body()!!

                    val token = tokenData.token

                    val userId = tokenData.id
                     Log.d("AUTH_LOGIN", "Token recebido do servidor: ${tokenData.token}")

                    if (!token.isNullOrEmpty() && userId != null) {
                        // Salva no SessionManager e atualiza a variavel global do Retrofit
                        sessionManager.salvarSessao(
                            token = token,
                            userId = userId,
                            name = tokenData.name ?: "",
                            username = tokenData.usuario ?: usuario
                        )
                        RetrofitClient.userToken = tokenData.token
                        _authState.value = AuthState.Success("Login efetuado com sucesso!")

                    } else {
                        _authState.value = AuthState.Error("Resposta de login inválida do servidor.")
                    }

                } else {
                    _authState.value = AuthState.Error("Usuário ou senha inválidos.")
                }
            } catch (e: Exception) {
                Log.e("AUTH_API", "Erro no login", e)
                _authState.value = AuthState.Error("Falha na conexão com o servidor.")
            }
        }
    }

    fun cadastrar(nome: String, email: String, usuario: String, password: String) {
        if (nome.isBlank() || email.isBlank() || usuario.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos obrigatórios.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val dto = UsuarioDTO(name = nome, email = email, usuario = usuario, password = password)
                val response = RetrofitClient.apiService.cadastrarUsuario(dto)

                if (response.isSuccessful) {
                    _authState.value = AuthState.Success("Cadastro realizado com sucesso! Faça login.")
                } else {
                    _authState.value = AuthState.Error("Erro ao cadastrar. Usuário ou e-mail já existente.")
                }
            } catch (e: Exception) {
                Log.e("AUTH_API", "Erro no cadastro", e)
                _authState.value = AuthState.Error("Falha na conexão com o servidor.")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}