package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Sucesso(
        val historicoSessoes: List<SessaoTreinoResponseDTO>
    ) : HomeUiState()
    data class Erro(val mensagem: String) : HomeUiState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun carregarDadosHome(idUsuario: Long = 1L) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = RetrofitClient.apiService.buscarHistoricoSessoes(idUsuario)

                if (response.isSuccessful) {
                    val sessoes = response.body() ?: emptyList()
                    _uiState.value = HomeUiState.Sucesso(historicoSessoes = sessoes)
                } else {
                    _uiState.value = HomeUiState.Erro("Erro no servidor: code ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Erro(e.message ?: "Erro ao conectar com o backend")
            }
        }
    }
}