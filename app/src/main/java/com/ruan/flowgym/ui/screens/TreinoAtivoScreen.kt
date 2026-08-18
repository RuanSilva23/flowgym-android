package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.ruan.flowgym.ui.components.TimerDescansoCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruan.flowgym.data.model.SerieTreinoResponseDTO
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoUiState

@Composable
fun TreinoAtivoScreen(
    treinoViewModel: TreinoAtivoViewModel = hiltViewModel(),
    onFinalizarTreinoClick: () -> Unit = {}
) {
    val uiState by treinoViewModel.uiState.collectAsState()
    val tempoRestante by treinoViewModel.tempoRestante.collectAsState()
    val tempoTotal by treinoViewModel.tempoTotalDescanso.collectAsState()

    var serieParaEditar by remember { mutableStateOf<SerieTreinoResponseDTO?>(null) }

    var tabSelecionada by remember { mutableIntStateOf(0) }
    var exercicioIndexAtual by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is TreinoUiState.Idle -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum treino ativo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Acesse a aba 'Minhas Fichas' e selecione uma ficha para começar.",
                            style = MaterialTheme.typography.bodyMedium,
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
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.mensagem,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is TreinoUiState.Sucesso -> {
                    val rotina = state.rotinaAtiva
                    val todosItens = rotina?.itens.orEmpty()

                    val listaItensFicha = remember(todosItens, state.ordemExerciciosIds) {
                        if (state.ordemExerciciosIds.isNotEmpty()) {
                            val mapa = todosItens.associateBy { it.exercicio.id ?: 0L }
                            state.ordemExerciciosIds.mapNotNull { mapa[it] }
                        } else {
                            todosItens.sortedBy { it.item.ordem }
                        }
                    }

                    val idSessao: Long = state.sessao.id ?: state.sessaoLocalId ?: 0L

                    if (exercicioIndexAtual >= listaItensFicha.size && listaItensFicha.isNotEmpty()) {
                        exercicioIndexAtual = listaItensFicha.size - 1
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (tabSelecionada == 0) {
                                // ABA 1: EXERCÍCIO ATUAL
                                if (listaItensFicha.isNotEmpty()) {
                                    val itemAtual = listaItensFicha[exercicioIndexAtual]
                                    val exercicioObj = itemAtual.exercicio
                                    val fichaItem = itemAtual.item

                                    val idExercicioAtual: Long = exercicioObj.id ?: 0L
                                    val seriesAlvo: Int = fichaItem.seriesAlvo ?: 0
                                    val repsAlvo: Int = fichaItem.repeticoesAlvo ?: 0
                                    val cargaAlvo: Double = fichaItem.cargaAlvo ?: 0.0
                                    val tempoDescansoAlvo: Int = fichaItem.descansoSeg ?: 60

                                    val grupoBiSetAtual = fichaItem.grupoBiSet
                                    val ehBiSet = grupoBiSetAtual != null && grupoBiSetAtual > 0
                                    val exerciciosMesmoBiSet = listaItensFicha.filter { it.item.grupoBiSet == grupoBiSetAtual && ehBiSet}
                                    val ehUltimoBiSet = exerciciosMesmoBiSet.lastOrNull()?.exercicio?.id == idExercicioAtual

                                    val seriesRegistradasExercicio = state.series.filter {
                                        it.idExercicio == idExercicioAtual
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        // Navegador de Exercícios
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { if (exercicioIndexAtual > 0) exercicioIndexAtual-- },
                                                enabled = exercicioIndexAtual > 0
                                            ) {
                                                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                                            }

                                            Text(
                                                text = "EXERCÍCIO ${exercicioIndexAtual + 1} DE ${listaItensFicha.size}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            IconButton(
                                                onClick = { if (exercicioIndexAtual < listaItensFicha.size - 1) exercicioIndexAtual++ },
                                                enabled = exercicioIndexAtual < listaItensFicha.size - 1
                                            ) {
                                                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo")
                                            }
                                        }

                                        // Banner do Exercício
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                BadgesTreino(
                                                    ehAquecimento = fichaItem.aquecimento,
                                                    grupoBiSet = fichaItem.grupoBiSet
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = exercicioObj.nome ?: "Exercício",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Meta: ${seriesAlvo} séries x ${repsAlvo} reps | Carga: ${cargaAlvo} kg",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Cronômetro
                                        TimerDescansoCard(
                                            tempoRestante = tempoRestante,
                                            tempoTotal = tempoTotal,
                                            onAdicionarDezSegundos = { treinoViewModel.adicionarTempoDescanso(10) },
                                            onPular = { treinoViewModel.pularTimerDescanso() }
                                        )

                                        if (tempoRestante > 0) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        // Formulário de Registro
                                        FormularioSerieRestrito(
                                            bloqueadoPorDescanso = tempoRestante > 0,
                                            tempoRestante = tempoRestante,
                                            cargaPadrao = cargaAlvo,
                                            repsPadrao = repsAlvo,
                                            onRegistrarSerie = { carga, reps ->
                                                treinoViewModel.registrarSerie(
                                                    idSessao = idSessao,
                                                    idExercicio = idExercicioAtual,
                                                    carga = carga,
                                                    repeticoes = reps,
                                                    nomeExercicio = exercicioObj.nome ?: "Exercício",
                                                    tempoDescansoAlvo = tempoDescansoAlvo,
                                                    ehBiSet = ehBiSet,
                                                    ehUltimoBiSet = ehUltimoBiSet,
                                                    ehAquecimento = fichaItem.aquecimento,
                                                    grupoBiSet = fichaItem.grupoBiSet
                                                )
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Séries Realizadas
                                        Text(
                                            text = "Séries Realizadas (${seriesRegistradasExercicio.size}/$seriesAlvo)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (seriesRegistradasExercicio.isEmpty()) {
                                                item {
                                                    Text(
                                                        text = "Nenhuma série registrada para este exercício ainda.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            } else {
                                                itemsIndexed(seriesRegistradasExercicio) { index, serie ->
                                                    Card(
                                                        onClick = { serieParaEditar = serie },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(12.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Text(
                                                                    text = "Série ${index + 1}",
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            Text(
                                                                text = "${serie.carga} kg  x  ${serie.repeticoes} reps",
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
                            } else {
                                // ABA 2: FILA DO TREINO COM REORDENAÇÃO
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "Fila do Treino (Altere a ordem se o aparelho estiver ocupado)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    itemsIndexed(listaItensFicha) { index, itemEx ->
                                        val ex = itemEx.exercicio
                                        val fichaItem = itemEx.item
                                        val idExercicioItem: Long = ex.id ?: 0L
                                        val targetSeries: Int = fichaItem.seriesAlvo ?: 0

                                        val qtdSeriesFeitas = state.series.count { it.idExercicio == idExercicioItem }
                                        val concluido = qtdSeriesFeitas >= targetSeries && targetSeries > 0

                                        Card(
                                            onClick = {
                                                exercicioIndexAtual = index
                                                tabSelecionada = 0
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (index == exercicioIndexAtual)
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${index + 1}. ${ex.nome ?: "Exercício"}",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (index == exercicioIndexAtual) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            SuggestionChip(
                                                                onClick = {},
                                                                label = { Text("EM FOCO", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "Meta: ${targetSeries}x ${fichaItem.repeticoesAlvo ?: 0} reps | ${fichaItem.cargaAlvo ?: 0.0} kg",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (concluido) {
                                                        Text(
                                                            text = "✅ Concluído",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "$qtdSeriesFeitas/$targetSeries séries",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                // Botões Subir e Descer
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = {
                                                            treinoViewModel.moverExercicioParaCima(index)
                                                            if (exercicioIndexAtual == index) exercicioIndexAtual--
                                                            else if (exercicioIndexAtual == index - 1) exercicioIndexAtual++
                                                        },
                                                        enabled = index > 0
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowUp,
                                                            contentDescription = "Subir ordem",
                                                            tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            treinoViewModel.moverExercicioParaBaixo(index)
                                                            if (exercicioIndexAtual == index) exercicioIndexAtual++
                                                            else if (exercicioIndexAtual == index + 1) exercicioIndexAtual--
                                                        },
                                                        enabled = index < listaItensFicha.size - 1
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowDown,
                                                            contentDescription = "Descer ordem",
                                                            tint = if (index < listaItensFicha.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Painel Inferior
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = state.sessao.nomeRotina ?: "Treino em Andamento",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Sessão #$idSessao",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            treinoViewModel.finalizarTreino(idSessao)
                                            onFinalizarTreinoClick()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Finalizar", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TabRow(
                                    selectedTabIndex = tabSelecionada,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Tab(
                                        selected = tabSelecionada == 0,
                                        onClick = { tabSelecionada = 0 },
                                        text = { Text("Exercício Atual", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
                                    )
                                    Tab(
                                        selected = tabSelecionada == 1,
                                        onClick = { tabSelecionada = 1 },
                                        text = { Text("Fila do Treino (${listaItensFicha.size})", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            serieParaEditar?.let { serie ->
                DialogEditarSerie(
                    serie = serie,
                    onDismiss = { serieParaEditar = null },
                    onConfirmar = { novaCarga, novasReps ->
                        serie.id?.let { id ->
                            treinoViewModel.editarSerie(id, novaCarga, novasReps)
                        }
                        serieParaEditar = null
                    }
                )
            }
        }
    }
}

@Composable
fun FormularioSerieRestrito(
    bloqueadoPorDescanso: Boolean,
    tempoRestante: Int,
    cargaPadrao: Double,
    repsPadrao: Int,
    onRegistrarSerie: (Double, Int) -> Unit
) {
    var cargaText by remember(cargaPadrao) { mutableStateOf(TextFieldValue(cargaPadrao.toString())) }
    var repsText by remember(repsPadrao) { mutableStateOf(TextFieldValue(repsPadrao.toString())) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "REGISTRAR NOVA SÉRIE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = cargaText,
                    onValueChange = { cargaText = it },
                    label = { Text("Carga (kg)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !bloqueadoPorDescanso
                )

                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !bloqueadoPorDescanso
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val carga = cargaText.text.toDoubleOrNull() ?: cargaPadrao
                    val reps = repsText.text.toIntOrNull() ?: repsPadrao
                    onRegistrarSerie(carga, reps)
                },
                enabled = !bloqueadoPorDescanso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (bloqueadoPorDescanso) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AGUARDE O DESCANSO (${tempoRestante}s)",
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONCLUIR SÉRIE",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DialogEditarSerie(
    serie: SerieTreinoResponseDTO,
    onDismiss: () -> Unit,
    onConfirmar: (Double, Int) -> Unit
) {
    var cargaText by remember { mutableStateOf(TextFieldValue((serie.carga ?: 0.0).toString())) }
    var repsText by remember { mutableStateOf(TextFieldValue((serie.repeticoes ?: 0).toString())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Série", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cargaText,
                    onValueChange = { cargaText = it },
                    label = { Text("Carga (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("Repetições") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val carga = cargaText.text.toDoubleOrNull() ?: (serie.carga ?: 0.0)
                val reps = repsText.text.toIntOrNull() ?: (serie.repeticoes ?: 0)
                onConfirmar(carga, reps)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BadgesTreino(
    ehAquecimento: Boolean,
    grupoBiSet: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (ehAquecimento) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = "🔥 AQUECIMENTO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                border = null
            )
        }

        if (grupoBiSet != null && grupoBiSet > 0) {
            SuggestionChip(
                onClick = {},
                label = {
                    Text(
                        text = "⚡ BI-SET #$grupoBiSet",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                border = null
            )
        }
    }
}