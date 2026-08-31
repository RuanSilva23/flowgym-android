package com.ruan.apexlift.ui.screens

import com.ruan.apexlift.data.local.entity.ExercicioEntity
import com.ruan.apexlift.data.model.ItemFichaRequestDTO

data class ItemFichaFormState(
    val exercicioSelecionado: ExercicioEntity? = null,
    val series: String = "4",
    val repeticoes: String = "10",
    val carga: String = "20.0",
    val descanso: String = "60"
) {
    fun toDTO(ordem: Int): ItemFichaRequestDTO? {
        val idExercicio = exercicioSelecionado?.id ?: return null
        return ItemFichaRequestDTO(
            idExercicio = idExercicio,
            ordem = ordem,
            seriesAlvo = series.toIntOrNull() ?: 1,
            repeticoesAlvo = repeticoes.toIntOrNull() ?: 1,
            cargaAlvo = carga.toDoubleOrNull() ?: 0.0,
            descanso = descanso.toIntOrNull() ?: 60
        )
    }
}