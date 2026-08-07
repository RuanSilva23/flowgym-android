package com.ruan.flowgym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pesos_pendentes")
data class PesoEntity(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0,
    val usuarioId: Long,
    val peso: Double,
    val dataRegistro: String
)
