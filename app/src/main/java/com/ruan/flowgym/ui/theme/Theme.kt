package com.ruan.flowgym.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FlowGymDarkColorScheme = darkColorScheme(
    primary = VoltGreen,
    onPrimary = OnVoltGreen,
    primaryContainer = VoltGreenContainer,
    onPrimaryContainer = OnVoltGreenContainer,
    secondary = ElectricCyan,
    onSecondary = OnElectricCyan,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = ErrorRed,
    onError = OnErrorRed
)

@Composable
fun FlowGymTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FlowGymDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pinta a barra de status com a cor de fundo do app
            window.statusBarColor = colorScheme.background.toArgb()
            // Garante ícones claros na barra de status
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}