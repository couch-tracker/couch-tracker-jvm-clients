package com.github.couchtracker.jvmclients.android

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.couchtracker.jvmclients.common.App
import com.github.couchtracker.jvmclients.common.DriverFactory
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val driver = DriverFactory(this)

        setContent {
            var stackData by remember { mutableStateOf(StackData.of<Location>(Location.Home)) }
            onBackPressedDispatcher.addCallback(this) {
                if (stackData.canPop()) {
                    stackData = stackData.pop()
                } else finish()
            }
            App(
                driver, stackData,
                close = { finish() },
                editStack = { stackData = it }
            )
        }
    }
}