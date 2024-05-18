package com.solid.number2048.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
@Preview
fun Thermometer(){

    val density = LocalDensity.current.density


    Box(modifier = Modifier
        .size(100.dp),
        contentAlignment = Alignment.BottomCenter){

        Box(modifier = Modifier
            .width(100.dp)
            .height(50.dp)
            .padding(bottom = 1.dp, end = 1.dp)
            .background(Color.Green))


        Box(modifier = Modifier
            .size(100.dp)
            .drawBehind {
                drawThermometer((100 * density))
            }
        )
    }
}


fun DrawScope.drawThermometer(
    sizeDp : Float
){
    val rectPath = Path().apply {
        lineTo(sizeDp, 0f)
        lineTo(sizeDp, sizeDp)
        lineTo(0f, sizeDp)
        lineTo(0f, 0f)
        close()
    }


    val thermometerPath = Path().apply {


        val per_40 = sizeDp * 0.4f
        val per_20 = sizeDp * 0.2f
        val per_60 = sizeDp * 0.6f
        val per_80 = sizeDp * 0.8f
        val per_90 = sizeDp * 0.9f

        arcTo(
            Rect(
                offset = Offset(per_40, sizeDp * 0.1f),
                size = Size(per_20, per_20)
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )


        lineTo(per_60, per_60)

        quadraticBezierTo(
            per_80, per_80,
            per_60, per_90
        )

        quadraticBezierTo(
            sizeDp * 0.5f, sizeDp * 0.95f,
            per_40, per_90
        )

        quadraticBezierTo(
            per_20, per_80,
            per_40, per_60
        )

        lineTo(per_40, per_60)

        lineTo(per_40, per_20)
        close()
    }


    rectPath.apply {
        fillType = PathFillType.EvenOdd
        addPath(thermometerPath)
    }



    drawPath(path = rectPath, color = Color.DarkGray)

    drawPath(path = thermometerPath, color = Color.LightGray,
        style = Stroke(
            width = 3f * density
        )
    )
}

