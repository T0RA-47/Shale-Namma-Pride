package com.shalenammapride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.shalenammapride.ui.navigation.ShaleNavGraph
import com.shalenammapride.ui.theme.ShaleNammaPrideTheme
import com.shalenammapride.util.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        languageManager = LanguageManager(applicationContext)

        setContent {
            var isKannada by remember {
                mutableStateOf(runBlocking { languageManager.isKannada.first() })
            }

            LaunchedEffect(Unit) {
                languageManager.isKannada.collect { isKannada = it }
            }

            ShaleNammaPrideTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShaleNavGraph(
                        isKannada = isKannada,
                        onLanguageToggle = {
                            lifecycleScope.launch {
                                languageManager.setKannada(!isKannada)
                            }
                        }
                    )
                }
            }
        }
    }
}
