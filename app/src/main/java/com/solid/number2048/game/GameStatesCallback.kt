package com.solid.number2048.game

import com.solid.number2048.game.entities.FallingBox
import com.solid.number2048.game.entities.GameSpeed
import com.solid.number2048.game.entities.MergingTargetBox
import com.solid.number2048.game.entities.QueueState
import com.solid.number2048.game.entities.StaticBox
import com.solid.number2048.game.entities.UserInputEffects

interface GameStatesCallback {

    fun onBoardUpdate(board: Array<Array<StaticBox?>>)
    fun onQueueStateUpdate(queueState: QueueState)
    fun onPlayableBoxUpdate(playableBox: FallingBox?)
    fun onMergingTargetBoxUpdate(mergingTargetBox: MergingTargetBox?)
    fun onGamePlayingPausedStateUpdate(gameState: Boolean)
    fun onGameScoreUpdate(score : Int)
    fun onGameSpeedUpdate(gameSpeed: GameSpeed)
    fun onLivesRemainUpdate(livesRemain: Int)
    fun onNewGameEvent(event: UserInputEffects)

}