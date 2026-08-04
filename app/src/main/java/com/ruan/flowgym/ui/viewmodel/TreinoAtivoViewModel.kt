package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.SerieTreinoRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class TreinoUiState {
    object Idle : TreinoUiState()
    object Loading : TreinoUiState()
    data class Sucesso(val sessao: SessaoTreinoResponseDTO) : TreinoUiState()
    data class Erro(val mensagem: String) : TreinoUiState()
}

class TreinoAtivoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TreinoUiState>(TreinoUiState.Idle)
    val uiState: StateFlow<TreinoUiState> = _uiState

    fun iniciarTreino(idUsuario: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            try {
                // Nome do método atualizado na interface
                val response = RetrofitClient.apiService.iniciarTreino(idUsuario)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TreinoUiState.Sucesso(response.body()!!)
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao iniciar treino: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro(e.message ?: "Falha na conexão com a API")
            }
        }
    }

    fun registrarSerie(idSessao: Long, idExercicio: Long, carga: Double, repeticoes: Int) {
        viewModelScope.launch {
            try {
                val dto = SerieTreinoRequestDTO(
                    idSessao = idSessao,
                    idExercicio = idExercicio,
                    carga = carga,
                    repeticoes = repeticoes
                )
                // Nome do método e envio via DTO atualizados
                val response = RetrofitClient.apiService.salvarSerie(dto)
                if (response.isSuccessful) {
                    // Atualização da UI ou lista de séries se necessário
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao salvar série: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro(e.message ?: "Falha ao registrar série")
            }
        }
    }

    fun finalizarTreino(idSessao: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            try {
                // Nome do método atualizado na interface
                val response = RetrofitClient.apiService.finalizarTreino(idSessao)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TreinoUiState.Sucesso(response.body()!!)
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao finalizar treino: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro(e.message ?: "Falha ao finalizar treino")
            }
        }
    }
}