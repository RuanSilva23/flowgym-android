package com.ruan.apexlift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ruan.apexlift.data.model.SerieTreinoResponseDTO
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.compareTo

@Composable
fun ResumoTreinoDialog(
    nomeRotina: String,
    dataHoraInicio: String?,
    seriesRealizadas: List<SerieTreinoResponseDTO>,
    onConfirmarConclusao: () -> Unit
) {
    // 1. Cálculo de Volume Total (Tonelagem)
    val volumeTotalKg = remember(seriesRealizadas) {
        seriesRealizadas.sumOf { (it.carga ?: 0.0) * (it.repeticoes ?: 0) }
    }

    // 2. Total de Séries e Exercícios Distintos
    val totalSeries = seriesRealizadas.size
    val totalExerciciosDistintos = remember(seriesRealizadas) {
        seriesRealizadas.map { it.idExercicio }.distinct().size
    }

    // 3. Cálculo de Duração
    val duracaoFormatada = remember(dataHoraInicio) {
        calcularDuracao(dataHoraInicio)
    }

    // 4. Agrupamento de Séries por Exercício para o Detalhamento
    val exerciciosAgrupados = remember(seriesRealizadas) {
        seriesRealizadas.groupBy { it.nomeExercicio.ifBlank { "Exercício #${it.idExercicio}" } }
    }

    Dialog(
        onDismissRequest = onConfirmarConclusao,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Troféu de Celebração
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TREINO FINALIZADO!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = nomeRotina,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid de Métricas Principais
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardMetricaResumo(
                        titulo = "VOLUME TOTAL",
                        valor = if (volumeTotalKg >= 1000.0) {
                            String.format(Locale.getDefault(), "%.2f t", volumeTotalKg / 1000.0)
                        } else {
                            String.format(Locale.getDefault(), "%.0f kg", volumeTotalKg)
                        },
                        subtitulo = "${String.format(Locale.getDefault(), "%.0f", volumeTotalKg)} kg totais",
                        icone = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )

                    CardMetricaResumo(
                        titulo = "DURAÇÃO",
                        valor = duracaoFormatada,
                        subtitulo = "tempo de sessão",
                        icone = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardMetricaResumo(
                        titulo = "SÉRIES FEITAS",
                        valor = "$totalSeries",
                        subtitulo = "concluídas",
                        icone = Icons.Default.Repeat,
                        modifier = Modifier.weight(1f)
                    )

                    CardMetricaResumo(
                        titulo = "EXERCÍCIOS",
                        valor = "$totalExerciciosDistintos",
                        subtitulo = "executados",
                        icone = Icons.Default.Check,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "RESUMO DOS EXERCÍCIOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de Exercícios e Cargas do Treino
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exerciciosAgrupados.entries.toList()) { (nomeEx, series) ->
                        val maxCarga = series.maxOfOrNull { it.carga ?: 0.0 } ?: 0.0
                        val totalReps = series.sumOf { it.repeticoes ?: 0 }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nomeEx,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${series.size} séries • $totalReps reps totais",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Máx: ${maxCarga}kg",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão de Conclusão
                Button(
                    onClick = onConfirmarConclusao,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONCLUIR E VOLTAR AO INÍCIO",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CardMetricaResumo(
    titulo: String,
    valor: String,
    subtitulo: String,
    icone: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun calcularDuracao(dataHoraInicioStr: String?): String {
    if (dataHoraInicioStr.isNullOrBlank()) return "30 min"

    return try {
        val formatoLimpo = dataHoraInicioStr.take(19).replace("T", " ")
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val inicio = formato.parse(formatoLimpo) ?: return "45 min"
        val agora = Date()

        val diffMs = agora.time - inicio.time
        val minutosTotais = (diffMs / (1000 * 60)).toInt()

        if (minutosTotais < 1) {
            "1 min"
        } else if (minutosTotais >= 60) {
            val horas = minutosTotais / 60
            val minutos = minutosTotais % 60
            "${horas}h ${minutos}m"
        } else {
            "$minutosTotais min"
        }
    } catch (_: Exception) {
        "40 min"
    }
}