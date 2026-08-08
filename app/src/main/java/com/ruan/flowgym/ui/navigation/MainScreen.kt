package com.ruan.flowgym.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ruan.flowgym.data.local.SessionManager
import com.ruan.flowgym.ui.screens.ExerciciosScreen
import com.ruan.flowgym.ui.screens.FichasScreen
import com.ruan.flowgym.ui.screens.HomeScreen
import com.ruan.flowgym.ui.viewmodel.ExerciciosViewModel
import com.ruan.flowgym.ui.viewmodel.FichaViewModel
import com.ruan.flowgym.ui.viewmodel.HomeViewModel

sealed class BottomBarItem(val title: String, val icon: ImageVector) {
    object Home : BottomBarItem("Início", Icons.Default.Home)
    object Fichas : BottomBarItem("Fichas", Icons.Default.DateRange)
    object TreinoAtivo : BottomBarItem("Treino", Icons.Default.PlayArrow)
    object Exercicios : BottomBarItem("Exercícios", Icons.Default.FitnessCenter)
    object Perfil : BottomBarItem("Perfil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    var selectedItem by remember { mutableStateOf<BottomBarItem>(BottomBarItem.Home) }
    val idUsuario = sessionManager.obterUserId()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomBarItem.Home,
                    BottomBarItem.Fichas,
                    BottomBarItem.TreinoAtivo,
                    BottomBarItem.Exercicios,
                    BottomBarItem.Perfil
                )
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selectedItem == item,
                        onClick = { selectedItem = item }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                is BottomBarItem.Home -> {
                    HomeScreen(
                        idUsuario = idUsuario,
                        viewModel = homeViewModel
                    )
                }
                is BottomBarItem.Fichas -> {
                    val fichaViewModel: FichaViewModel = hiltViewModel()
                    FichasScreen(
                        idUsuario = idUsuario,
                        fichaViewModel = fichaViewModel,
                        onIniciarTreinoClick = { idFicha ->
                            // Navegar para a aba de Treino Ativo selecionando a ficha
                            selectedItem = BottomBarItem.TreinoAtivo
                        }
                    )
                }
                is BottomBarItem.TreinoAtivo -> {
                    TelaPlaceholder(titulo = "Sessão de Treino em Andamento")
                }
                is BottomBarItem.Exercicios -> {
                    val exerciciosViewModel: ExerciciosViewModel = hiltViewModel()
                    ExerciciosScreen(
                        idUsuario = idUsuario,
                        viewModel = exerciciosViewModel,
                        onExercicioClick = { /* Detalhes do exercício */ }
                    )
                }
                is BottomBarItem.Perfil -> {
                    PerfilScreen(
                        sessionManager = sessionManager,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun PerfilScreen(
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    val nome = sessionManager.obterNome()
    val username = sessionManager.obterUsername()
    val idUsuario = sessionManager.obterUserId()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar com ícone de perfil
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Foto do Usuário",
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = nome,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (username.isNotBlank()) {
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Card de Informações da Conta
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DADOS DA CONTA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "ID do Usuário", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "#$idUsuario", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sincronização", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Conectado", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botão para Sair da Conta
        Button(
            onClick = {
                sessionManager.limparSessao()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Sair")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sair da Conta", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TelaPlaceholder(titulo: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
    }
}