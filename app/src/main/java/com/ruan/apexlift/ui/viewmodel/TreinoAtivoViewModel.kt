package com.ruan.apexlift.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.apexlift.data.local.dao.RotinaDao
import com.ruan.apexlift.data.local.dao.SessaoPendenteDao
import com.ruan.apexlift.data.local.entity.SeriePendenteEntity
import com.ruan.apexlift.data.local.entity.SessaoPendenteEntity
import com.ruan.apexlift.data.local.model.RotinaComExercicios
import com.ruan.apexlift.data.model.EditarSerieRequestDTO
import com.ruan.apexlift.data.model.NovaSerieRequestDTO
import com.ruan.apexlift.data.model.SerieTreinoResponseDTO
import com.ruan.apexlift.data.model.SessaoTreinoResponseDTO
import com.ruan.apexlift.data.remote.TreinoApiService
import com.ruan.apexlift.data.repository.ExercicioRepository
import com.ruan.apexlift.ui.util.TimerAlertHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

sealed interface TreinoUiState {
    object Idle : TreinoUiState
    object Loading : TreinoUiState
    data class Sucesso(
        val sessao: SessaoTreinoResponseDTO,
        val rotinaAtiva: RotinaComExercicios? = null,
        val ordemExerciciosIds: List<Long> = emptyList(),
        val series: List<SerieTreinoResponseDTO> = emptyList(),
        val sessaoLocalId: Long? = null
    ) : TreinoUiState
    data class Erro(val mensagem: String) : TreinoUiState
}

