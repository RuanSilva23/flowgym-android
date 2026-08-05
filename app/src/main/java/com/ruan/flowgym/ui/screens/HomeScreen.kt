package com.ruan.flowgym.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.ui.viewmodel.HomeUiState
import com.ruan.flowgym.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    nomeUsuario: String = "Ruan",
    idUsuario: Long = 1L,
    homeViewModel: HomeViewModel = viewModel(),
    onIniciarTreinoClick: () -> Unit = {}
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.carregarDadosHome(idUsuario)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is HomeUiState.Erro -> {
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { homeViewModel.carregarDadosHome(idUsuario) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }

                is HomeUiState.Sucesso -> {
                    val sessoes = state.historicoSessoes

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        // Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Fala, $nomeUsuario! 👋",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Pronto para esmagar hoje?",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notificações",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Banner para Novo Treino
                        item {
                            CardNovoTreino(onIniciarClick = onIniciarTreinoClick)
                        }

                        // Cabeçalho de Histórico
                        item {
                            Text(
                                text = "Histórico de Treinos (${sessoes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (sessoes.isEmpty()) {
                            item {
                                Text(
                                    text = "Nenhum treino realizado ainda.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(sessoes) { sessao ->
                                ItemSessaoHistorico(
                                    sessao = sessao,
                                    onDeletarSessao = { idSessao ->
                                        homeViewModel.deletarSessao(idSessao, idUsuario)
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

@Composable
fun CardNovoTreino(onIniciarClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "TREINO DO DIA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Iniciar Nova Sessão",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onIniciarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("INICIAR AGORA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ItemSessaoHistorico(
    sessao: SessaoTreinoResponseDTO,
    onDeletarSessao: (Long) -> Unit = {}
) {
    var expandido by remember { mutableStateOf(false) }
    var exibirDialogExclusao by remember { mutableStateOf(false) }

    val idSessao = sessao.id ?: 0L
    val dataFormatada = sessao.dataHoraInicio?.take(16)?.replace("T", " ")
        ?: sessao.dataInicio?.take(16)?.replace("T", " ")
        ?: "Data não informada"
    val tituloTreino = sessao.nomeRotina?.takeIf { it.isNotBlank() } ?: "Sessão #$idSessao"
    val listaSeries = sessao.series.orEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tituloTreino,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Início: $dataFormatada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { exibirDialogExclusao = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Treino",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (listaSeries.isEmpty()) {
                        Text(
                            text = "Nenhum exercício registrado nesta sessão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val exerciciosAgrupados = listaSeries.groupBy { serie ->
                            val nome = serie.nomeExercicio
                            if (!nome.isNullOrBlank() && nome != "Exercício") nome else "Exercício #${serie.idExercicio}"
                        }

                        exerciciosAgrupados.forEach { (nomeExercicio, series) ->
                            Text(
                                text = nomeExercicio,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            series.forEachIndexed { index, serie ->
                                val carga = serie.carga ?: 0.0
                                val reps = serie.repeticoes ?: 0
                                Text(
                                    text = "  • Série ${index + 1}: ${carga} kg x $reps reps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }

    if (exibirDialogExclusao) {
        AlertDialog(
            onDismissRequest = { exibirDialogExclusao = false },
            title = { Text("Excluir Treino") },
            text = { Text("Tem certeza que deseja apagar este treino do histórico? Essa ação não poderá ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        exibirDialogExclusao = false
                        onDeletarSessao(idSessao)
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { exibirDialogExclusao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}