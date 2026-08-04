package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinoAtivoScreen(
    viewModel: TreinoAtivoViewModel,
    idUsuario: Long = 1L
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlowGym - Treino Ativo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is TreinoUiState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nenhum treino em andamento",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.iniciarTreino(idUsuario) }) {
                            Text("Iniciar Novo Treino")
                        }
                    }
                }

                is TreinoUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is TreinoUiState.Sucesso -> {
                    val sessao = state.sessao
                    var idExercicioText by remember { mutableStateOf("") }
                    var cargaText by remember { mutableStateOf("") }
                    var repeticoesText by remember { mutableStateOf("") }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Sessão de Treino #${sessao.id ?: "-"}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Status: ${sessao.status ?: "EM ANDAMENTO"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Registrar Série",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = idExercicioText,
                                onValueChange = { idExercicioText = it },
                                label = { Text("ID Exer.") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cargaText,
                                onValueChange = { cargaText = it },
                                label = { Text("Carga (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = repeticoesText,
                                onValueChange = { repeticoesText = it },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val idExercicio = idExercicioText.toLongOrNull()
                                val carga = cargaText.toDoubleOrNull()
                                val reps = repeticoesText.toIntOrNull()
                                val idSessao = sessao.id

                                if (idSessao != null && idExercicio != null && carga != null && reps != null) {
                                    viewModel.registrarSerie(
                                        idSessao = idSessao,
                                        idExercicio = idExercicio,
                                        carga = carga,
                                        repeticoes = reps
                                    )
                                    idExercicioText = ""
                                    cargaText = ""
                                    repeticoesText = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salvar Série")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                sessao.id?.let { idSessao ->
                                    viewModel.finalizarTreino(idSessao)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Finalizar Treino")
                        }
                    }
                }

                is TreinoUiState.Erro -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.mensagem,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.iniciarTreino(idUsuario) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}