package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreinoAtivoScreen(
    viewModel: TreinoAtivoViewModel,
    idUsuario: Long = 1L
) {
    val uiState by viewModel.uiState.collectAsState()
    val tempoRestante by viewModel.tempoRestante.collectAsState()
    val tempoTotalDescanso by viewModel.tempoTotalDescanso.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FlowGym - Treino Ativo",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is TreinoUiState.Idle -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Pronto para treinar?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.iniciarTreino(idUsuario) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "INICIAR TREINO",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                is TreinoUiState.Loading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                }

                is TreinoUiState.Sucesso -> {
                    val sessao = state.sessao
                    val series = state.series
                    val listaExercicios by viewModel.exercicios.collectAsState()

                    var exercicioSelecionado by remember { mutableStateOf<ExercicioEntity?>(null) }
                    var cargaText by remember { mutableStateOf("") }
                    var repeticoesText by remember { mutableStateOf("") }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        TimerDescansoCard(
                            tempoRestante = tempoRestante,
                            tempoTotal = tempoTotalDescanso,
                            onAdicionarDezSegundos = { viewModel.adicionarTempoDescanso(10) },
                            onPular = { viewModel.pularTimerDescanso() }
                        )

                        if (tempoRestante > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        SeletorExercicio(
                            listaExercicios = listaExercicios,
                            exercicioSelecionado = exercicioSelecionado,
                            onExercicioSelecionado = { exercicioSelecionado = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = cargaText,
                                onValueChange = { cargaText = it },
                                label = { Text("Carga (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = repeticoesText,
                                onValueChange = { repeticoesText = it },
                                label = { Text("Reps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // BOTÃO SALVAR SÉRIE COM SMART CAST GARANTIDO
                        Button(
                            onClick = {
                                val idSessao = sessao.id
                                val exercicio = exercicioSelecionado
                                val idExercicio = exercicio?.id
                                val carga = cargaText.toDoubleOrNull()
                                val reps = repeticoesText.toIntOrNull()

                                // Ao verificar idSessao e idExercicio como variáveis locais val, o Kotlin converte para Long não-nulo
                                if (idSessao != null && exercicio != null && idExercicio != null && carga != null && reps != null) {
                                    viewModel.registrarSerie(
                                        idSessao = idSessao,
                                        idExercicio = idExercicio,
                                        carga = carga,
                                        repeticoes = reps,
                                        nomeExercicio = exercicio.nome
                                    )
                                    cargaText = ""
                                    repeticoesText = ""
                                }
                            },
                            enabled = exercicioSelecionado != null && cargaText.isNotBlank() && repeticoesText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "SALVAR SÉRIE",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Séries Realizadas (${series.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (series.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma série registrada ainda",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            ListaSeriesAgrupadas(
                                series = series,
                                listaExercicios = listaExercicios,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                sessao.id?.let { idSessao ->
                                    viewModel.finalizarTreino(idSessao)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                "FINALIZAR TREINO",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                is TreinoUiState.Erro -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.mensagem,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { viewModel.iniciarTreino(idUsuario) },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tentar Novamente", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeletorExercicio(
    listaExercicios: List<ExercicioEntity>,
    exercicioSelecionado: ExercicioEntity?,
    onExercicioSelecionado: (ExercicioEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = exercicioSelecionado?.nome ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Selecione o Exercício") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (listaExercicios.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nenhum exercício encontrado") },
                    onClick = { expanded = false }
                )
            } else {
                listaExercicios.forEach { exercicio ->
                    DropdownMenuItem(
                        text = { Text(exercicio.nome) },
                        onClick = {
                            onExercicioSelecionado(exercicio)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ListaSeriesAgrupadas(
    series: List<SerieTreinoResponseDTO>,
    listaExercicios: List<ExercicioEntity>,
    modifier: Modifier = Modifier
) {
    val seriesPorExercicio: Map<Long, List<SerieTreinoResponseDTO>> = series
        .groupByTo(linkedMapOf()) { it.idExercicio }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(seriesPorExercicio.toList()) { (idExercicio, seriesDoExercicio) ->
            val nomeDoDto = seriesDoExercicio.firstOrNull()?.nomeExercicio

            val nomeExercicio = if (!nomeDoDto.isNullOrBlank() && nomeDoDto != "Exercício") {
                nomeDoDto
            } else {
                listaExercicios.find { it.id == idExercicio }?.nome ?: "Exercício #$idExercicio"
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = nomeExercicio,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    seriesDoExercicio.forEachIndexed { index, serie ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Concluída",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Série ${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${serie.carga} kg  ×  ${serie.repeticoes} reps",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}