package com.ruan.flowgym.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ruan.flowgym.data.local.AppDatabase
import com.ruan.flowgym.data.remote.RetrofitClient
import com.ruan.flowgym.data.repository.ExercicioRepository
import com.ruan.flowgym.ui.screens.*
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModel
import com.ruan.flowgym.ui.viewmodel.TreinoAtivoViewModelFactory
import android.net.Uri

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Inicialização do Room Database e do Repositório
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ExercicioRepository(database.exercicioDao(), RetrofitClient.apiService) }

    // Instanciação do ViewModel utilizando a Factory personalizada
    val treinoViewModel: TreinoAtivoViewModel = viewModel(
        factory = TreinoAtivoViewModelFactory(repository)
    )

    val navController = rememberNavController()

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
                HomeScreen(
                    idUsuario = 1L,
                    onIniciarTreinoClick = {
                        navController.navigate(BottomNavItem.TreinoAtivo.route)
                    }
                )
            }

            // 2. Treino Ativo
            composable(BottomNavItem.TreinoAtivo.route) {
                TreinoAtivoScreen(
                    viewModel = treinoViewModel,
                    idUsuario = 1L
                )
            }

            // 3. Biblioteca de Exercícios
            composable(BottomNavItem.Historico.route) {
                ExerciciosScreen(
                    idUsuario = 1L,
                    onExercicioClick = { exercicio ->
                        val id = exercicio.id ?: return@ExerciciosScreen
                        val nome = exercicio.nome.ifEmpty { "Exercicio" }

                        // Codifica caracteres especiais como '/' e espaços
                        val nomeEncoded = Uri.encode(nome)

                        navController.navigate("historico_exercicio/$id/$nomeEncoded")
                    }
                )
            }

            // 4. Perfil (Placeholder provisório)
            composable(BottomNavItem.Perfil.route) {
                // Tela de Perfil
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

                // Decodifica a string de volta para a forma original
                val nomeExercicio = Uri.decode(rawNome)

                HistoricoExercicioScreen(
                    idExercicio = idExercicio,
                    nomeExercicio = nomeExercicio,
                    idUsuario = 1L,
                    onVoltarClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}