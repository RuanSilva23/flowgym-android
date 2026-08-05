package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.ItemFichaRequestDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarFichaDialog(
    exerciciosDisponiveis: List<ExercicioEntity>,
    onDismiss: () -> Unit,
    onConfirm: (nome: String, descricao: String, itens: List<ItemFichaRequestDTO>) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    // Lista mutável observável de exercícios adicionados na ficha
    val itensFormState = remember {
        mutableStateListOf(ItemFichaFormState())
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Nova Ficha de Treino",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Área rolável do formulário
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Cabeçalho da Ficha
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome da Ficha (ex: Treino A)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descricao,
                        onValueChange = { descricao = it },
                        label = { Text("Descrição / Foco (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exercícios (${itensFormState.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { itensFormState.add(ItemFichaFormState()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Exercício")
                        }
                    }

                    // Linhas de Exercícios Dinâmicos
                    itensFormState.forEachIndexed { index, item ->
                        ItemExercicoRow(
                            index = index,
                            itemState = item,
                            exerciciosDisponiveis = exerciciosDisponiveis,
                            onUpdate = { novoEstado -> itensFormState[index] = novoEstado },
                            onDelete = {
                                if (itensFormState.size > 1) {
                                    itensFormState.removeAt(index)
                                }
                            },
                            canDelete = itensFormState.size > 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões de Ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    val itensDTO = itensFormState.mapIndexedNotNull { idx, itm -> itm.toDTO(idx + 1) }
                    val isValid = nome.isNotBlank() && itensDTO.size == itensFormState.size

                    Button(
                        onClick = {
                            if (isValid) {
                                onConfirm(nome, descricao, itensDTO)
                            }
                        },
                        enabled = isValid
                    ) {
                        Text("Criar Ficha")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemExercicoRow(
    index: Int,
    itemState: ItemFichaFormState,
    exerciciosDisponiveis: List<ExercicioEntity>,
    onUpdate: (ItemFichaFormState) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Dropdown de Seleção de Exercício
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = itemState.exercicioSelecionado?.nome ?: "Selecione o Exercício",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        exerciciosDisponiveis.forEach { exercicio ->
                            DropdownMenuItem(
                                text = { Text(exercicio.nome) },
                                onClick = {
                                    onUpdate(itemState.copy(exercicioSelecionado = exercicio))
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inputs de Séries, Repetições, Carga e Descanso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = itemState.series,
                    onValueChange = { onUpdate(itemState.copy(series = it)) },
                    label = { Text("Séries") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = itemState.repeticoes,
                    onValueChange = { onUpdate(itemState.copy(repeticoes = it)) },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = itemState.carga,
                    onValueChange = { onUpdate(itemState.copy(carga = it)) },
                    label = { Text("Kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = itemState.descanso,
                    onValueChange = { onUpdate(itemState.copy(descanso = it)) },
                    label = { Text("Seg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}