package com.solid.number2048.ui.compose

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
inline fun CalcRecomposes(
    label: String,
    tag : String
){
    val count = remember { mutableIntStateOf(0) }
    SideEffect {
        count.intValue ++
        Log.d(tag, "Recomposes at $label: ${count.intValue}")
    }
}