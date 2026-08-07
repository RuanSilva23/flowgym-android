package com.ruan.flowgym.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ruan.flowgym.ui.screens.FichasScreen
import com.ruan.flowgym.ui.screens.HomeScreen
import com.ruan.flowgym.ui.screens.TreinoAtivoScreen
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "Home", { Icon(Icons.Default.Home, contentDescription = null) })
    object Fichas : Screen("fichas", "Fichas", { Icon(Icons.Default.FitnessCenter, contentDescription = null) })
    object TreinoAtivo : Screen("treino_ativo", "Treino", { Icon(Icons.Default.PlayArrow, contentDescription = null) })
}

@Composable
fun MainScreen(
    treinoAtivoViewModel: TreinoAtivoViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Screen.Home,
        Screen.Fichas,
        Screen.TreinoAtivo
    )

    Scaffold(
        bottomBar = {
            // 👈 Esconde a barra inferior durante o treino ativo
            if (currentRoute != Screen.TreinoAtivo.route) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    nomeUsuario = "Ruan",
                    idUsuario = 1L,
                    onIniciarTreinoClick = {
                        navController.navigate(Screen.Fichas.route)
                    }
                )
            }

            composable(Screen.Fichas.route) {
                FichasScreen(
                    idUsuario = 1L,
                    onIniciarTreinoClick = { idRotina ->
                        treinoAtivoViewModel.iniciarTreinoComRotina(idUsuario = 1L, idRotina = idRotina)
                        navController.navigate(Screen.TreinoAtivo.route)
                    }
                )
            }

            composable(Screen.TreinoAtivo.route) {
                TreinoAtivoScreen(
                    treinoViewModel = treinoAtivoViewModel,
                    onFinalizarTreinoClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}