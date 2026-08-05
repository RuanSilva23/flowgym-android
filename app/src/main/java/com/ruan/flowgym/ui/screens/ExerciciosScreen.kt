package com.ruan.flowgym.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ruan.flowgym.data.model.ExercicioResponseDTO
import com.ruan.flowgym.ui.viewmodel.ExerciciosUiState
import com.ruan.flowgym.ui.viewmodel.ExerciciosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciciosScreen(
    idUsuario: Long = 1L,
    viewModel: ExerciciosViewModel = viewModel(),
    onExercicioClick: (ExercicioResponseDTO) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val grupoSelecionado by viewModel.grupoSelecionado.collectAsState()
    val buscaQuery by viewModel.buscaQuery.collectAsState()

    val gruposMusculares = listOf(
        "TODOS", "PEITO", "COSTAS", "PERNAS",
        "OMBROS", "BICEPS", "TRICEPS", "ABDOMEN"
    )

    LaunchedEffect(Unit) {
        viewModel.carregarExercicios(idUsuario)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Biblioteca de Exercícios",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // 1. Campo de Pesquisa
            OutlinedTextField(
                value = buscaQuery,
                onValueChange = { viewModel.onBuscaQueryChange(it) },
                placeholder = { Text("Buscar exercício...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Carrossel de Categorias / FilterChips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gruposMusculares.forEach { grupo ->
                    val isSelected = grupo == grupoSelecionado
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selecionarGrupo(grupo, idUsuario) },
                        label = {
                            Text(
                                text = grupo,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Conteúdo Central (Lista, Loading ou Erro)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is ExerciciosUiState.Loading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    is ExerciciosUiState.Erro -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.mensagem,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.carregarExercicios(idUsuario) }) {
                                Text("Tentar Novamente")
                            }
                        }
                    }

                    is ExerciciosUiState.Sucesso -> {
                        val lista = state.exerciciosFiltrados

                        if (lista.isEmpty()) {
                            Text(
                                text = "Nenhum exercício encontrado",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(lista) { exercicio ->
                                    CardItemExercicio(
                                        exercicio = exercicio,
                                        onClick = { onExercicioClick(exercicio) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardItemExercicio(
    exercicio: ExercicioResponseDTO,
    onClick: () -> Unit
) {
    val nomeExercicio = exercicio.nome ?: "Exercício sem nome"
    val grupo = exercicio.grupoMuscular?.uppercase() ?: "GERAL"

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = nomeExercicio,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = grupo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver Detalhes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}