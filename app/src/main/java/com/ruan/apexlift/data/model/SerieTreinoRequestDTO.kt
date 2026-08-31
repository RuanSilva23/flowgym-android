package com.ruan.apexlift.data.model

data class SerieTreinoRequestDTO(
    val idSessao: Long,
    val idExercicio: Long,
    val carga: Double,
    val repeticoes: Int
)
