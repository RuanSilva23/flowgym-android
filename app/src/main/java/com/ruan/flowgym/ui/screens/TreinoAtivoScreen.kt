package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinoAtivoScreen(
    viewModel: TreinoAtivoViewModel,
    idUsuarioLogado: Long = 1L // ID fixo para testes
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FlowGym - Treino Ativo") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val currentState = state) {
                is TreinoUiState.SemSessao -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhum treino em andamento",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.iniciarTreino(idUsuarioLogado) }) {
                            Text("Iniciar Novo Treino")
                        }
                    }
                }

                is TreinoUiState.Carregando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TreinoUiState.SessaoAtiva -> {
                    ConteudoTreinoAtivo(
                        state = currentState,
                        onSalvarSerie = { idEx, carga, reps ->
                            viewModel.registrarSerie(idEx, carga, reps)
                        },
                        onFinalizarTreino = { viewModel.finalizarTreino() }
                    )
                }

                is TreinoUiState.Finalizado -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💪 Treino Finalizado com Sucesso!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sessão #${currentState.sessao.id} salva no histórico.")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { viewModel.iniciarTreino(idUsuarioLogado) }) {
                            Text("Iniciar Outro Treino")
                        }
                    }
                }

                is TreinoUiState.Erro -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentState.mensagem,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.iniciarTreino(idUsuarioLogado) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConteudoTreinoAtivo(
    state: TreinoUiState.SessaoAtiva,
    onSalvarSerie: (Long, Double, Int) -> Unit,
    onFinalizarTreino: () -> Unit
) {
    var idExercicioInput by remember { mutableStateOf("1") }
    var cargaInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sessão Ativa #${state.sessao.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Iniciada em: ${state.sessao.dataHoraInicio}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formulário para registrar série
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Registrar Série", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = idExercicioInput,
                        onValueChange = { idExercicioInput = it },
                        label = { Text("ID Exer.") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cargaInput,
                        onValueChange = { cargaInput = it },
                        label = { Text("Carga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsInput,
                        onValueChange = { repsInput = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                state.mensagemErro?.let { erro ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = erro, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val idEx = idExercicioInput.toLongOrNull() ?: 1L
                        val carga = cargaInput.toDoubleOrNull() ?: 0.0
                        val reps = repsInput.toIntOrNull() ?: 0
                        if (carga > 0 && reps > 0) {
                            onSalvarSerie(idEx, carga, reps)
                            cargaInput = ""
                            repsInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Adicionar Série")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Séries Registradas nesta Sessão", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.series) { serie ->
                ItemSerie(serie)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinalizarTreino,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finalizar Treino")
        }
    }
}

@Composable
fun ItemSerie(serie: SerieTreinoResponseDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = serie.nomeExercicio, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Série #${serie.id}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${serie.carga} kg  ×  ${serie.repeticoes} reps",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}