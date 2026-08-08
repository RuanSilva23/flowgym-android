package com.ruan.flowgym.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ruan.flowgym.data.local.SessionManager

import com.ruan.flowgym.ui.screen.LoginScreen
import com.ruan.flowgym.ui.navigation.MainScreen
import com.ruan.flowgym.ui.screen.RegisterScreen
import com.ruan.flowgym.ui.screens.HomeScreen
import com.ruan.flowgym.ui.viewmodel.AuthViewModel
import com.ruan.flowgym.ui.viewmodel.HomeViewModel

sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Register : Screens("register")
    object Home : Screens("home")
}

@Composable
fun AppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()

    // Decida qual tela abrir ao iniciar o app: se estiver logado vai para Home, senão para Login
    val startDestination = if (sessionManager.estaLogado()) Screens.Home.route else Screens.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Tela de Login
        composable(Screens.Login.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Limpa a pilha e vai para a Home
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screens.Register.route)
                }
            )
        }

        // Tela de Cadastro
        composable(Screens.Register.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.popBackStack() // Volta para a tela de login
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Tela Inicial (Home)
        // Em AppNavigation.kt:

        composable(Screens.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()

            // Chama a MainScreen (que contém o Scaffold com a BottomBar)
            MainScreen(
                homeViewModel = homeViewModel,
                sessionManager = sessionManager,
                onLogout = {
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}