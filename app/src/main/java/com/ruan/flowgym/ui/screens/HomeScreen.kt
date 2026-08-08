package com.ruan.flowgym.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.ruan.flowgym.data.model.SessaoTreinoResponseDTO
import com.ruan.flowgym.ui.screens.components.CardEvolucaoPeso
import com.ruan.flowgym.ui.viewmodel.HomeUiState
import com.ruan.flowgym.ui.viewmodel.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    nomeUsuario: String = "Ruan",
    idUsuario: Long = 1L,
    viewModel: HomeViewModel = hiltViewModel(),
    onIniciarTreinoClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var exibirModalHistoricoCompleto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.carregarDadosHome(idUsuario)
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
                        Button(onClick = { viewModel.carregarDadosHome(idUsuario) }) {
                            Text("Tentar Novamente")
                        }
                    }
                }

                is HomeUiState.Sucesso -> {
                    val sessoes = state.historicoSessoes
                    // 👈 LIMITE DE 5 TREINOS NO DASHBOARD
                    val ultimasSessoes = sessoes.take(5)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        // 1. Header de Saudação
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

                        // 2. 👈 NOVO: Card Motivacional Diário
                        item {
                            CardMotivacionalDiario()
                        }

                        // Dentro da LazyColumn da HomeScreen.kt:

                        item {
                            CardEvolucaoPeso(
                                pesoAtual = state.pesoAtual, // Ex: vindo do HomeUiState
                                pesoMeta = state.pesoMeta,
                                historicoPeso = state.historicoPeso,
                                onRegistrarNovoPeso = { novoPeso ->
                                    viewModel.registrarNovoPeso(idUsuario, novoPeso)
                                },
                                onAtualizarMeta = { novaMeta ->
                                    viewModel.atualizarMetaPeso(idUsuario, novaMeta)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }





                        // 3. Banner para Iniciar Treino
                        item {
                            CardNovoTreino(onIniciarClick = onIniciarTreinoClick)
                        }

                        // 4. Cabeçalho de Histórico
                        item {
                            Text(
                                text = "Últimos Treinos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 5. Lista de no máximo 5 treinos
                        if (ultimasSessoes.isEmpty()) {
                            item {
                                Text(
                                    text = "Nenhum treino realizado ainda.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(ultimasSessoes) { sessao ->
                                ItemSessaoHistorico(
                                    sessao = sessao,
                                    onDeletarSessao = { idSessao ->
                                        viewModel.deletarSessao(idSessao, idUsuario)
                                    }
                                )
                            }

                            // 6. 👈 Botão "Ver Mais" quando houver mais de 5 treinos
                            if (sessoes.size > 5) {
                                item {
                                    OutlinedButton(
                                        onClick = { exibirModalHistoricoCompleto = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Ver histórico completo (${sessoes.size} treinos)",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Modal de Histórico Completo
                    if (exibirModalHistoricoCompleto) {
                        AlertDialog(
                            onDismissRequest = { exibirModalHistoricoCompleto = false },
                            title = { Text("Histórico Completo", fontWeight = FontWeight.Bold) },
                            text = {
                                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(sessoes) { sessao ->
                                            ItemSessaoHistorico(
                                                sessao = sessao,
                                                onDeletarSessao = { idSessao ->
                                                    viewModel.deletarSessao(idSessao, idUsuario)
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { exibirModalHistoricoCompleto = false }) {
                                    Text("Fechar")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// 👈 COMPONENTE: Card Motivacional com Mensagens Diárias
@Composable
fun CardMotivacionalDiario() {
    val frasesMotivacionais = remember {
        listOf(
            "O único treino ruim é aquele que não aconteceu.",
            "Consistência vence o talento quando o talento não tem consistência.",
            "Cada repetição te deixa mais perto do seu objetivo.",
            "A dor do treino é temporária, mas o orgulho é para sempre.",
            "Não conte os dias, faça os dias contarem.",
            "Sua única competição é quem você foi ontem.",
            "Pequenos progressos diários resultam em grandes conquistas.",
            "A disciplina te leva aonde a motivação não consegue chegar.",
            "Sem Dor, Sem Ganhos(NO PAIN, NO GAIN)"
        )
    }

    // Calcula o índice com base no dia do ano (troca automaticamente à meia-noite)
    val diaDoAno = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val fraseHoje = frasesMotivacionais[diaDoAno % frasesMotivacionais.size]

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "MOTIVAÇÃO DO DIA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"$fraseHoje\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                text = "Iniciar Sessão de Treino",
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
        onClick = { expandido = !expandido }, // 👈 Usar o onClick nativo do Card (Material 3)
        modifier = Modifier.fillMaxWidth(),
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

                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { exibirDialogExclusao = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir este treino", fontWeight = FontWeight.Bold)
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