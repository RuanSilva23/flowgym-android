package com.ruan.flowgym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rotinas")
data class RotinaEntity(
    @PrimaryKey(autoGenerate = false) // ID vindo do backend
    val id: Long,
    val usuarioId: Long,
    val nome: String,
    val descricao: String?,
    val sincronizado: Boolean = true // Flag útil para controle offline
)