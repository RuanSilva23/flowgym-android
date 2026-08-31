package com.ruan.apexlift.ui.viewmodel

import com.ruan.apexlift.data.model.RotinaResponseDTO

sealed class FichaUiState {
    object Loading : FichaUiState()
    data class Success(val rotinas: List<RotinaResponseDTO>) : FichaUiState()
    data class Error(val message: String) : FichaUiState()
}