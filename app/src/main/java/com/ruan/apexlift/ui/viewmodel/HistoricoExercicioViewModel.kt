package com.ruan.apexlift.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.apexlift.data.local.SessionManager
import com.ruan.apexlift.data.model.SerieTreinoResponseDTO
import com.ruan.apexlift.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HistoricoExercicioUiState {
    object Loading : HistoricoExercicioUiState
    data class Sucesso(val series: List<SerieTreinoResponseDTO>) : HistoricoExercicioUiState
    data class Erro(val mensagem: String) : HistoricoExercicioUiState
}

@HiltViewModel
class HistoricoExercicioViewModel @Inject constructor(
    private val api: TreinoApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoricoExercicioUiState>(HistoricoExercicioUiState.Loading)
    val uiState: StateFlow<HistoricoExercicioUiState> = _uiState.asStateFlow()

    fun carregarHistorico(idUsuario: Long = sessionManager.obterUserId(), idExercicio: Long) {
        viewModelScope.launch {
            _uiState.value = HistoricoExercicioUiState.Loading
            try {
                val response = api.buscarHistoricoExercicio(idUsuario, idExercicio)
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _uiState.value = HistoricoExercicioUiState.Sucesso(lista)
                } else {
                    _uiState.value = HistoricoExercicioUiState.Erro("Erro ao carregar histórico (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = HistoricoExercicioUiState.Erro(e.localizedMessage ?: "Erro de conexão")
            }
        }
    }
}