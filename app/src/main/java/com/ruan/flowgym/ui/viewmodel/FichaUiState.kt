package com.ruan.flowgym.ui.viewmodel

import com.ruan.flowgym.data.local.model.RotinaComExercicios

sealed interface FichaUiState {
    object Loading : FichaUiState
    data class Success(val rotinas: List<RotinaComExercicios>) : FichaUiState
    data class Error(val message: String) : FichaUiState
}