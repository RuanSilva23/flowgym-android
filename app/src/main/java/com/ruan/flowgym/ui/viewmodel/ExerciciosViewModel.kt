package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.data.repository.ExercicioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val exercicioRepository: ExercicioRepository
) : ViewModel() {

    private val _grupoSelecionado = MutableStateFlow("TODOS")
    val grupoSelecionado: StateFlow<String> = _grupoSelecionado.asStateFlow()

    private val _buscaQuery = MutableStateFlow("")
    val buscaQuery: StateFlow<String> = _buscaQuery.asStateFlow()

    private val _uiState = MutableStateFlow<ExerciciosUiState>(ExerciciosUiState.Loading)
    val uiState: StateFlow<ExerciciosUiState> = _uiState.asStateFlow()

    init {
        observarExerciciosLocais()
        carregarExercicios(idUsuario = 1L)
    }

    // 👈 Observa o Room em tempo real. Se estiver offline, entrega os dados locais instantaneamente.
    private fun observarExerciciosLocais() {
        viewModelScope.launch {
            combine(
                exercicioRepository.todosExercicios,
                _grupoSelecionado,
                _buscaQuery
            ) { listaEntities, grupo, query ->
                val listaDtos = listaEntities.map { entity ->
                    ExercicioResponseDTO(
                        id = entity.id,
                        nome = entity.nome,
                        grupoMuscular = entity.grupoMuscular
                    )
                }

                val filtradosPorGrupo = if (grupo == "TODOS") {
                    listaDtos
                } else {
                    listaDtos.filter { it.grupoMuscular.equals(grupo, ignoreCase = true) }
                }

                val filtradosFinais = if (query.isBlank()) {
                    filtradosPorGrupo
                } else {
                    filtradosPorGrupo.filter {
                        it.nome.contains(query, ignoreCase = true) ||
                                it.grupoMuscular.contains(query, ignoreCase = true)
                    }
                }

                ExerciciosUiState.Sucesso(
                    exercicios = listaDtos,
                    exerciciosFiltrados = filtradosFinais
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun carregarExercicios(idUsuario: Long = 1L) {
        viewModelScope.launch {
            // Tenta sincronizar com o backend em segundo plano
            exercicioRepository.sincronizarExercicios(idUsuario)
        }
    }

    fun selecionarGrupo(grupo: String, idUsuario: Long = 1L) {
        _grupoSelecionado.value = grupo
    }

    fun onBuscaQueryChange(query: String) {
        _buscaQuery.value = query
    }
}