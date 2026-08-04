package com.ruan.flowgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ruan.flowgym.ui.screens.TreinoAtivoScreen
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TreinoAtivoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    TreinoAtivoScreen(viewModel = viewModel)
                }
            }
        }
    }
}