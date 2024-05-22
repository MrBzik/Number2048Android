package com.solid.number2048.game.entities

import androidx.compose.ui.graphics.Color

sealed class UserInputEffects {
    data class ClickHighlight (val col : Int, val color: Color, val id: Long) : UserInputEffects()
    data object InvalidInput : UserInputEffects()

    data class ObtainedItems (val items : List<SpecialItem>) : UserInputEffects()
}
