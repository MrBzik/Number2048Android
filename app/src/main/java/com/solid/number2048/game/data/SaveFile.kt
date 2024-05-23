package com.solid.number2048.game.data

import com.solid.number2048.game.entities.BoxTypes


data class SaveFile(
    val board: Array<Array<BoxTypes?>>,
) {

}
