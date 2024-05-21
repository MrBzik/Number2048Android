package com.solid.number2048.game.entities

import androidx.compose.ui.graphics.Color


data class FallingBox(
    val numBox: BoxTypes,
    val x: Float,
    val y: Float,
    val targetY: Int,
    val scale: Float = 1f,
    val item: SpecialItems? = null
) {

    fun toStaticBox() : StaticBox {
        return StaticBox(
            item = item,
            boxTypes = numBox
        )
    }

}

data class MergingBox(
    val numBox: BoxTypes,
    val x: Float,
    val y: Float,
    val vector: Vector,
    val targetX: Float,
    val targetY: Float,
    val color: Color? = null,
//    val item: SpecialItems? = null
){
}

enum class Vector {
    UP, LEFT, RIGHT
}


