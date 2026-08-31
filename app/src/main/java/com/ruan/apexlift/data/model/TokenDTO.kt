package com.ruan.apexlift.data.model

data class TokenDTO(
    val token: String,
    val id: Long? = null,
    val usuario: String? = null,
    val name: String? = null
)
