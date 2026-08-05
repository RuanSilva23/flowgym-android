package com.ruan.flowgym.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruan.flowgym.data.local.dao.RotinaDao
import com.ruan.flowgym.data.mapper.toEntity
import com.ruan.flowgym.data.model.CriarFichaRequestDTO
import com.ruan.flowgym.data.model.ExercicioResponseDTO
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
        carregarFichas(idUsuario = 1L)
    }

    fun carregarFichas(idUsuario: Long = 1L) {
        viewModelScope.launch {
            _uiState.value = FichaUiState.Loading
            try {
                // 1. Sincroniza exercicios locais para garantir chaves estrangeiras no Room
                exercicioRepository.sincronizarExercicios(idUsuario)

                // 2. Carrega exercícios para o dropdown
                val responseExercicios = apiService.listarExercicios(idUsuario)
                if (responseExercicios.isSuccessful) {
                    _exerciciosDisponiveis.value = responseExercicios.body().orEmpty()
                }

                // 3. Carrega as fichas da API
                val responseFichas = apiService.listarFichasPorUsuario(idUsuario)
                if (responseFichas.isSuccessful && responseFichas.body() != null) {
                    val fichasDto = responseFichas.body()!!
                    _uiState.value = FichaUiState.Success(fichasDto)

                    // 👈 PERSISTE NO ROOM LOCAL PARA O TREINO ATIVO PODER LER
                    fichasDto.forEach { rotinaDto ->
                        val rotinaEntity = rotinaDto.toEntity(idUsuario)
                        val itensEntities = rotinaDto.exercicios.map { itemDto ->
                            itemDto.toEntity(rotinaId = rotinaDto.id)
                        }
                        rotinaDao.salvarFichaCompleta(rotinaEntity, itensEntities)
                    }
                } else {
                    _uiState.value = FichaUiState.Error("Erro ao buscar fichas: ${responseFichas.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = FichaUiState.Error("Falha de rede: ${e.localizedMessage}")
            }
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
                val response = apiService.deletarFicha(idFicha)
                if (response.isSuccessful) {
                    rotinaDao.deletarRotina(idFicha)
                    carregarFichas(idUsuario)
                } else {
                    _uiState.value = FichaUiState.Error("Erro ao excluir: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = FichaUiState.Error("Erro ao apagar ficha: ${e.localizedMessage}")
            }
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
                } else {
                    _uiState.value = FichaUiState.Error("Erro ao editar: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = FichaUiState.Error("Erro ao editar ficha: ${e.localizedMessage}")
            }
        }
    }
}