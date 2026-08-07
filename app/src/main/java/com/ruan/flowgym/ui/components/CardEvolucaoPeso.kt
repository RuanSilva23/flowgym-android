package com.ruan.flowgym.ui.screens.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.data.model.PesoResponseDTO
import kotlin.math.abs

@Composable
fun CardEvolucaoPeso(
    pesoAtual: Double?,
    pesoMeta: Double?,
    historicoPeso: List<PesoResponseDTO>,
    onRegistrarNovoPeso: (Double) -> Unit,
    onAtualizarMeta: (Double) -> Unit
) {
    var exibirDialogNovoPeso by remember { mutableStateOf(false) }
    var exibirDialogMeta by remember { mutableStateOf(false) }

    val corPrimaria = MaterialTheme.colorScheme.primary
    val corSecundaria = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho do Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = corPrimaria
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PESO CORPORAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = corPrimaria
                    )
                }

                IconButton(
                    onClick = { exibirDialogNovoPeso = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Registrar Peso",
                        tint = corPrimaria
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Linha com Peso Atual, Meta e Diferença
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (pesoAtual != null) "${"%.1f".format(pesoAtual)} kg" else "-- kg",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Atual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Card Múltiplo para Peso Meta
                Surface(
                    onClick = { exibirDialogMeta = true },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = corSecundaria,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (pesoMeta != null) "${"%.1f".format(pesoMeta)} kg" else "Definir",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Meta",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Status da Meta
            if (pesoAtual != null && pesoMeta != null) {
                val diff = pesoMeta - pesoAtual
                val textoStatus = when {
                    abs(diff) < 0.2 -> "Meta atingida! 🎉"
                    diff > 0 -> "Faltam ${"%.1f".format(diff)} kg para sua meta"
                    else -> "Faltam ${"%.1f".format(abs(diff))} kg para reduzir até a meta"
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = textoStatus,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = corPrimaria
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GRÁFICO CANVAS VETORIAL DE EVOLUÇÃO
            if (historicoPeso.size >= 2) {
                GraficoEvolucaoPesoCanvas(
                    historico = historicoPeso,
                    corLinha = corPrimaria,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            } else {
                Text(
                    text = "Registre pelo menos 2 medições de peso para visualizar o gráfico de tendência.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }

    // Modal para Adicionar Registro de Peso
    if (exibirDialogNovoPeso) {
        DialogSalvarPeso(
            titulo = "Registrar Peso Corporal",
            valorInicial = pesoAtual ?: 70.0,
            onDismiss = { exibirDialogNovoPeso = false },
            onConfirmar = { valor ->
                onRegistrarNovoPeso(valor)
                exibirDialogNovoPeso = false
            }
        )
    }

    // Modal para Atualizar Meta de Peso
    if (exibirDialogMeta) {
        DialogSalvarPeso(
            titulo = "Definir Peso Meta",
            valorInicial = pesoMeta ?: (pesoAtual ?: 70.0),
            onDismiss = { exibirDialogMeta = false },
            onConfirmar = { valor ->
                onAtualizarMeta(valor)
                exibirDialogMeta = false
            }
        )
    }
}

// 👈 GRÁFICO CUSTOMIZADO VIA CANVAS COM DEGRADÊ
@Composable
private fun GraficoEvolucaoPesoCanvas(
    historico: List<PesoResponseDTO>,
    corLinha: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val pesos = historico.mapNotNull { it.pesoCorporal }
        val minPeso = pesos.minOrNull() ?: 0.0
        val maxPeso = pesos.maxOrNull() ?: 1.0
        val margem = if (maxPeso == minPeso) 1.0 else (maxPeso - minPeso) * 0.2

        val minY = (minPeso - margem).toFloat()
        val maxY = (maxPeso + margem).toFloat()

        val pontosX = historico.indices.map { index ->
            index * (width / (historico.size - 1).coerceAtLeast(1))
        }

        val pontosY = pesos.map { peso ->
            height - ((peso.toFloat() - minY) / (maxY - minY)) * height
        }

        val caminhoLinha = Path().apply {
            moveTo(pontosX[0], pontosY[0])
            for (i in 1 until pontosX.size) {
                val x1 = (pontosX[i - 1] + pontosX[i]) / 2f
                val y1 = pontosY[i - 1]
                val x2 = (pontosX[i - 1] + pontosX[i]) / 2f
                val y2 = pontosY[i]
                cubicTo(x1, y1, x2, y2, pontosX[i], pontosY[i])
            }
        }

        val caminhoPreenchimento = Path().apply {
            addPath(caminhoLinha)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Preenchimento em degradê sob o gráfico
        drawPath(
            path = caminhoPreenchimento,
            brush = Brush.verticalGradient(
                colors = listOf(
                    corLinha.copy(alpha = 0.35f),
                    Color.Transparent
                )
            )
        )

        // Traço do Gráfico
        drawPath(
            path = caminhoLinha,
            color = corLinha,
            style = Stroke(width = 3.dp.toPx())
        )

        // Desenhar os pontos das medições
        for (i in pontosX.indices) {
            drawCircle(
                color = corLinha,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(pontosX[i], pontosY[i])
            )
        }
    }
}

// 👈 DIALOG COMPARTILHADO PARA NOVO PESO E META
@Composable
private fun DialogSalvarPeso(
    titulo: String,
    valorInicial: Double,
    onDismiss: () -> Unit,
    onConfirmar: (Double) -> Unit
) {
    var textValue by remember { mutableStateOf(TextFieldValue(valorInicial.toString())) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = titulo, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("Peso (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val valor = textValue.text.toDoubleOrNull()
                    if (valor != null && valor > 0.0) {
                        onConfirmar(valor)
                    }
                }
            ) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}