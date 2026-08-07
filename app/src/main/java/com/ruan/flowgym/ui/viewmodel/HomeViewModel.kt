package com.ruan.flowgym.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.PesoDao
import com.ruan.flowgym.data.local.dao.SessaoPendenteDao
import com.ruan.flowgym.data.local.entity.PesoEntity
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.PesoRequestDTO
import com.ruan.flowgym.data.model.PesoResponseDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Sucesso(
        val nomeUsuario: String = "Ruan",
        val historicoSessoes: List<SessaoTreinoResponseDTO>,
        val pesoAtual: Double? = null,
        val pesoMeta: Double? = 70.0,
        val historicoPeso: List<PesoResponseDTO> = emptyList()
    ) : HomeUiState()
    data class Erro(val mensagem: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: TreinoApiService,
    private val sessaoPendenteDao: SessaoPendenteDao,
    private val pesoDao: PesoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun carregarDadosHome(idUsuario: Long = 1L) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // 1. SINCRONIZA PESOS OFFLINE COM O BACKEND
            try {
                val pesosPendentes = pesoDao.listarPesosPendentes(idUsuario)
                for (pesoPendente in pesosPendentes) {
                    val dto = PesoRequestDTO(
                        idUsuario = idUsuario,
                        peso = pesoPendente.peso,
                        dataRegistro = pesoPendente.dataRegistro
                    )
                    val resp = api.cadastrarPeso(idUsuario, dto)
                    if (resp.isSuccessful) {
                        pesoDao.deletarPesoPendente(pesoPendente.idLocal)
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC_PESO", "Servidor offline durante sincronização de peso.")
            }

            // 2. SINCRONIZA TREINOS PENDENTES COM O BACKEND
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
            } catch (_: Exception) {}

            // 3. BUSCA HISTÓRICO DE PESOS (SERVIDOR + ROOM LOCAL)
            var listaPesoServidor: List<PesoResponseDTO> = emptyList()

            try {
                val respHistoricoPeso = api.buscarHistoricoPeso(idUsuario)
                if (respHistoricoPeso.isSuccessful) {
                    listaPesoServidor = respHistoricoPeso.body().orEmpty()
                }
            } catch (_: Exception) {}

            // Busca os pesos salvos localmente no Room
            val pesosLocais = try {
                pesoDao.listarPesosPendentes(idUsuario).map { entity ->
                    PesoResponseDTO(
                        id = entity.idLocal,
                        pesoCorporal = entity.peso,
                        dataRegistro = entity.dataRegistro
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }

            // Unifica os registros (Servidor + Locais)
            val historicoPesoCompleto = listaPesoServidor + pesosLocais
            val pesoAtualCalculado = historicoPesoCompleto.lastOrNull()?.pesoCorporal

            // 4. BUSCA SESSÕES DE TREINO (SERVIDOR + ROOM LOCAL)
            var sessoesServidor: List<SessaoTreinoResponseDTO> = emptyList()
            try {
                val response = api.buscarHistoricoSessoes(idUsuario)
                if (response.isSuccessful) {
                    sessoesServidor = response.body().orEmpty()
                }
            } catch (_: Exception) {}

            val pendentesLocais = try {
                sessaoPendenteDao.listarSessoesPendentes().map { p ->
                    val seriesPendentes = sessaoPendenteDao.listarSeriesDaSessao(p.idLocal)
                    SessaoTreinoResponseDTO(
                        id = p.idLocal,
                        idUsuario = p.usuarioId,
                        nomeRotina = "${p.nomeRotina} (Offline ⏳)",
                        dataHoraInicio = p.dataHoraInicio,
                        status = "FINALIZADO",
                        series = seriesPendentes.map { s ->
                            SerieTreinoResponseDTO(
                                id = s.idLocal,
                                idSessao = s.sessaoLocalId,
                                idExercicio = s.exercicioId,
                                carga = s.carga,
                                repeticoes = s.repeticoes
                            )
                        }
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }

            _uiState.value = HomeUiState.Sucesso(
                historicoSessoes = pendentesLocais + sessoesServidor,
                pesoAtual = pesoAtualCalculado,
                historicoPeso = historicoPesoCompleto
            )
        }
    }

    // REGISTRA NO ROOM E TENTA ENVIAR AO BACKEND
    fun registrarNovoPeso(idUsuario: Long, novoPeso: Double) {
        viewModelScope.launch {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dataAtual = formatter.format(Date())

            val idLocalGerado = pesoDao.salvarPesoLocal(
                PesoEntity(
                    usuarioId = idUsuario,
                    peso = novoPeso,
                    dataRegistro = dataAtual
                )
            )

            try {
                val dto = PesoRequestDTO(
                    idUsuario = idUsuario,
                    peso = novoPeso,
                    dataRegistro = dataAtual
                )

                val response = api.cadastrarPeso(idUsuario, dto)
                if (response.isSuccessful) {
                    Log.d("PESO_API", "Sucesso ao cadastrar no servidor. Removendo pendência ID: $idLocalGerado")
                    pesoDao.deletarPesoPendente(idLocalGerado)
                } else {
                    Log.e("PESO_API", "Erro HTTP ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("PESO_API", "Erro de conexão (mantido no Room offline): ${e.localizedMessage}")
            } finally {
                carregarDadosHome(idUsuario)
            }
        }
    }

    fun atualizarMetaPeso(idUsuario: Long, novaMeta: Double) {
        viewModelScope.launch {
            val estadoAtual = _uiState.value
            if (estadoAtual is HomeUiState.Sucesso) {
                _uiState.value = estadoAtual.copy(pesoMeta = novaMeta)
            }
        }
    }

    fun deletarSessao(idSessao: Long, idUsuario: Long) {
        viewModelScope.launch {
            try {
                sessaoPendenteDao.deletarSeriesDaSessao(idSessao)
                sessaoPendenteDao.deletarSessaoPendente(idSessao)
                api.deletarSessao(idSessao)
            } catch (e: Exception) {
                Log.e("DELETAR_TREINO", "Falha ao deletar: ${e.localizedMessage}")
            } finally {
                carregarDadosHome(idUsuario)
            }
        }
    }
}