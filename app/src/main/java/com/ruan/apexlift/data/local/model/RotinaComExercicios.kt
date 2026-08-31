package com.ruan.apexlift.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ruan.apexlift.data.local.entity.FichaExercicioEntity
import com.ruan.apexlift.data.local.entity.RotinaEntity

data class RotinaComExercicios(
    @Embedded
    val rotina: RotinaEntity,

    @Relation(
        entity = FichaExercicioEntity::class,
        parentColumn = "id",
        entityColumn = "rotinaId"
    )
    val itens: List<ItemFichaDetalhado>
)
