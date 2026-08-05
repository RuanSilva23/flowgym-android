package com.ruan.flowgym.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ficha_exercicios",
    foreignKeys = [
        ForeignKey(
            entity = RotinaEntity::class,
            parentColumns = ["id"],
            childColumns = ["rotinaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExercicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercicioId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["rotinaId"]),
        Index(value = ["exercicioId"])
    ]
)
data class FichaExercicioEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val rotinaId: Long,
    val exercicioId: Long,
    val ordem: Int,
    val seriesAlvo: Int,
    val repeticoesAlvo: Int,
    val cargaAlvo: Double,
    val descansoSeg: Int
)