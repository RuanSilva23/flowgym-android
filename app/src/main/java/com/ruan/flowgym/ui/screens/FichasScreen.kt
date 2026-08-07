package com.ruan.flowgym.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruan.flowgym.data.local.entity.ExercicioEntity
import com.ruan.flowgym.data.model.ItemFichaRequestDTO
import com.ruan.flowgym.data.model.RotinaResponseDTO
import com.ruan.flowgym.ui.viewmodel.FichaUiState
import com.ruan.flowgym.ui.viewmodel.FichaViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichasScreen(
    idUsuario: Long = 1L,
    fichaViewModel: FichaViewModel = hiltViewModel(),
    onIniciarTreinoClick: (Long) -> Unit = {}
) {
    val uiState by fichaViewModel.uiState.collectAsState()
    var exibindoModalCriacao by remember { mutableStateOf(false) }
    var fichaParaEditar by remember { mutableStateOf<RotinaResponseDTO?>(null) }
    var fichaParaDeletar by remember { mutableStateOf<Long?>(null) }

    val exerciciosDisponiveis by fichaViewModel.exerciciosDisponiveis.collectAsState()

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
        fichaViewModel.carregarFichas(idUsuario)
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
                                    ficha = rotina,
                                    onIniciarTreino = { rotina.id?.let { idRotina -> onIniciarTreinoClick(idRotina) } },
                                    onEditarFicha = { fichaParaEditar = rotina },
                                    onDeletarFicha = { rotina.id?.let { idRotina -> fichaParaDeletar = idRotina } }
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
                fichaViewModel.criarFicha(
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
                    fichaViewModel.editarFicha(
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
                        fichaViewModel.deletarFicha(idFicha, idUsuario)
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
    ficha: RotinaResponseDTO,
    onIniciarTreino: (Long) -> Unit,
    onEditarFicha: (RotinaResponseDTO) -> Unit,
    onDeletarFicha: (Long) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        onClick = { expandido = !expandido }, // 👈 Usar o onClick nativo do Card do Material 3
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho: Título, Descrição e Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ficha.nome ?: "Ficha sem nome",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!ficha.descricao.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = ficha.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${ficha.exercicios.orEmpty().size} exercícios cadastrados",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEditarFicha(ficha) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Ficha",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onDeletarFicha(ficha.id ?: 0L) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Deletar Ficha",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Recolher" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Lista de Exercícios + Botão de Início
            AnimatedVisibility(visible = expandido) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "EXERCÍCIOS DESSA FICHA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val listaExercicios = ficha.exercicios.orEmpty()
                    if (listaExercicios.isEmpty()) {
                        Text(
                            text = "Nenhum exercício cadastrado nesta ficha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        listaExercicios.sortedBy { it.ordem }.forEachIndexed { index, itemEx ->
                            val nomeEx = itemEx.nomeExercicio?.takeIf { it.isNotBlank() } ?: "Exercício ${index + 1}"
                            val series = itemEx.seriesAlvo ?: 0
                            val reps = itemEx.repeticoesAlvo ?: 0
                            val carga = itemEx.cargaAlvo ?: 0.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. $nomeEx",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${series}x ${reps} reps | ${carga} kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão para iniciar treino
                    Button(
                        onClick = { onIniciarTreino(ficha.id ?: 0L) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INICIAR ESTA FICHA",
                            fontWeight = FontWeight.Bold
                        )
                    }
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