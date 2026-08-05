package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.ItemFichaRequestDTO
import com.ruan.flowgym.data.model.RotinaResponseDTO
import com.ruan.flowgym.ui.viewmodel.FichaUiState
import com.ruan.flowgym.ui.viewmodel.FichaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichasScreen(
    viewModel: FichaViewModel,
    idUsuario: Long = 1L,
    onRotinaClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var exibindoModalCriacao by remember { mutableStateOf(false) }
    var fichaParaEditar by remember { mutableStateOf<RotinaResponseDTO?>(null) }
    var fichaParaDeletar by remember { mutableStateOf<Long?>(null) }

    val exerciciosDisponiveis by viewModel.exerciciosDisponiveis.collectAsState()

    val exerciciosEntityList = remember(exerciciosDisponiveis) {
        exerciciosDisponiveis.map { dto ->
            ExercicioEntity(
                id = dto.id ?: 0L,
                nome = dto.nome ?: "",
                grupoMuscular = dto.grupoMuscular ?: ""
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.carregarFichas(idUsuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Minhas Fichas de Treino", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { exibindoModalCriacao = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Ficha")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is FichaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FichaUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FichaUiState.Success -> {
                    if (state.rotinas.isEmpty()) {
                        Text(
                            text = "Nenhuma ficha cadastrada ainda.",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.rotinas) { rotina ->
                                ItemFichaCard(
                                    rotina = rotina,
                                    onClick = { rotina.id?.let { idRotina -> onRotinaClick(idRotina) } },
                                    onEditarClick = { fichaParaEditar = rotina },
                                    onDeletarClick = { rotina.id?.let { idRotina -> fichaParaDeletar = idRotina } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (exibindoModalCriacao) {
        CriarFichaDialog(
            exerciciosDisponiveis = exerciciosEntityList,
            onDismiss = { exibindoModalCriacao = false },
            onConfirm = { nome, descricao, itens ->
                viewModel.criarFicha(
                    idUsuario = idUsuario,
                    nome = nome,
                    descricao = descricao,
                    itens = itens,
                    onSuccess = { exibindoModalCriacao = false },
                    onError = { }
                )
            }
        )
    }

    fichaParaEditar?.let { rotina ->
        EditarFichaDialog(
            rotina = rotina,
            exerciciosDisponiveis = exerciciosEntityList,
            onDismiss = { fichaParaEditar = null },
            onConfirm = { nome, descricao, itens ->
                rotina.id?.let { idRotina ->
                    viewModel.editarFicha(
                        idFicha = idRotina,
                        idUsuario = idUsuario,
                        nome = nome,
                        descricao = descricao,
                        itens = itens
                    )
                }
                fichaParaEditar = null
            }
        )
    }

    fichaParaDeletar?.let { idFicha ->
        AlertDialog(
            onDismissRequest = { fichaParaDeletar = null },
            title = { Text("Excluir Ficha") },
            text = { Text("Deseja realmente apagar esta ficha de treino? As sessões já concluídas no histórico permanecerão salvas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletarFicha(idFicha, idUsuario)
                        fichaParaDeletar = null
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fichaParaDeletar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ItemFichaCard(
    rotina: RotinaResponseDTO,
    onClick: () -> Unit,
    onEditarClick: () -> Unit,
    onDeletarClick: () -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }

    val nome = rotina.nome ?: "Ficha sem nome"
    val descricao = rotina.descricao
    val totalExercicios = rotina.exercicios?.size ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!descricao.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descricao,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$totalExercicios exercícios cadastrados",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box {
                IconButton(onClick = { menuExpandido = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opções da Ficha"
                    )
                }

                DropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar Ficha") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpandido = false
                            onEditarClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir Ficha", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpandido = false
                            onDeletarClick()
                        }
                    )
                }
            }
        }
    }
}

class EditableItemFicha(
    idExercicio: Long = 0L,
    series: String = "4",
    reps: String = "10",
    carga: String = "20.0",
    descanso: String = "60"
) {
    var idExercicio by mutableStateOf(idExercicio)
    var series by mutableStateOf(series)
    var reps by mutableStateOf(reps)
    var carga by mutableStateOf(carga)
    var descanso by mutableStateOf(descanso)
}

@Composable
fun EditarFichaDialog(
    rotina: RotinaResponseDTO,
    exerciciosDisponiveis: List<ExercicioEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<ItemFichaRequestDTO>) -> Unit
) {
    var nome by remember { mutableStateOf(rotina.nome ?: "") }
    var descricao by remember { mutableStateOf(rotina.descricao ?: "") }

    val itensEdicao = remember {
        mutableStateListOf<EditableItemFicha>().apply {
            val exList = rotina.exercicios.orEmpty()
            if (exList.isNotEmpty()) {
                exList.forEach { ex ->
                    add(
                        EditableItemFicha(
                            idExercicio = ex.idExercicio ?: 0L,
                            series = (ex.seriesAlvo ?: 4).toString(),
                            reps = (ex.repeticoesAlvo ?: 10).toString(),
                            carga = (ex.cargaAlvo ?: 0.0).toString(),
                            descanso = (ex.descanso ?: 60).toString()
                        )
                    )
                }
            } else {
                add(EditableItemFicha())
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ficha de Treino", fontWeight = FontWeight.Bold) },
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
                    Text("Exercícios (${itensEdicao.size})", fontWeight = FontWeight.Bold)
                    IconButton(onClick = { itensEdicao.add(EditableItemFicha()) }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Exercício")
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(itensEdicao) { index, item ->
                        CardItemFichaEdicao(
                            index = index + 1,
                            item = item,
                            exerciciosDisponiveis = exerciciosDisponiveis,
                            onRemover = { if (itensEdicao.size > 1) itensEdicao.removeAt(index) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val requestItens = itensEdicao.mapIndexed { index, item ->
                        ItemFichaRequestDTO(
                            idExercicio = item.idExercicio,
                            ordem = index + 1,
                            seriesAlvo = item.series.toIntOrNull() ?: 4,
                            repeticoesAlvo = item.reps.toIntOrNull() ?: 10,
                            cargaAlvo = item.carga.toDoubleOrNull() ?: 0.0,
                            descanso = item.descanso.toIntOrNull() ?: 60
                        )
                    }
                    onConfirm(nome, descricao, requestItens)
                },
                enabled = nome.isNotBlank() && itensEdicao.any { it.idExercicio > 0L }
            ) {
                Text("Salvar Alterações")
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
fun CardItemFichaEdicao(
    index: Int,
    item: EditableItemFicha,
    exerciciosDisponiveis: List<ExercicioEntity>,
    onRemover: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val exercicioSelecionadoNome = exerciciosDisponiveis.find { it.id == item.idExercicio }?.nome ?: "Selecione o Exercício"

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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    OutlinedCard(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercicioSelecionadoNome,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Selecionar Exercício"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        exerciciosDisponiveis.forEach { ex ->
                            DropdownMenuItem(
                                text = { Text(ex.nome) },
                                onClick = {
                                    item.idExercicio = ex.id ?: 0L
                                    dropdownExpanded = false
                                }
                            )
                        }
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
        }
    }
}