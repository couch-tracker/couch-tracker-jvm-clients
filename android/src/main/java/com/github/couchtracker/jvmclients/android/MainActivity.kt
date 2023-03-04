package com.github.couchtracker.jvmclients.android

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.DriverFactory
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData
import com.github.couchtracker.jvmclients.common.ui.screen.HomeLocation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val driver = DriverFactory(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            var stackData by remember { mutableStateOf(StackData.of<Location>(HomeLocation)) }
            onBackPressedDispatcher.addCallback(this) {
                if (stackData.canPop()) {
                    stackData = stackData.pop()
                } else {
                    finish()
                }
            }
            App(
                driver,
                stackData,
                editStack = {
                    when (it) {
                        null -> finish()
                        else -> stackData = it
                    }
                },
            )
        }
    }
}
