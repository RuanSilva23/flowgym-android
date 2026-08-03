package com.ruan.flowgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruan.flowgym.ui.screens.TreinoAtivoScreen
import com.ruan.flowgym.ui.theme.FlowGymTheme
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlowGymTheme {
                val viewModel: TreinoAtivoViewModel = viewModel()
                TreinoAtivoScreen(
                    viewModel = viewModel,
                    idUsuarioLogado = 1L // ID fixo para o usuário dos seus testes
                )
            }
        }
    }
}