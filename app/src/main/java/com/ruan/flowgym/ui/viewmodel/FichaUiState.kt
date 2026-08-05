package com.ruan.flowgym.ui.viewmodel

import com.ruan.flowgym.data.model.RotinaResponseDTO

sealed class FichaUiState {
    object Loading : FichaUiState()
    data class Success(val rotinas: List<RotinaResponseDTO>) : FichaUiState()
    data class Error(val message: String) : FichaUiState()
}