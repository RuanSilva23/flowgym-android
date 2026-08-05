package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import com.ruan.flowgym.data.model.ItemFichaRequestDTO
import com.ruan.flowgym.data.repository.ExercicioRepository
import com.ruan.flowgym.data.repository.FichaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FichaViewModel @Inject constructor(
    private val repository: FichaRepository,
    private val exercicioRepository: ExercicioRepository // 👈 Injetado para acessar a lista de exercícios
) : ViewModel() {

    private val usuarioIdLogado = 1L

    // Lista de exercícios cadastrados no banco local (Room)
    val exerciciosDisponiveis: StateFlow<List<ExercicioEntity>> = exercicioRepository.todosExercicios
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<FichaUiState> = repository
        .getFichasDoUsuario(usuarioIdLogado)
        .map<_, FichaUiState> { rotinas -> FichaUiState.Success(rotinas) }
        .onStart { emit(FichaUiState.Loading) }
        .catch { emit(FichaUiState.Error("Erro ao carregar fichas locais")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FichaUiState.Loading
        )

    init {
        sincronizar()
    }

    fun sincronizar() {
        viewModelScope.launch {
            repository.sincronizarFichas(usuarioIdLogado)
            exercicioRepository.sincronizarExercicios(usuarioIdLogado)
        }
    }

    fun criarFicha(
        nome: String,
        descricao: String,
        itens: List<ItemFichaRequestDTO>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val dto = CriarFichaRequestDTO(
                idUsuario = usuarioIdLogado,
                nome = nome,
                descricao = descricao,
                itemFicha = itens
            )
            val result = repository.criarFicha(dto)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Erro ao criar ficha")
            }
        }
    }
}