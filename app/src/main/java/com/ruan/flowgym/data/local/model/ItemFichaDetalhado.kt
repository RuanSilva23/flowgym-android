package com.ruan.flowgym.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.local.entity.FichaExercicioEntity

data class ItemFichaDetalhado(
    @Embedded
    val item: FichaExercicioEntity,
    @Relation(
        parentColumn = "exercicioId",
        entityColumn = "id"
    )
    val exercicio: ExercicioEntity
)
