package com.kelvinsaputra.tvpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kelvinsaputra.tvpulse.ui.navigation.TvPulseApp
import com.kelvinsaputra.tvpulse.ui.theme.TVPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TVPulseTheme {
                TvPulseApp(onExit = ::finish)
            }
        }
    }
}
