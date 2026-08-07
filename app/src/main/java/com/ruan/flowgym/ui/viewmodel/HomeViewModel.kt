package com.ruan.flowgym.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.SessaoPendenteDao
import com.ruan.flowgym.data.model.NovaSerieRequestDTO
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Sucesso(
        val historicoSessoes: List<SessaoTreinoResponseDTO>
    ) : HomeUiState()
    data class Erro(val mensagem: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: TreinoApiService,
    private val sessaoPendenteDao: SessaoPendenteDao // 👈 Injetado para ler treinos offline
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun carregarDadosHome(idUsuario: Long = 1L) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            // 1. Tenta enviar treinos pendentes para o Spring Boot se houver conexão
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

                        // Limpa do banco local após enviar com sucesso
                        sessaoPendenteDao.deletarSeriesDaSessao(sessao.idLocal)
                        sessaoPendenteDao.deletarSessaoPendente(sessao.idLocal)
                    }
                }
            } catch (_: Exception) {
                // Servidor offline: ignora e prossegue para carregar dados locais
            }

            // 2. Busca histórico oficial do servidor
            var sessoesServidor: List<SessaoTreinoResponseDTO> = emptyList()
            var erroServidor = false

            try {
                val response = api.buscarHistoricoSessoes(idUsuario)
                if (response.isSuccessful) {
                    sessoesServidor = response.body().orEmpty()
                } else {
                    erroServidor = true
                }
            } catch (_: Exception) {
                erroServidor = true
            }

            // 3. Busca treinos offline pendentes no Room do celular
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

            // Unifica os treinos offline pendentes com o histórico do servidor
            val listaCompleta = pendentesLocais + sessoesServidor

            if (listaCompleta.isNotEmpty()) {
                _uiState.value = HomeUiState.Sucesso(historicoSessoes = listaCompleta)
            } else if (erroServidor) {
                _uiState.value = HomeUiState.Erro("Servidor offline. Nenhum treino local encontrado.")
            } else {
                _uiState.value = HomeUiState.Sucesso(historicoSessoes = emptyList())
            }
        }
    }

    fun deletarSessao(idSessao: Long, idUsuario: Long) {
        viewModelScope.launch {
            try {
                // Tenta apagar do banco local de pendências primeiro
                sessaoPendenteDao.deletarSeriesDaSessao(idSessao)
                sessaoPendenteDao.deletarSessaoPendente(idSessao)

                // Tenta apagar no servidor
                api.deletarSessao(idSessao)
            } catch (e: Exception) {
                Log.e("DELETAR_TREINO", "Falha ao deletar: ${e.localizedMessage}")
            } finally {
                carregarDadosHome(idUsuario)
            }
        }
    }
}