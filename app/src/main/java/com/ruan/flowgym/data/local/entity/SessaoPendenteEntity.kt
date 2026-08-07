package com.ruan.flowgym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessoes_pendentes")
data class SessaoPendenteEntity(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0L,
    val usuarioId: Long,
    val rotinaId: Long?,
    val nomeRotina: String,
    val dataHoraInicio: String
)

@Entity(tableName = "series_pendentes")
data class SeriePendenteEntity(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0L,
    val sessaoLocalId: Long,
    val exercicioId: Long,
    val carga: Double,
    val repeticoes: Int
)