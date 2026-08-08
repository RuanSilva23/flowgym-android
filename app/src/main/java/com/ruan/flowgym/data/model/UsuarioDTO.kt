package com.ruan.flowgym.data.model

data class UsuarioDTO(
    val name: String,
    val email: String,
    val usuario: String,
    val password: String,
    val role: String? = "ROLE_USER"
)
