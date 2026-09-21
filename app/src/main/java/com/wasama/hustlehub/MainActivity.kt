package com.wasama.hustlehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wasama.hustlehub.ui.navigation.AppScaffold
import com.wasama.hustlehub.ui.theme.HustleHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HustleHubTheme {
                AppScaffold()
            }
        }
    }
}
