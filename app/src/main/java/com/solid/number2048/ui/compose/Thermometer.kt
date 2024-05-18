package com.solid.number2048.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun Thermometer(){

    Box(modifier = Modifier
        .drawBehind {

            val path = Path().apply {

                lineTo(100f, 0f)
                lineTo(100f, 100f)
                lineTo(0f, 100f)
                lineTo(0f, 0f)
                close()
            }

            drawPath(path = path, color = Color.Red)


        }
    )
}