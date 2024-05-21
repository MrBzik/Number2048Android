package com.solid.number2048.game.entities


data class MergingTargetBox(
    val startBox: BoxTypes,
    val x: Float,
    val y: Float,
    val targetBox: BoxTypes,
//    val item : SpecialItems? = null
)