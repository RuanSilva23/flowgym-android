package com.ruan.apexlift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.ruan.apexlift.data.local.SessionManager
import com.ruan.apexlift.ui.navigation.AppNavigation
import com.ruan.apexlift.ui.theme.ApexLiftTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApexLiftTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(sessionManager = sessionManager)
                }
            }
        }
    }
}