package com.ruan.apexlift.data.model

data class FichaTreinoResponseDTO(
    val id: Long? = null,
    val nome: String = "",
    val gruposMusculares: String = "",
    val totalExercicios: Int = 0,
    val duracaoEstimadaMin: Int = 0
)
