package com.ruan.flowgym.data.model

data class PesoResponseDTO(
    val id: Long? = null,
    val pesoCorporal: Double? = null,
    val dataRegistro: String? = null
)

data class PesoRequestDTO(
    val idUsuario: Long,
    val peso: Double,
    val dataRegistro: String? = null
)
