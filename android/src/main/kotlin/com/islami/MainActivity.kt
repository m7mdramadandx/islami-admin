package com.islami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.islami.App
import com.islami.core.di.initializeKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start Koin if not already started
        try {
            startKoin {
                androidContext(this@MainActivity)
                modules(initializeKoinModules())
            }
        } catch (e: Exception) {
            // Koin already started
        }

        setContent {
            App()
        }
    }
}
