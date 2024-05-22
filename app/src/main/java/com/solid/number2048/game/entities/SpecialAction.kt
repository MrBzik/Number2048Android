package com.solid.number2048.game.entities

enum class SpecialItemsType {
    QUEUE_EXPANDER, QUBE_DESTROYER, EXTRA_LIFE, SLOW_DOWN
}

data class SpecialItem(val type: SpecialItemsType, val col: Int, val row: Int)