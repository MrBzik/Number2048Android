package com.solid.number2048.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints

@Composable
fun WithMeasures(
    modifier: Modifier,
    content: @Composable (widthDp: Int, heightDp: Int) -> Unit
){
        val density = LocalDensity.current.density

        val height = remember { mutableStateOf(0) }
        val width = remember { mutableStateOf(0) }

        Layout(
            content = {
               content((width.value / density).toInt(), (height.value / density).toInt())
                      },
            modifier = modifier
        )
        { measurables: List<Measurable>, constraints: Constraints ->

            height.value = constraints.maxHeight
            width.value = constraints.maxWidth

            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEach { placeable ->
                    placeable.place(x = 0, y = 0)
                }
            }
        }
}
