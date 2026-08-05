package com.ruan.flowgym.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Início", Icons.Default.Home)
    object TreinoAtivo : BottomNavItem("treino_ativo", "Treino", Icons.Default.FitnessCenter)
    object Historico : BottomNavItem("exercicios", "Exercícios", Icons.Default.List)
    object Perfil : BottomNavItem("perfil", "Perfil", Icons.Default.Person)
}