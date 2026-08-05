package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.local.model.RotinaComExercicios
import com.ruan.flowgym.data.mapper.toEntity
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import com.ruan.flowgym.data.repository.ExercicioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TreinoUiState {
    object Idle : TreinoUiState
    object Loading : TreinoUiState
    data class Sucesso(
        val sessao: SessaoTreinoResponseDTO,
        val rotinaAtiva: RotinaComExercicios? = null,
        val series: List<SerieTreinoResponseDTO> = emptyList()
    ) : TreinoUiState
    data class Erro(val mensagem: String) : TreinoUiState
}

@HiltViewModel
class TreinoAtivoViewModel @Inject constructor(
    private val rotinaDao: RotinaDao,
    private val exercicioRepository: ExercicioRepository,
    private val api: TreinoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<TreinoUiState>(TreinoUiState.Idle)
    val uiState: StateFlow<TreinoUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val _tempoRestante = MutableStateFlow(0)
    val tempoRestante: StateFlow<Int> = _tempoRestante.asStateFlow()

    private val _tempoTotalDescanso = MutableStateFlow(60)
    val tempoTotalDescanso: StateFlow<Int> = _tempoTotalDescanso.asStateFlow()

    fun iniciarTreinoComRotina(idUsuario: Long, idRotina: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            try {
                // 1. Tenta buscar a ficha no Room local
                var rotinaComExercicios = rotinaDao.buscarRotinaPorId(idRotina)

                // 2. Se a ficha não existir no Room local, busca na API e grava no Room
                if (rotinaComExercicios == null) {
                    exercicioRepository.sincronizarExercicios(idUsuario)
                    val responseRotina = api.buscarRotinaPorId(idRotina)
                    if (responseRotina.isSuccessful && responseRotina.body() != null) {
                        val rotinaDto = responseRotina.body()!!
                        val rotinaEntity = rotinaDto.toEntity(idUsuario)
                        val itensEntities = rotinaDto.exercicios.map { itemDto ->
                            itemDto.toEntity(rotinaId = rotinaDto.id)
                        }
                        rotinaDao.salvarFichaCompleta(rotinaEntity, itensEntities)
                        rotinaComExercicios = rotinaDao.buscarRotinaPorId(idRotina)
                    }
                }

                // 3. Abre a sessão de treino no backend
                val responseSessao = api.iniciarSessao(idUsuario = idUsuario, idRotina = idRotina)

                if (responseSessao.isSuccessful && responseSessao.body() != null) {
                    _uiState.value = TreinoUiState.Sucesso(
                        sessao = responseSessao.body()!!,
                        rotinaAtiva = rotinaComExercicios
                    )
                } else {
                    _uiState.value = TreinoUiState.Erro("Erro ao iniciar sessão no servidor: ${responseSessao.code()}")
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
        nomeExercicio: String = "Exercício",
        tempoDescansoAlvo: Int = 60
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TreinoUiState.Sucesso) {
                try {
                    val request = NovaSerieRequestDTO(idSessao, idExercicio, carga, repeticoes)
                    val response = api.registrarSerie(request)

                    if (response.isSuccessful && response.body() != null) {
                        val serieBackend = response.body()!!

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

                        iniciarTimerDescanso(if (tempoDescansoAlvo > 0) tempoDescansoAlvo else 60)
                    }
                } catch (e: Exception) {
                    // Tratar exceções se necessário
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