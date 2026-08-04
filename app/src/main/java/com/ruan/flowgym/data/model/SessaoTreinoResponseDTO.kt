package com.ruan.flowgym.data.model

data class SessaoTreinoResponseDTO(
    val id: Long? = null,
    val idUsuario: Long? = null,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val status: String? = "EM ANDAMENTO",
    val series: List<SerieTreinoResponseDTO>? = emptyList()
)
