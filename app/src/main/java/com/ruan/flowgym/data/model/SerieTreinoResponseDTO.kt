package com.ruan.flowgym.data.model

import com.google.gson.annotations.SerializedName

data class SerieTreinoResponseDTO(
    val id: Long = 0L,

    @SerializedName("sessaoId")
    val idSessao: Long = 0L,

    @SerializedName("exercicioId")
    val idExercicio: Long = 0L,

    val nomeExercicio: String = "Exercício",
    val carga: Double = 0.0,
    val repeticoes: Int = 0
)