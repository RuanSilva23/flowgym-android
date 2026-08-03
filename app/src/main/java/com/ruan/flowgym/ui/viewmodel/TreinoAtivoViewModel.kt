package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TreinoUiState {
    object SemSessao : TreinoUiState
    object Carregando : TreinoUiState
    data class SessaoAtiva(
        val sessao: SessaoTreinoResponseDTO,
        val series: List<SerieTreinoResponseDTO> = emptyList(),
        val mensagemErro: String? = null
    ) : TreinoUiState
    data class Finalizado(val sessao: SessaoTreinoResponseDTO) : TreinoUiState
    data class Erro(val mensagem: String) : TreinoUiState
}

class TreinoAtivoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TreinoUiState>(TreinoUiState.SemSessao)
    val uiState: StateFlow<TreinoUiState> = _uiState.asStateFlow()

    fun iniciarTreino(idUsuario: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Carregando
            try {
                val response = RetrofitClient.apiService.iniciarSessao(idUsuario)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TreinoUiState.SessaoAtiva(sessao = response.body()!!)
                } else {
                    _uiState.value = TreinoUiState.Erro("Falha ao iniciar treino: Código ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro("Sem conexão com o servidor: ${e.localizedMessage}")
            }
        }
    }

    fun registrarSerie(idExercicio: Long, carga: Double, repeticoes: Int) {
        val estadoAtual = _uiState.value
        if (estadoAtual is TreinoUiState.SessaoAtiva) {
            viewModelScope.launch {
                try {
                    val request = NovaSerieRequestDTO(
                        idSessao = estadoAtual.sessao.id,
                        idExercicio = idExercicio,
                        carga = carga,
                        repeticoes = repeticoes
                    )
                    val response = RetrofitClient.apiService.registrarSerie(request)
                    if (response.isSuccessful && response.body() != null) {
                        val novaSerie = response.body()!!
                        val listaAtualizada = estadoAtual.series + novaSerie
                        _uiState.value = estadoAtual.copy(
                            series = listaAtualizada,
                            mensagemErro = null
                        )
                    } else {
                        val erroMsg = response.errorBody()?.string() ?: "Dados inválidos"
                        _uiState.value = estadoAtual.copy(mensagemErro = erroMsg)
                    }
                } catch (e: Exception) {
                    _uiState.value = estadoAtual.copy(mensagemErro = "Erro de conexão: ${e.localizedMessage}")
                }
            }
        }
    }

    fun finalizarTreino() {
        val estadoAtual = _uiState.value
        if (estadoAtual is TreinoUiState.SessaoAtiva) {
            viewModelScope.launch {
                _uiState.value = TreinoUiState.Carregando
                try {
                    val response = RetrofitClient.apiService.finalizarSessao(estadoAtual.sessao.id)
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = TreinoUiState.Finalizado(response.body()!!)
                    } else {
                        _uiState.value = TreinoUiState.Erro("Erro ao finalizar treino")
                    }
                } catch (e: Exception) {
                    _uiState.value = TreinoUiState.Erro("Erro de conexão: ${e.localizedMessage}")
                }
            }
        }
    }
}