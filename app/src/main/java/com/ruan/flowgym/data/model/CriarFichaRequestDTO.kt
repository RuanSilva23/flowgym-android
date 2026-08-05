package com.ruan.flowgym.data.model

import com.google.gson.annotations.SerializedName

// Request enviado ao backend (Spring Boot)
data class CriarFichaRequestDTO(
    val idUsuario: Long,
    val nome: String,
    val descricao: String?,
    val itemFicha: List<ItemFichaRequestDTO>
)

data class ItemFichaRequestDTO(
    val idExercicio: Long,
    val ordem: Int,
    val seriesAlvo: Int,
    val repeticoesAlvo: Int,
    val cargaAlvo: Double,
    val descanso: Int
)

// Response recebido do backend (Spring Boot)
data class RotinaResponseDTO(
    val id: Long,
    val nome: String,
    val descricao: String?,
    val exercicios: List<FichaResponseDTO> = emptyList()
)

data class FichaResponseDTO(
    val id: Long,
    val idExercicio: Long,
    val nomeExercicio: String?,
    val ordem: Int,
    val seriesAlvo: Int,
    val repeticoesAlvo: Int,
    val cargaAlvo: Double,

    @SerializedName("descanso") // 👈 CORREÇÃO CRÍTICA: Mapeia o "descanso" do Java
    val descanso: Int,

    val grupoMuscular: String? // Mapeia o Enum GrupoMuscular do Java como String
)