package com.ruan.apexlift.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruan.apexlift.data.local.entity.ExercicioEntity
import com.ruan.apexlift.data.model.ItemFichaRequestDTO
import com.ruan.apexlift.ui.components.DialogSelecionarExercicio

@Composable
fun CriarFichaDialog(
    exerciciosDisponiveis: List<ExercicioEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<ItemFichaRequestDTO>) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    val itensCriacao = remember {
        mutableStateListOf<EditableItemFicha>().apply {
            add(EditableItemFicha())
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Ficha de Treino", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Ficha") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição / Foco") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exercícios (${itensCriacao.size})", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { itensCriacao.add(EditableItemFicha()) }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Exercício")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(itensCriacao) { index, item ->
                        CardItemFichaCriacao(
                            index = index + 1,
                            item = item,
                            exerciciosDisponiveis = exerciciosDisponiveis,
                            onRemover = { if (itensCriacao.size > 1) itensCriacao.removeAt(index) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val requestItens = itensCriacao.mapIndexed { index, item ->
                        ItemFichaRequestDTO(
                            idExercicio = item.idExercicio,
                            ordem = index + 1,
                            seriesAlvo = item.series.toIntOrNull() ?: 4,
                            repeticoesAlvo = item.reps.toIntOrNull() ?: 10,
                            cargaAlvo = item.carga.toDoubleOrNull() ?: 0.0,
                            descanso = item.descanso.toIntOrNull() ?: 60,
                            aquecimento = item.aquecimento,
                            grupoBiSet = item.grupoBiSet.toIntOrNull()
                        )
                    }
                    onConfirm(nome, descricao, requestItens)
                },
                enabled = nome.isNotBlank() && itensCriacao.any { it.idExercicio > 0L }
            ) {
                Text("Criar Ficha")
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
fun CardItemFichaCriacao(
    index: Int,
    item: EditableItemFicha,
    exerciciosDisponiveis: List<ExercicioEntity>,
    onRemover: () -> Unit
) {
    var exibindoModalBusca by remember { mutableStateOf(false) }
    val exercicioSelecionado = exerciciosDisponiveis.find { it.id == item.idExercicio }
    val exercicioNome = exercicioSelecionado?.nome ?: "Selecionar Exercício..."

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#$index", fontWeight = FontWeight.Bold)

                OutlinedCard(
                    onClick = { exibindoModalBusca = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercicioNome,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (exercicioSelecionado != null)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            if (exercicioSelecionado?.grupoMuscular != null) {
                                Text(
                                    text = exercicioSelecionado.grupoMuscular.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar Exercício",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onRemover) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.series,
                    onValueChange = { item.series = it },
                    label = { Text("Séries") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.reps,
                    onValueChange = { item.reps = it },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.carga,
                    onValueChange = { item.carga = it },
                    label = { Text("Kg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = item.descanso,
                    onValueChange = { item.descanso = it },
                    label = { Text("Seg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = item.aquecimento,
                    onClick = { item.aquecimento = !item.aquecimento },
                    label = { Text("🔥 Aquecimento", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )

                OutlinedTextField(
                    value = item.grupoBiSet,
                    onValueChange = { item.grupoBiSet = it },
                    label = { Text("Bi-Set nº") },
                    placeholder = { Text("Ex: 1") },
                    modifier = Modifier.width(110.dp),
                    singleLine = true
                )
            }
        }
    }

    if (exibindoModalBusca) {
        DialogSelecionarExercicio(
            exerciciosDisponiveis = exerciciosDisponiveis,
            onExercicioSelecionado = { selecionado ->
                item.idExercicio = selecionado.id ?: 0L
            },
            onDismiss = { exibindoModalBusca = false }
        )
    }
}