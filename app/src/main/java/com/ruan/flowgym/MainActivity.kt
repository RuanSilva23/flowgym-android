package com.ruan.flowgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ruan.flowgym.ui.navigation.MainScreen
import com.ruan.flowgym.ui.theme.FlowGymTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowGymTheme {
                MainScreen()
            }
        }
    }
}