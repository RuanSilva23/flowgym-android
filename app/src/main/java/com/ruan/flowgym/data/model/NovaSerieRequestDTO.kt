package com.ruan.flowgym.data.model

data class NovaSerieRequestDTO(
    val idSessao: Long,
    val idExercicio: Long,
    val carga: Double,
    val repeticoes: Int
)
