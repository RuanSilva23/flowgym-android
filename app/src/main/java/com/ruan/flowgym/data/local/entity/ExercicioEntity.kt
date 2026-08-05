package com.ruan.flowgym.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercicios")
data class ExercicioEntity(
    @PrimaryKey
    val id: Long ?= null,
    val nome: String,
    val grupoMuscular: String
)