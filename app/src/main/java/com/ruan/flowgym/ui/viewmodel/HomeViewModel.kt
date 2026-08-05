package com.ruan.flowgym.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import com.ruan.flowgym.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Sucesso(
        val historicoSessoes: List<SessaoTreinoResponseDTO>
    ) : HomeUiState()
    data class Erro(val mensagem: String) : HomeUiState()
}
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: TreinoApiService
) : ViewModel() {

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

    fun deletarSessao(idSessao: Long, idUsuario: Long) {
        viewModelScope.launch {
            try {
                val response = api.deletarSessao(idSessao)
                if (response.isSuccessful) {
                    // Recarrega a lista do histórico imediatamente
                    carregarDadosHome(idUsuario)

                } else {
                    Log.e("DELETAR_TREINO", "Erro no servidor: Código ${response.code()}")
                }
            } catch (e: Exception) {
                // Pode tratar erros de rede se necessário
                Log.e("DELETAR_TREINO", "Falha de conexão: ${e.localizedMessage}")
            }
        }
    }
}