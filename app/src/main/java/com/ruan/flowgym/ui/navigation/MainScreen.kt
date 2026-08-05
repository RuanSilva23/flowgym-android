package com.ruan.flowgym.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ruan.flowgym.ui.screens.*
import com.ruan.flowgym.ui.viewmodel.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // 👈 HOISTING: ViewModel compartilhado no nível da MainScreen
    val treinoViewModel: TreinoAtivoViewModel = hiltViewModel()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.TreinoAtivo,
        BottomNavItem.Historico,
        BottomNavItem.Perfil
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Home
            composable(BottomNavItem.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    nomeUsuario = "Ruan",
                    idUsuario = 1L,
                    homeViewModel = homeViewModel,
                    onIniciarTreinoClick = {
                        navController.navigate(BottomNavItem.TreinoAtivo.route)
                    }
                )
            }

            // 2. Treino Ativo
            composable(BottomNavItem.TreinoAtivo.route) {
                TreinoAtivoScreen(
                    viewModel = treinoViewModel, // 👈 Usa a instância compartilhada
                    idUsuario = 1L
                )
            }

            // 3. Biblioteca de Exercícios
            composable(BottomNavItem.Historico.route) {
                val exerciciosViewModel: ExerciciosViewModel = hiltViewModel()
                ExerciciosScreen(
                    idUsuario = 1L,
                    viewModel = exerciciosViewModel,
                    onExercicioClick = { exercicio ->
                        val id = exercicio.id ?: return@ExerciciosScreen
                        val nome = exercicio.nome.ifEmpty { "Exercicio" }
                        val nomeEncoded = Uri.encode(nome)

                        navController.navigate("historico_exercicio/$id/$nomeEncoded")
                    }
                )
            }

            // 4. Minhas Fichas (Aba Perfil/Fichas)
            composable(BottomNavItem.Perfil.route) {
                val fichaViewModel: FichaViewModel = hiltViewModel()
                FichasScreen(
                    viewModel = fichaViewModel,
                    onRotinaClick = { idRotina ->
                        // 1. Abre a sessão de treino no Spring Boot e carrega os exercícios no ViewModel
                        treinoViewModel.iniciarTreinoComRotina(
                            idUsuario = 1L,
                            idRotina = idRotina
                        )

                        // 2. Alterna para a aba do Treino Ativo
                        navController.navigate(BottomNavItem.TreinoAtivo.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 5. Rota Interna: Evolução de Carga do Exercício
            composable(
                route = "historico_exercicio/{idExercicio}/{nomeExercicio}",
                arguments = listOf(
                    navArgument("idExercicio") { type = NavType.LongType },
                    navArgument("nomeExercicio") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val idExercicio = backStackEntry.arguments?.getLong("idExercicio") ?: 0L
                val rawNome = backStackEntry.arguments?.getString("nomeExercicio") ?: "Exercício"
                val nomeExercicio = Uri.decode(rawNome)

                val historicoViewModel: HistoricoExercicioViewModel = hiltViewModel()

                HistoricoExercicioScreen(
                    idExercicio = idExercicio,
                    nomeExercicio = nomeExercicio,
                    idUsuario = 1L,
                    viewModel = historicoViewModel,
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}