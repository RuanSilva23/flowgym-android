package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.data.remote.RetrofitClient
import com.ruan.flowgym.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExerciciosUiState {
    object Loading : ExerciciosUiState
    data class Sucesso(
        val exercicios: List<ExercicioResponseDTO>,
        val exerciciosFiltrados: List<ExercicioResponseDTO>
    ) : ExerciciosUiState
    data class Erro(val mensagem: String) : ExerciciosUiState
}
@HiltViewModel
class ExerciciosViewModel @Inject constructor(
    private val api: TreinoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciciosUiState>(ExerciciosUiState.Loading)
    val uiState: StateFlow<ExerciciosUiState> = _uiState.asStateFlow()

    private val _grupoSelecionado = MutableStateFlow("TODOS")
    val grupoSelecionado: StateFlow<String> = _grupoSelecionado.asStateFlow()

    private val _buscaQuery = MutableStateFlow("")
    val buscaQuery: StateFlow<String> = _buscaQuery.asStateFlow()

    fun carregarExercicios(idUsuario: Long = 1L) {
        viewModelScope.launch {
            _uiState.value = ExerciciosUiState.Loading
            try {
                val grupo = _grupoSelecionado.value
                val response = if (grupo == "TODOS") {
                    RetrofitClient.apiService.listarExercicios(idUsuario)
                } else {
                    RetrofitClient.apiService.listarPorGrupoMuscular(grupo)
                }

                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    val filtrada = aplicarFiltroBusca(lista, _buscaQuery.value)
                    _uiState.value = ExerciciosUiState.Sucesso(
                        exercicios = lista,
                        exerciciosFiltrados = filtrada
                    )
                } else {
                    _uiState.value = ExerciciosUiState.Erro("Erro ao carregar exercícios (${response.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = ExerciciosUiState.Erro(e.localizedMessage ?: "Erro de conexão")
            }
        }
    }

    fun selecionarGrupo(grupo: String, idUsuario: Long = 1L) {
        _grupoSelecionado.value = grupo
        carregarExercicios(idUsuario)
    }

    fun onBuscaQueryChange(query: String) {
        _buscaQuery.value = query
        val currentState = _uiState.value
        if (currentState is ExerciciosUiState.Sucesso) {
            val filtrada = aplicarFiltroBusca(currentState.exercicios, query)
            _uiState.value = currentState.copy(exerciciosFiltrados = filtrada)
        }
    }

    private fun aplicarFiltroBusca(lista: List<ExercicioResponseDTO>, query: String): List<ExercicioResponseDTO> {
        if (query.isBlank()) return lista
        return lista.filter { exercicio ->
            val nomeOk = exercicio.nome?.contains(query, ignoreCase = true) == true
            val grupoOk = exercicio.grupoMuscular?.contains(query, ignoreCase = true) == true
            nomeOk || grupoOk
        }
    }
}