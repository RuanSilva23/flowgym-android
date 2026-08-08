package com.ruan.flowgym

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ruan.flowgym.data.local.SessionManager
import com.ruan.flowgym.ui.navigation.AppNavigation
import com.ruan.flowgym.ui.theme.FlowGymTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowGymTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(sessionManager = sessionManager)
                }
            }
        }
    }
}