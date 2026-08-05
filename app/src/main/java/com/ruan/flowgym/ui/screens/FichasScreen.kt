package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // 👈 Importante
import androidx.compose.runtime.getValue        // 👈 Importante
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.ui.components.RotinaCard
import com.ruan.flowgym.ui.viewmodel.FichaUiState
import com.ruan.flowgym.ui.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichasScreen(
    viewModel: FichaViewModel,
    onRotinaClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var exibindoModalCriacao by remember { mutableStateOf(false) }
    val exerciciosDisponiveis by viewModel.exerciciosDisponiveis.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Fichas de Treino") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { exibindoModalCriacao = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Ficha")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FichaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FichaUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FichaUiState.Success -> {
                    if (state.rotinas.isEmpty()) {
                        Text(
                            text = "Nenhuma ficha cadastrada ainda.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.rotinas) { rotinaComExercicios ->
                                RotinaCard(
                                    rotinaComExercicios = rotinaComExercicios,
                                    onClick = { onRotinaClick(rotinaComExercicios.rotina.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    if (exibindoModalCriacao) {
        CriarFichaDialog(
            exerciciosDisponiveis = exerciciosDisponiveis,
            onDismiss = { exibindoModalCriacao = false },
            onConfirm = { nome, descricao, itens ->
                viewModel.criarFicha(
                    nome = nome,
                    descricao = descricao,
                    itens = itens,
                    onSuccess = { exibindoModalCriacao = false },
                    onError = { mensagemErro -> /* Exibir Snackbar */ }
                )
            }
        )
    }
}