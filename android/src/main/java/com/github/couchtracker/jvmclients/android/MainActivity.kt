package com.github.couchtracker.jvmclients.android

import com.github.couchtracker.jvmclients.common.App
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.couchtracker.jvmclients.common.Location
import com.github.couchtracker.jvmclients.common.navigation.StackData

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var stackData by remember { mutableStateOf(StackData.of<Location>(Location.Home)) }
            onBackPressedDispatcher.addCallback(this) {
                if (stackData.canPop()) {
                    stackData = stackData.pop()
                } else finish()
            }
            App(stackData) { stackData = it }
        }
    }
}