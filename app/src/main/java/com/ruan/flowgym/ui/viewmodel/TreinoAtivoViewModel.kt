package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import com.ruan.flowgym.data.repository.ExercicioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TreinoUiState {
    object Idle : TreinoUiState
    object Loading : TreinoUiState
    data class Sucesso(
        val sessao: SessaoTreinoResponseDTO,
        val series: List<SerieTreinoResponseDTO> = emptyList()
    ) : TreinoUiState
    data class Erro(val mensagem: String) : TreinoUiState
}
@HiltViewModel // 👈 ADICIONADO
class TreinoAtivoViewModel @Inject constructor( // 👈 @Inject ADICIONADO E REMOVIDO VALOR DEFAULT
    private val exercicioRepository: ExercicioRepository,
    private val api: TreinoApiService
) : ViewModel(){

    // Lista de Exercícios consumida diretamente do Room (carregamento instantâneo offline)
    val exercicios: StateFlow<List<ExercicioEntity>> = exercicioRepository.todosExercicios
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<TreinoUiState>(TreinoUiState.Idle)
    val uiState: StateFlow<TreinoUiState> = _uiState.asStateFlow()

    // Estados do Timer de Descanso
    private var timerJob: Job? = null

    private val _tempoRestante = MutableStateFlow(0)
    val tempoRestante: StateFlow<Int> = _tempoRestante.asStateFlow()

    private val _tempoTotalDescanso = MutableStateFlow(60)
    val tempoTotalDescanso: StateFlow<Int> = _tempoTotalDescanso.asStateFlow()

    init {
        // Tenta atualizar o Room com os dados mais recentes do backend em background
        sincronizarExercicios()
    }

    fun sincronizarExercicios() {
        viewModelScope.launch {
            exercicioRepository.sincronizarExercicios()
        }
    }

    fun iniciarTreino(idUsuario: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            try {
                val response = api.iniciarSessao(idUsuario)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = TreinoUiState.Sucesso(sessao = response.body()!!)
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao iniciar sessão: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro("Falha de conexão: ${e.localizedMessage}")
            }
        }
    }

    fun registrarSerie(
        idSessao: Long,
        idExercicio: Long,
        carga: Double,
        repeticoes: Int,
        nomeExercicio: String = "Exercício"
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TreinoUiState.Sucesso) {
                try {
                    val request = NovaSerieRequestDTO(idSessao, idExercicio, carga, repeticoes)
                    val response = api.registrarSerie(request)

                    if (response.isSuccessful && response.body() != null) {
                        val serieBackend = response.body()!!

                        // Trata tipos não-nulos e substitui fallbacks do backend se necessário
                        val idExercicioFinal = if (serieBackend.idExercicio == 0L) idExercicio else serieBackend.idExercicio
                        val nomeExercicioFinal = if (serieBackend.nomeExercicio.isBlank() || serieBackend.nomeExercicio == "Exercício") {
                            nomeExercicio
                        } else {
                            serieBackend.nomeExercicio
                        }

                        val novaSerie = serieBackend.copy(
                            idExercicio = idExercicioFinal,
                            nomeExercicio = nomeExercicioFinal
                        )

                        val listaAtualizada = currentState.series + novaSerie
                        _uiState.value = currentState.copy(series = listaAtualizada)

                        iniciarTimerDescanso(60)
                    }
                } catch (e: Exception) {
                    // Tratar erro de conexão
                }
            }
        }
    }

    fun iniciarTimerDescanso(segundos: Int = 60) {
        timerJob?.cancel()
        _tempoTotalDescanso.value = segundos
        _tempoRestante.value = segundos

        timerJob = viewModelScope.launch {
            while (_tempoRestante.value > 0) {
                delay(1000L)
                _tempoRestante.value -= 1
            }
        }
    }

    fun adicionarTempoDescanso(segundos: Int = 10) {
        if (_tempoRestante.value > 0) {
            _tempoRestante.value += segundos
            _tempoTotalDescanso.value = maxOf(_tempoTotalDescanso.value, _tempoRestante.value)
        }
    }

    fun pularTimerDescanso() {
        timerJob?.cancel()
        _tempoRestante.value = 0
    }

    fun finalizarTreino(idSessao: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            pularTimerDescanso()
            try {
                val response = api.finalizarSessao(idSessao)
                if (response.isSuccessful) {
                    _uiState.value = TreinoUiState.Idle
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao finalizar treino")
                }
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro("Falha de conexão: ${e.localizedMessage}")
            }
        }
    }
}
