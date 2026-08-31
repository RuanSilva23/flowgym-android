package com.ruan.apexlift.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.apexlift.data.local.SessionManager
import com.ruan.apexlift.data.local.dao.PesoDao
import com.ruan.apexlift.data.local.dao.SessaoPendenteDao
import com.ruan.apexlift.data.local.entity.PesoEntity
import com.ruan.apexlift.data.model.NovaSerieRequestDTO
import com.ruan.apexlift.data.model.PesoMetaResquestDTO
import com.ruan.apexlift.data.model.PesoRequestDTO
import com.ruan.apexlift.data.model.PesoResponseDTO
import com.ruan.apexlift.data.model.SerieTreinoResponseDTO
import com.ruan.apexlift.data.model.SessaoTreinoResponseDTO
import com.ruan.apexlift.data.remote.RetrofitClient
import com.ruan.apexlift.data.remote.TreinoApiService
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
        val nomeUsuario: String,
        val historicoSessoes: List<SessaoTreinoResponseDTO>,
        val pesoAtual: Double? = null,
        val pesoMeta: Double? = null,
        val historicoPeso: List<PesoResponseDTO> = emptyList()
    ) : HomeUiState()
    data class Erro(val mensagem: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: TreinoApiService,
    private val sessaoPendenteDao: SessaoPendenteDao,
    private val pesoDao: PesoDao,
    private val sessionManager: SessionManager // 👈 Injetado para gerenciar sessão e token
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        // 💡 Garante que o Token JWT esteja carregado no Retrofit antes de qualquer chamada
        val tokenSalvo = sessionManager.obterToken()
        if (!tokenSalvo.isNullOrEmpty()) {
            RetrofitClient.userToken = tokenSalvo
        }
    }

    fun carregarDadosHome() {
        val idUsuario = sessionManager.obterUserId()
        val nomeUsuarioLogado = sessionManager.obterNome().ifBlank { "Atleta" }

        var pesoMetaSalvo = sessionManager.obterPesoMeta()

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
                    } else {
                        Log.e("SYNC_PESO", "Erro HTTP ${resp.code()}: ${resp.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC_PESO", "Falha de conexão ao sincronizar peso: ${e.localizedMessage}")
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
                        Log.d("SYNC_TREINO", "Sessão local ID ${sessao.idLocal} sincronizada no servidor!")
                    } else {
                        Log.e("SYNC_TREINO", "Erro HTTP ${respSessao.code()}: ${respSessao.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SYNC_TREINO", "Falha ao sincronizar treino pendente: ${e.localizedMessage}")
            }

            // 3. BUSCA HISTÓRICO DE PESOS (SERVIDOR + ROOM LOCAL)
            var listaPesoServidor: List<PesoResponseDTO> = emptyList()

            try {
                val respHistoricoPeso = api.buscarHistoricoPeso(idUsuario)
                if (respHistoricoPeso.isSuccessful) {
                    listaPesoServidor = respHistoricoPeso.body().orEmpty()
                } else {
                    Log.e("API_PESO", "Erro ao buscar pesos: ${respHistoricoPeso.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_PESO", "Erro de conexão ao buscar pesos: ${e.localizedMessage}")
            }

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

            val historicoPesoCompleto = listaPesoServidor + pesosLocais
            val pesoAtualCalculado = historicoPesoCompleto.lastOrNull()?.pesoCorporal

            // 4. BUSCA SESSÕES DE TREINO (SERVIDOR + ROOM LOCAL)
            var sessoesServidor: List<SessaoTreinoResponseDTO> = emptyList()
            try {
                val response = api.buscarHistoricoSessoes(idUsuario)
                if (response.isSuccessful) {
                    sessoesServidor = response.body().orEmpty()
                } else {
                    Log.e("API_TREINO", "Erro ao buscar histórico de treinos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_TREINO", "Erro de conexão ao buscar histórico de treinos: ${e.localizedMessage}")
            }

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

            try {
                val responseMeta = api.buscarPesoMeta(idUsuario)

                if (responseMeta.isSuccessful && responseMeta.body() != null) {
                    val metaServidor = responseMeta.body()?.get("pesoMeta")

                    if (metaServidor != null && metaServidor > 0.0) {
                        pesoMetaSalvo = metaServidor
                        sessionManager.salvarPesoMeta(metaServidor)
                    }
                }

            } catch (e: Exception) {
                Log.e("SYNC_META", "Sem conexão para buscar meta no servidor: ${e.localizedMessage}")
            }

            _uiState.value = HomeUiState.Sucesso(
                nomeUsuario = nomeUsuarioLogado,
                historicoSessoes = pendentesLocais + sessoesServidor,
                pesoAtual = pesoAtualCalculado,
                pesoMeta =  pesoMetaSalvo,
                historicoPeso = historicoPesoCompleto
            )
        }
    }

    fun registrarNovoPeso(novoPeso: Double) {
        val idUsuario = sessionManager.obterUserId()
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
                    Log.d("PESO_API", "Sucesso ao cadastrar peso no servidor. Removendo id local: $idLocalGerado")
                    pesoDao.deletarPesoPendente(idLocalGerado)
                } else {
                    Log.e("PESO_API", "Erro HTTP ${response.code()}: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("PESO_API", "Erro de conexão (peso salvo localmente): ${e.localizedMessage}")
            } finally {
                carregarDadosHome()
            }
        }
    }

    fun atualizarMetaPeso(novaMeta: Double) {
        val idUsuario = sessionManager.obterUserId()

        viewModelScope.launch {
            val estadoAnterior = _uiState.value
            val metaAntiga = (estadoAnterior as? HomeUiState.Sucesso)?.pesoMeta

            sessionManager.salvarPesoMeta(novaMeta)

            if (estadoAnterior is HomeUiState.Sucesso) {
                _uiState.value = estadoAnterior.copy(pesoMeta = novaMeta)
            }

            try {
                val dto = PesoMetaResquestDTO(
                    idUsuario = idUsuario,
                    pesoMeta = novaMeta
                )

                val response = api.atualizarPesoMeta(idUsuario, dto)

                if (response.isSuccessful) {
                    Log.d("PESO_META_API", "Meta de peso atualizada com sucesso no servidor!")
                } else {
                    Log.e("PESO_META_API", "Erro HTTP ${response.code()}: ${response.errorBody()?.string()}")
                    // Reverte para a meta anterior caso a API rejeite
                    reverterMeta(metaAntiga)
                }
            } catch (e: Exception) {
                Log.e("PESO_META_API", "Falha de conexão ao atualizar meta: ${e.localizedMessage}", e)
                // Reverte em caso de perda de conexão
                reverterMeta(metaAntiga)
            }
        }
    }

    private fun reverterMeta(metaAntiga: Double?) {
        if (metaAntiga != null) sessionManager.salvarPesoMeta(metaAntiga)

        val estadoAtual = _uiState.value
        if (estadoAtual is HomeUiState.Sucesso) {
            _uiState.value = estadoAtual.copy(pesoMeta = metaAntiga)
        }
    }

    fun deletarSessao(idSessao: Long) {
        val idUsuario = sessionManager.obterUserId()

        viewModelScope.launch {
            try {
                sessaoPendenteDao.deletarSeriesDaSessao(idSessao)
                sessaoPendenteDao.deletarSessaoPendente(idSessao)
                api.deletarSessao(idSessao)
            } catch (e: Exception) {
                Log.e("DELETAR_TREINO", "Falha ao deletar: ${e.localizedMessage}")
            } finally {
                carregarDadosHome()
            }
        }
    }
}