@HiltViewModel
class TreinoAtivoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rotinaDao: RotinaDao,
    private val sessaoPendenteDao: SessaoPendenteDao,
    private val exercicioRepository: ExercicioRepository,
    private val api: TreinoApiService
) : ViewModel() {

    private val timerAlertHelper = TimerAlertHelper(context)
    private val _uiState = MutableStateFlow<TreinoUiState>(TreinoUiState.Idle)
    val uiState: StateFlow<TreinoUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val _tempoRestante = MutableStateFlow(0)
    val tempoRestante: StateFlow<Int> = _tempoRestante.asStateFlow()

    private val _tempoTotalDescanso = MutableStateFlow(60)
    val tempoTotalDescanso: StateFlow<Int> = _tempoTotalDescanso.asStateFlow()

    init {
        sincronizarTreinosPendentes()
    }

    fun sincronizarTreinosPendentes() {
        viewModelScope.launch {
            try {
                val pendentes = sessaoPendenteDao.listarSessoesPendentes()
                for (sessao in pendentes) {
                    val respSessao = api.iniciarSessao(sessao.usuarioId, sessao.rotinaId)
                    if (respSessao.isSuccessful && respSessao.body() != null) {
                        val idSessaoServidor = respSessao.body()!!.id ?: continue
                        val series = sessaoPendenteDao.listarSeriesDaSessao(sessao.idLocal)

                        for (serie in series) {
                            api.registrarSerie(
                                NovaSerieRequestDTO(
                                    idSessao = idSessaoServidor,
                                    idExercicio = serie.exercicioId,
                                    carga = serie.carga,
                                    repeticoes = serie.repeticoes
                                )
                            )
                        }
                        api.finalizarSessao(idSessaoServidor)

                        sessaoPendenteDao.deletarSeriesDaSessao(sessao.idLocal)
                        sessaoPendenteDao.deletarSessaoPendente(sessao.idLocal)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun iniciarTreinoComRotina(idUsuario: Long, idRotina: Long) {
        viewModelScope.launch {
            _uiState.value = TreinoUiState.Loading
            sincronizarTreinosPendentes()

            try {
                val rotinaComExercicios = rotinaDao.buscarRotinaPorId(idRotina)
                var sessaoDto: SessaoTreinoResponseDTO? = null
                var sessaoLocalId: Long? = null

                try {
                    val responseSessao = api.iniciarSessao(idUsuario = idUsuario, idRotina = idRotina)
                    if (responseSessao.isSuccessful && responseSessao.body() != null) {
                        sessaoDto = responseSessao.body()
                    }
                } catch (_: Exception) { }

                if (sessaoDto == null) {
                    val dataAtualFormatada = java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    val entidadePendente = SessaoPendenteEntity(
                        usuarioId = idUsuario,
                        rotinaId = idRotina,
                        nomeRotina = rotinaComExercicios?.rotina?.nome ?: "Treino",
                        dataHoraInicio = dataAtualFormatada
                    )
                    sessaoLocalId = sessaoPendenteDao.salvarSessaoPendente(entidadePendente)

                    sessaoDto = SessaoTreinoResponseDTO(
                        id = sessaoLocalId,
                        idUsuario = idUsuario,
                        nomeRotina = rotinaComExercicios?.rotina?.nome ?: "Treino",
                        status = "EM_ANDAMENTO"
                    )
                }

                val ordemInicial = rotinaComExercicios?.itens.orEmpty()
                    .sortedBy { it.item.ordem }
                    .mapNotNull { it.exercicio.id }

                _uiState.value = TreinoUiState.Sucesso(
                    sessao = sessaoDto,
                    rotinaAtiva = rotinaComExercicios,
                    ordemExerciciosIds = ordemInicial,
                    sessaoLocalId = sessaoLocalId
                )
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro("Erro ao iniciar treino: ${e.localizedMessage}")
            }
        }
    }

    fun moverExercicioParaCima(index: Int) {
        val state = _uiState.value
        if (state is TreinoUiState.Sucesso && index > 0 && index < state.ordemExerciciosIds.size) {
            val lista = state.ordemExerciciosIds.toMutableList()
            Collections.swap(lista, index, index - 1)
            _uiState.value = state.copy(ordemExerciciosIds = lista)
        }
    }

    fun moverExercicioParaBaixo(index: Int) {
        val state = _uiState.value
        if (state is TreinoUiState.Sucesso && index >= 0 && index < state.ordemExerciciosIds.size - 1) {
            val lista = state.ordemExerciciosIds.toMutableList()
            Collections.swap(lista, index, index + 1)
            _uiState.value = state.copy(ordemExerciciosIds = lista)
        }
    }

    fun registrarSerie(
        idSessao: Long,
        idExercicio: Long,
        carga: Double,
        repeticoes: Int,
        nomeExercicio: String = "Exercício",
        tempoDescansoAlvo: Int = 60,
        ehBiSet: Boolean = false,
        ehUltimoBiSet: Boolean = false,
        ehAquecimento: Boolean = false,
        grupoBiSet: Int? = null
    ) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TreinoUiState.Sucesso) {
                var novaSerie: SerieTreinoResponseDTO? = null

                try {
                    val request = NovaSerieRequestDTO(idSessao, idExercicio, carga, repeticoes, ehAquecimento, grupoBiSet)
                    val response = api.registrarSerie(request)

                    if (response.isSuccessful && response.body() != null) {
                        val serieBackend = response.body()!!
                        val idExercicioFinal = if (serieBackend.idExercicio == 0L) idExercicio else serieBackend.idExercicio
                        val nomeExercicioFinal = if (serieBackend.nomeExercicio.isBlank() || serieBackend.nomeExercicio == "Exercício") {
                            nomeExercicio
                        } else {
                            serieBackend.nomeExercicio
                        }

                        novaSerie = serieBackend.copy(
                            idExercicio = idExercicioFinal,
                            nomeExercicio = nomeExercicioFinal,
                            aquecimento = ehAquecimento,
                            grupoBiSet = grupoBiSet
                        )
                    }
                } catch (_: Exception) { }

                if (novaSerie == null) {
                    val localId = currentState.sessaoLocalId ?: idSessao
                    sessaoPendenteDao.salvarSeriePendente(
                        SeriePendenteEntity(
                            sessaoLocalId = localId,
                            exercicioId = idExercicio,
                            carga = carga,
                            repeticoes = repeticoes
                        )
                    )

                    novaSerie = SerieTreinoResponseDTO(
                        id = System.currentTimeMillis(),
                        idSessao = localId,
                        idExercicio = idExercicio,
                        nomeExercicio = nomeExercicio,
                        carga = carga,
                        repeticoes = repeticoes,
                        aquecimento = ehAquecimento,
                        grupoBiSet = grupoBiSet
                    )
                }

                val listaAtualizada = currentState.series + novaSerie
                _uiState.value = currentState.copy(series = listaAtualizada)

                if (ehBiSet && !ehUltimoBiSet){
                    pularTimerDescanso()

                } else {
                    iniciarTimerDescanso(if (tempoDescansoAlvo > 0) tempoDescansoAlvo else 60)
                }
            }
        }
    }

    fun editarSerie(idSerie: Long, novaCarga: Double, novasReps: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is TreinoUiState.Sucesso) {
                // 1. Atualização Otimista no State
                val seriesAtualizadas = currentState.series.map { serie ->
                    if (serie.id == idSerie) {
                        serie.copy(carga = novaCarga, repeticoes = novasReps)
                    } else serie
                }
                _uiState.value = currentState.copy(series = seriesAtualizadas)

                // 2. Sincronização com o Backend
                try {
                    val response = api.editarSerie(idSerie,
                        EditarSerieRequestDTO(novaCarga, novasReps)
                    )
                    if (!response.isSuccessful) {
                        Log.e("EDITAR_SERIE", "Erro HTTP ${response.code()}: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Log.e("EDITAR_SERIE", "Falha de conexão ao editar série: ${e.localizedMessage}")
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
            timerAlertHelper.dispararAlertaFimDescanso()
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
                api.finalizarSessao(idSessao)
            } catch (_: Exception) { }

            sincronizarTreinosPendentes()
            _uiState.value = TreinoUiState.Idle
        }
    }
}