package com.solid.number2048.presenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solid.number2048.game.NumbersGame
import com.solid.number2048.game.entities.UserInputEffects
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {


    private val _userInputEffects = Channel<UserInputEffects>()
    val userInputEffects = _userInputEffects.receiveAsFlow()

    val game = NumbersGame().apply {
        setOnGameEventsCallback { event ->
            viewModelScope.launch {
                _userInputEffects.send(event)
            }
        }
    }


    val curNumBox = game.curNumBox
    val fallingBoxes = game.fallingBoxes
    val mergingBoxes = game.mergingBoxes
    val mergeTargetBox = game.mergeTargetBox
    val gameScore = game.gameScore
    val isGamePlaying = game.isGamePlaying
    val gameSpeedState = game.gameSpeedState
    val board = game.board
    val queueState = game.queueState


    fun onNewFrame(frameMills: Long){
        game.onNewFrame(frameMills)
    }

    fun onUserBoardInput(posX: Int, isTap : Boolean){
        game.onUserBoardInput(posX, isTap)
    }

    fun onTogglePlayStop(isOnPauseEvent : Boolean = false){
        game.onTogglePlayStop(isOnPauseEvent)
    }

    fun save(){
        game.save()
    }



}