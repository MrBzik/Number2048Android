package com.solid.number2048.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
inline fun CalcRecomposes(
    label: String
){
    val count = remember { mutableStateOf(0) }
    SideEffect {
        count.value ++
        println("Recomposes at $label: ${count.value}")
    }
}