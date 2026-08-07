package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.local.dao.SessaoPendenteDao
import com.ruan.flowgym.data.local.entity.SeriePendenteEntity
import com.ruan.flowgym.data.local.entity.SessaoPendenteEntity
import com.ruan.flowgym.data.local.model.RotinaComExercicios
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
import java.time.LocalDateTime
import javax.inject.Inject

sealed interface TreinoUiState {
    object Idle : TreinoUiState
    object Loading : TreinoUiState
    data class Sucesso(
        val sessao: SessaoTreinoResponseDTO,
        val rotinaAtiva: RotinaComExercicios? = null,
        val series: List<SerieTreinoResponseDTO> = emptyList(),
        val sessaoLocalId: Long? = null
    ) : TreinoUiState
    data class Erro(val mensagem: String) : TreinoUiState
}

@HiltViewModel
class TreinoAtivoViewModel @Inject constructor(
    private val rotinaDao: RotinaDao,
    private val sessaoPendenteDao: SessaoPendenteDao,
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

    init {
        sincronizarTreinosPendentes()
    }

    // 👈 Sincroniza treinos guardados offline com o Spring Boot assim que houver conexão
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

                        // Limpa a pendência do celular após enviar com sucesso
                        sessaoPendenteDao.deletarSeriesDaSessao(sessao.idLocal)
                        sessaoPendenteDao.deletarSessaoPendente(sessao.idLocal)
                    }
                }
            } catch (_: Exception) {
                // Se continuar offline, mantém no banco local para tentar na próxima vez
            }
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

                // Fallback Offline: Grava sessão pendente no SQLite (Room)
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

                _uiState.value = TreinoUiState.Sucesso(
                    sessao = sessaoDto,
                    rotinaAtiva = rotinaComExercicios,
                    sessaoLocalId = sessaoLocalId
                )
            } catch (e: Exception) {
                _uiState.value = TreinoUiState.Erro("Erro ao iniciar treino: ${e.localizedMessage}")
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
                var novaSerie: SerieTreinoResponseDTO? = null

                try {
                    val request = NovaSerieRequestDTO(idSessao, idExercicio, carga, repeticoes)
                    val response = api.registrarSerie(request)

                    if (response.isSuccessful && response.body() != null) {
                        val serieBackend = response.body()!!

                        // Garante o ID e Nome do exercício correto caso venham zerados do backend
                        val idExercicioFinal = if (serieBackend.idExercicio == 0L) idExercicio else serieBackend.idExercicio
                        val nomeExercicioFinal = if (serieBackend.nomeExercicio.isBlank() || serieBackend.nomeExercicio == "Exercício") {
                            nomeExercicio
                        } else {
                            serieBackend.nomeExercicio
                        }

                        novaSerie = serieBackend.copy(
                            idExercicio = idExercicioFinal,
                            nomeExercicio = nomeExercicioFinal
                        )
                    }
                } catch (_: Exception) { }

                // Fallback Offline: se o servidor estiver fora, gera a série localmente na UI
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
                        repeticoes = repeticoes
                    )
                }

                val listaAtualizada = currentState.series + novaSerie
                _uiState.value = currentState.copy(series = listaAtualizada)

                iniciarTimerDescanso(if (tempoDescansoAlvo > 0) tempoDescansoAlvo else 60)
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
                api.finalizarSessao(idSessao)
            } catch (_: Exception) { }

            // Garante o envio de qualquer treino offline retido
            sincronizarTreinosPendentes()
            _uiState.value = TreinoUiState.Idle
        }
    }
}