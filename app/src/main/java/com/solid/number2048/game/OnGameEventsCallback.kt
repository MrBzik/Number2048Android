package com.solid.number2048.game

import com.solid.number2048.game.entities.UserInputEffects

fun interface OnGameEventsCallback {
    fun onGameEvent(event: UserInputEffects)

}