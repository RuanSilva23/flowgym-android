package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.mapper.toEntity
import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.data.model.FichaResponseDTO
import com.ruan.flowgym.data.model.ItemFichaRequestDTO
import com.ruan.flowgym.data.model.RotinaResponseDTO
import com.ruan.flowgym.data.remote.TreinoApiService
import com.ruan.flowgym.data.repository.ExercicioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FichaViewModel @Inject constructor(
    private val apiService: TreinoApiService,
    private val rotinaDao: RotinaDao,
    private val exercicioRepository: ExercicioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FichaUiState>(FichaUiState.Loading)
    val uiState: StateFlow<FichaUiState> = _uiState.asStateFlow()

    private val _exerciciosDisponiveis = MutableStateFlow<List<ExercicioResponseDTO>>(emptyList())
    val exerciciosDisponiveis: StateFlow<List<ExercicioResponseDTO>> = _exerciciosDisponiveis.asStateFlow()

    init {
        observarFichasLocais(idUsuario = 1L)
        carregarFichas(idUsuario = 1L)
    }

    // 👈 Carrega as fichas salvas no SQLite (Room) local
    private fun observarFichasLocais(idUsuario: Long = 1L) {
        viewModelScope.launch {
            rotinaDao.listarRotinasPorUsuario(idUsuario).collect { listaRotinasComExercicios ->
                val listaDtos = listaRotinasComExercicios.map { item ->
                    RotinaResponseDTO(
                        id = item.rotina.id,
                        nome = item.rotina.nome,
                        descricao = item.rotina.descricao,
                        exercicios = item.itens.map { itemDetalhado ->
                            FichaResponseDTO(
                                id = itemDetalhado.item.id,
                                idExercicio = itemDetalhado.item.exercicioId,
                                nomeExercicio = itemDetalhado.exercicio.nome,
                                ordem = itemDetalhado.item.ordem,
                                seriesAlvo = itemDetalhado.item.seriesAlvo,
                                repeticoesAlvo = itemDetalhado.item.repeticoesAlvo,
                                cargaAlvo = itemDetalhado.item.cargaAlvo,
                                descanso = itemDetalhado.item.descansoSeg,
                                grupoMuscular = itemDetalhado.exercicio.grupoMuscular
                            )
                        }
                    )
                }
                _uiState.value = FichaUiState.Success(listaDtos)
            }
        }
    }

    fun carregarFichas(idUsuario: Long = 1L) {
        viewModelScope.launch {
            try {
                exercicioRepository.sincronizarExercicios(idUsuario)

                try {
                    val responseExercicios = apiService.listarExercicios(idUsuario)
                    if (responseExercicios.isSuccessful) {
                        _exerciciosDisponiveis.value = responseExercicios.body().orEmpty()
                    }
                } catch (_: Exception) { }

                try {
                    val responseFichas = apiService.listarFichasPorUsuario(idUsuario)
                    if (responseFichas.isSuccessful && responseFichas.body() != null) {
                        val fichasDto = responseFichas.body()!!
                        fichasDto.forEach { rotinaDto ->
                            val rotinaEntity = rotinaDto.toEntity(idUsuario)
                            val itensEntities = rotinaDto.exercicios.map { itemDto ->
                                itemDto.toEntity(rotinaId = rotinaDto.id)
                            }
                            rotinaDao.salvarFichaCompleta(rotinaEntity, itensEntities)
                        }
                    }
                } catch (_: Exception) {
                    // Sem internet? O observarFichasLocais() já mantem a UI com as fichas salvas!
                }
            } catch (_: Exception) { }
        }
    }

    fun criarFicha(
        idUsuario: Long = 1L,
        nome: String,
        descricao: String,
        itens: List<ItemFichaRequestDTO>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val dto = CriarFichaRequestDTO(
                    idUsuario = idUsuario,
                    nome = nome,
                    descricao = descricao,
                    itemFicha = itens
                )
                val response = apiService.montarFicha(dto)
                if (response.isSuccessful) {
                    carregarFichas(idUsuario)
                    onSuccess()
                } else {
                    onError("Erro ao criar ficha: ${response.code()}")
                }
            } catch (e: Exception) {
                onError("Falha na requisição: ${e.localizedMessage}")
            }
        }
    }

    fun deletarFicha(idFicha: Long, idUsuario: Long = 1L) {
        viewModelScope.launch {
            try {
                rotinaDao.deletarRotina(idFicha)
                val response = apiService.deletarFicha(idFicha)
                if (response.isSuccessful) {
                    carregarFichas(idUsuario)
                }
            } catch (_: Exception) { }
        }
    }

    fun editarFicha(
        idFicha: Long,
        idUsuario: Long = 1L,
        nome: String,
        descricao: String,
        itens: List<ItemFichaRequestDTO>
    ) {
        viewModelScope.launch {
            try {
                val request = CriarFichaRequestDTO(
                    idUsuario = idUsuario,
                    nome = nome,
                    descricao = descricao,
                    itemFicha = itens
                )
                val response = apiService.editarFicha(idFicha, request)
                if (response.isSuccessful) {
                    carregarFichas(idUsuario)
                }
            } catch (_: Exception) { }
        }
    }
}