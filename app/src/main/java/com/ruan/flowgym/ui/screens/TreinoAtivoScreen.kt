package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.data.local.model.ItemFichaDetalhado
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinoAtivoScreen(
    viewModel: TreinoAtivoViewModel,
    idUsuario: Long
) {
    val uiState by viewModel.uiState.collectAsState()
    val tempoRestante by viewModel.tempoRestante.collectAsState()
    val tempoTotalDescanso by viewModel.tempoTotalDescanso.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Treino Ativo") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TreinoUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Nenhum treino em andamento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selecione uma ficha na aba Perfil/Fichas para iniciar o treino guiado.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is TreinoUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TreinoUiState.Erro -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.mensagem,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is TreinoUiState.Sucesso -> {
                    val rotinaComExercicios = state.rotinaAtiva
                    val listaItens: List<ItemFichaDetalhado> = rotinaComExercicios?.itens ?: emptyList()

                    // 👈 CORREÇÃO: Extrai o id da sessão para uma constante primitiva Long não-nula local
                    val idSessao: Long = state.sessao.id ?: 0L

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (tempoRestante > 0) {
                            TreinoTimerCard(
                                tempoRestante = tempoRestante,
                                tempoTotal = tempoTotalDescanso,
                                onMaisDezSegundos = { viewModel.adicionarTempoDescanso(10) },
                                onPular = { viewModel.pularTimerDescanso() }
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = rotinaComExercicios?.rotina?.nome ?: "Treino Livre",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sessão #$idSessao",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = { viewModel.finalizarTreino(idSessao) }, // 👈 Usa a constante idSessao
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Finalizar")
                                }
                            }
                        }

                        if (listaItens.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum exercício nesta ficha.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(listaItens) { itemDetalhado ->
                                    val idRaw: Long? = itemDetalhado.item.exercicioId ?: itemDetalhado.exercicio?.id

                                    if (idRaw == null || idRaw == 0L) return@items

                                    val idExercicio: Long = idRaw

                                    val seriesDoExercicio = state.series.filter { it.idExercicio == idExercicio }
                                    val nomeExercicio = itemDetalhado.exercicio?.nome
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "Exercício #$idExercicio"

                                    ItemExercicioTreinoCard(
                                        itemDetalhado = itemDetalhado,
                                        exercicioId = idExercicio,
                                        nomeExercicio = nomeExercicio,
                                        seriesRegistradas = seriesDoExercicio,
                                        onRegistrarSerie = { carga, reps ->
                                            viewModel.registrarSerie(
                                                idSessao = idSessao, // 👈 Usa a constante idSessao
                                                idExercicio = idExercicio,
                                                carga = carga,
                                                repeticoes = reps,
                                                nomeExercicio = nomeExercicio,
                                                tempoDescansoAlvo = 60
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreinoTimerCard(
    tempoRestante: Int,
    tempoTotal: Int,
    onMaisDezSegundos: () -> Unit,
    onPular: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Descanso: ${tempoRestante}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    TextButton(onClick = onMaisDezSegundos) {
                        Text("+10s")
                    }
                    TextButton(onClick = onPular) {
                        Text("Pular")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (tempoTotal > 0) tempoRestante.toFloat() / tempoTotal.toFloat() else 0f },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ItemExercicioTreinoCard(
    itemDetalhado: ItemFichaDetalhado,
    exercicioId: Long,
    nomeExercicio: String,
    seriesRegistradas: List<SerieTreinoResponseDTO>,
    onRegistrarSerie: (carga: Double, reps: Int) -> Unit
) {
    var cargaInput by remember { mutableStateOf(itemDetalhado.item.cargaAlvo.toString()) }
    var repsInput by remember { mutableStateOf(itemDetalhado.item.repeticoesAlvo.toString()) }
    var enviandoSerie by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = nomeExercicio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Meta: ${itemDetalhado.item.seriesAlvo}x ${itemDetalhado.item.repeticoesAlvo} reps (${itemDetalhado.item.cargaAlvo}kg)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            seriesRegistradas.forEachIndexed { index, serie ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Série ${index + 1}", fontWeight = FontWeight.SemiBold)
                    Text(text = "${serie.carga} kg x ${serie.repeticoes} reps")
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Concluída",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = cargaInput,
                    onValueChange = { cargaInput = it },
                    label = { Text("Kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val carga = cargaInput.toDoubleOrNull() ?: 0.0
                        val reps = repsInput.toIntOrNull() ?: 0
                        if (reps > 0 && !enviandoSerie) {
                            enviandoSerie = true
                            onRegistrarSerie(carga, reps)
                            // Reseta a trava após registrar
                            enviandoSerie = false
                        }
                    },
                    enabled = !enviandoSerie,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(
                            if (enviandoSerie) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Registrar Série",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}