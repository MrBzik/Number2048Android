package com.solid.number2048.ui.compose

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.solid.number2048.game.entities.GameSpeed


@Composable
fun Thermometer(
    gameSpeed: State<GameSpeed>
){

    val density = LocalDensity.current.density

//    val gameSpeed = remember {
//        mutableStateOf(GameSpeed(97, Color.Blue))
//    }

    Box(modifier = Modifier
        .size(100.dp),
        contentAlignment = Alignment.BottomCenter){

        DrawThermometerValue(gameSpeed)

        Box(modifier = Modifier
            .size(100.dp)
            .drawBehind {
                drawThermometer((100 * density))
            }
        )
    }
}

@Composable
fun DrawThermometerValue(
    gameSpeed: State<GameSpeed>
){

//    val prevColor = remember {
//        mutableStateOf(gameSpeed.value.color)
//    }
//
//    val prevSpeed = remember {
//        mutableIntStateOf(gameSpeed.value.speed)
//    }

    val height = animateIntAsState(targetValue = (gameSpeed.value.speed * 0.8f).toInt(),
        animationSpec = tween(3000)
    )


    Box(modifier = Modifier
        .width(70.dp)
        .height(height.value.dp + 10.dp)
        .padding(bottom = 9.dp)
        .background(gameSpeed.value.color))
}



@Composable
@Preview
fun drawSpeedometer(){
    Box(modifier = Modifier.size(100.dp).background(Color.DarkGray).drawBehind {
        drawSpeedometer()
    })
}
fun DrawScope.drawSpeedometer(){

    val step = 22.5f
    var curPos = -90f

    drawArc(
        Color.Red.copy(alpha = 0.5f),
        startAngle = curPos,
        sweepAngle = step,
        useCenter = true
    )

    curPos += step

    drawArc(
        Color.Yellow.copy(alpha = 0.5f),
        startAngle = curPos,
        sweepAngle = step,
        useCenter = true
    )
    curPos += step

    drawArc(
        Color.Green.copy(alpha = 0.5f),
        startAngle = curPos,
        sweepAngle = step,
        useCenter = true
    )
    curPos += step

    drawArc(
        Color.Blue.copy(alpha = 0.5f),
        startAngle = curPos,
        sweepAngle = step,
        useCenter = true
    )



    drawArc(
        Color.LightGray,
        startAngle = -90f,
        sweepAngle = 90f,
        useCenter = true,
        size = Size(width = 200f, height = 200f),
        topLeft = Offset(x = 37f, y = 38f)

    )

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


    val per_40 = sizeDp * 0.4f
    val per_20 = sizeDp * 0.2f
    val per_50 = sizeDp * 0.5f
    val per_60 = sizeDp * 0.6f
    val per_80 = sizeDp * 0.8f
    val per_90 = sizeDp * 0.9f

    val thermometerPath = Path().apply {


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
            per_50, sizeDp * 0.95f,
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


    thermometerPath.apply {

        moveTo(per_40, per_20)
        lineTo(per_50, per_20)

        moveTo(per_40, per_40)
        lineTo(per_50, per_40)

        moveTo(per_40, per_60)
        lineTo(per_50, per_60)

    }



    drawPath(path = rectPath, color = Color.DarkGray)

    drawPath(path = thermometerPath, color = Color.LightGray,
        style = Stroke(
            width = 3f * density
        )
    )
}

