package com.solid.number2048.game

import androidx.compose.ui.graphics.Color
import com.solid.number2048.game.entities.BoxIdx
import com.solid.number2048.game.entities.BoxTypes
import com.solid.number2048.game.entities.FallingBox
import com.solid.number2048.game.entities.GameSpeed
import com.solid.number2048.game.entities.GameState
import com.solid.number2048.game.entities.MergingBox
import com.solid.number2048.game.entities.MergingTargetBox
import com.solid.number2048.game.entities.QueueState
import com.solid.number2048.game.entities.SpecialItem
import com.solid.number2048.game.entities.SpecialItemsType
import com.solid.number2048.game.entities.StaticBox
import com.solid.number2048.game.entities.UserInputEffects
import com.solid.number2048.game.entities.Vector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Random
import java.util.Stack

class NumbersGameWithCalbacks {


    private var gameState = GameState.PAUSED
    private var lastGameState = GameState.PLAYING
    private var gameSpeed = MIN_GAME_SPEED
    private var minNumber = 2
    private var maxNumber = 32
    private var lastFrame : Long? = null

    private var lastColumn = BOARD_WIDTH / 2

    private val gameBoard : Array<Array<StaticBox?>> = Array(BOARD_HEIGHT){ Array(BOARD_WIDTH) { null } }
    private val _board : MutableStateFlow<Array<Array<StaticBox?>>> = MutableStateFlow(
        gameBoard
    )
    val board = _board.asStateFlow()

    private var fallingBoxes : List<FallingBox> = emptyList()

    private var mergingBoxes : List<MergingBox> = emptyList()

    private val checkMatchesStack = Stack<BoxIdx>()

    private var queueSize = 1
    private var queueToExpand = 0
    private val boxesQueue = ArrayDeque<BoxTypes>()
    private val _queueState : MutableStateFlow<QueueState> = MutableStateFlow(QueueState(emptyList(), queueSize))
    val queueState = _queueState.asStateFlow()

    private var curNumBox : FallingBox? = null

    private var mergeTargetBox : MergingTargetBox? = null

//    private val _userInputEffects = Channel<UserInputEffects>()
//    val userInputEffects = _userInputEffects.receiveAsFlow()

    private val _isGamePlaying = MutableStateFlow(false)
    val isGamePlaying = _isGamePlaying.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore = _gameScore.asStateFlow()

    private val _gameSpeed = MutableStateFlow(GameSpeed(speed = convertSpeedToPercentageValue(), color = Color.Blue))
    val gameSpeedState = _gameSpeed.asStateFlow()

    private val _lives = MutableStateFlow(0)
    val lives = _lives.asStateFlow()

    private val _destroyers = MutableStateFlow(0)
    val destroyers = _destroyers.asStateFlow()

    private val _freezers = MutableStateFlow(0)
    val freezers = _freezers.asStateFlow()


    private var gameStateCallbacks : GameStatesCallback? = null

    fun save(){




    }



    fun onNewFrame(frameMills: Long){

        lastFrame?.let {  last ->

            val delta = (frameMills - last).toFloat() / 1000
            when(gameState){

                GameState.PLAYING -> {
                    onPlayingFrame(delta)
                }
                GameState.FALLING -> {
                    onFallingBoxesFrame(delta)
                }
                GameState.MERGING -> {
                    onMergeBoxesFrame(delta)
                }
                GameState.PAUSED -> {

                }
                GameState.CHECKING -> {
                    checkMatches()
                }

                GameState.GAME_OVER -> {
                    startNewGame()
                }
            }
        }

        lastFrame = frameMills
    }


    private fun startNewGame(){

        gameSpeed = MIN_GAME_SPEED
        minNumber = 2
        maxNumber = 32
        lastFrame = null
        lastColumn = BOARD_WIDTH / 2

        gameBoard.forEachIndexed { row, boxTypes ->
            boxTypes.forEachIndexed { col, _ ->
                gameBoard[row][col] = null
            }
        }

        _board.update {
            gameBoard
        }

        fallingBoxes = emptyList()

        boxesQueue.clear()

//        _boxesQueueState.update { emptyList() }

        queueSize = 1


        gameStateCallbacks?.let { it.onQueueStateUpdate(QueueState(emptyList(), queueSize)) }

        _queueState.update { QueueState(emptyList(), queueSize) }

        _isGamePlaying.update { true }

        _gameScore.update { 0 }

        _gameSpeed.update { GameSpeed(speed = convertSpeedToPercentageValue(), color = Color.Blue) }

        gameState = GameState.PLAYING
        lastGameState = GameState.PLAYING
    }



    private fun onPlayingFrame(delta: Float){
        if(boxesQueue.size < queueSize){
            fillBoxesQueue()
        }


        curNumBox?.let { box ->

            if(box.scale < 1f)
                curNumBox = box.copy(scale = (box.scale + delta + ANIM_SPEED)
                    .coerceAtMost(1f)
                )
            else {
                val yPos = box.y + delta * gameSpeed
                val isDropped = isBoxDroppedOnBoard(box, yPos)
                if(isDropped){
                    gameState = GameState.CHECKING
                    lastColumn = box.x.toInt()
                    curNumBox = null
                }
                else {
                    curNumBox = box.copy(y = yPos)
                }
            }
        } ?: run {
            curNumBox = getNextBox()
        }

        gameStateCallbacks?.onPlayableBoxUpdate(curNumBox)
    }

    private fun onFallingBoxesFrame(delta: Float){
        val update = mutableListOf<FallingBox>()

        fallingBoxes.forEach {
            val yPos = it.y + delta * FALL_SPEED
            val isDropped = isBoxDroppedOnBoard(it, yPos)
            if(!isDropped) update.add(it.copy(y = yPos))
        }
        if(update.isEmpty()) gameState = GameState.CHECKING

        fallingBoxes = update

        gameStateCallbacks?.onFallingBoxesUpdate(fallingBoxes)

    }



    private fun isBoxDroppedOnBoard(box : FallingBox, yPos : Float) : Boolean {
        val xIdx = box.x.toInt()
        val targetY = box.targetY
        if(yPos >= targetY){
            gameBoard[targetY][xIdx] = box.toStaticBox()
            _board.value = getBoardCopy()
            checkMatchesStack.push(BoxIdx(row = targetY, col = xIdx))
            return true
        }
        return false
    }

    private fun getBoardCopy() : Array<Array<StaticBox?>>{

        val clone = Array<Array<StaticBox?>>(BOARD_HEIGHT){
            Array(BOARD_WIDTH){
                null
            }
        }

        gameBoard.forEachIndexed { y, _ ->
            clone[y] = gameBoard[y].clone()
        }

        return clone
    }


    private fun onMergeBoxesFrame(delta: Float) {

        var isMerged = false

        val update = mutableListOf<MergingBox>()

        mergingBoxes.forEach { box ->
            var x = box.x
            var y = box.y
            when(box.vector){
                Vector.UP -> {
                    y = (y - (delta * ANIM_SPEED)).coerceAtLeast(box.targetY)
                }
                Vector.LEFT -> {
                    x = (x - (delta * ANIM_SPEED)).coerceAtLeast(box.targetX)
                }
                Vector.RIGHT -> {
                    x = (x + (delta * ANIM_SPEED)).coerceAtMost(box.targetX)
                }
            }
            isMerged = x == box.targetX && y == box.targetY
            update.add(box.copy(x = x, y = y))
        }

        if(isMerged){
            passMergedBoxForward()
            gameState = GameState.FALLING
            mergingBoxes = emptyList()
        }
        else {
            mergingBoxes = update
        }

        gameStateCallbacks?.onMergingBoxesUpdate(mergingBoxes)

    }


    private fun passMergedBoxForward(){


        mergeTargetBox?.let { target ->

            fallingBoxes = fallingBoxes + FallingBox(
                numBox = target.targetBox,
                x = target.x,
                y = target.y,
                targetY = getDepth(target.x.toInt())
            )

            gameStateCallbacks?.onFallingBoxesUpdate(fallingBoxes)

            val number = target.targetBox.number

            _gameScore.update {  prevScore ->
                prevScore + number
            }

            mergeTargetBox = null
            gameStateCallbacks?.onMergingTargetBoxUpdate(null)
        }

    }

    private fun getDepth(col : Int) : Int {

        var depth = BOARD_HEIGHT - 1

        for(row in BOARD_HEIGHT - 1 downTo 0){

            if(gameBoard[row][col] != null){
                depth --
            } else {
                break
            }
        }
        return depth

    }


    private fun checkMatches(){

        if(checkMatchesStack.isEmpty()){
            gameBoard[1].forEach {
                it?.let {
                    gameState = GameState.GAME_OVER
                    return
                }
            }

            gameState = GameState.PLAYING
            return
        }

        while (checkMatchesStack.isNotEmpty()){

            val check = checkMatchesStack.pop()

            val isMatchFound = onNumberDropped(check.row, check.col)

            if(isMatchFound) {
                gameState = GameState.MERGING
                break
            }
        }
    }



    private fun onNumberDropped(y: Int, x: Int) : Boolean {

        val numBox = gameBoard[y][x] ?: return false
        val boxesToMerge = mutableListOf<MergingBox>()
        val fallingBoxes = mutableListOf<FallingBox>()
        val obtainedItems = mutableListOf<SpecialItem>()
        var isMatchesFound = false

        fun isMatch(yIdx: Int, xIdx: Int, vector: Vector)  {
            gameBoard[yIdx][xIdx]?.let { match ->
                if(match.boxTypes.number == numBox.boxTypes.number){
                    isMatchesFound = true
                    boxesToMerge.add(
                        MergingBox(
                            numBox = match.boxTypes,
                            x = xIdx.toFloat(),
                            y = yIdx.toFloat(),
                            vector = vector,
                            targetX = x.toFloat(),
                            targetY = y.toFloat()
                        )
                    )

                    val item = gameBoard[yIdx][xIdx]?.item

                    item?.let {
                        obtainedItems.add(
                            SpecialItem(
                                type = it,
                                row = yIdx,
                                col = xIdx
                            )
                        )
                    }

                    gameBoard[yIdx][xIdx] = null


                    val startIdx = if(vector == Vector.UP) yIdx - 2 else yIdx - 1

                    for(i in startIdx downTo 0){

                        val box = gameBoard[i][xIdx] ?: break

                        gameBoard[i][xIdx] = null

                        fallingBoxes.add(
                            FallingBox(
                                numBox = box.boxTypes,
                                x = xIdx.toFloat(),
                                y = i.toFloat(),
                                targetY = i + 1,
                                item = box.item
                            )
                        )
                    }
                }
            }
        }


        if(x > 0){
            isMatch(y, x - 1, Vector.RIGHT)
        }
        if(x < BOARD_WIDTH - 1){
            isMatch(y, x + 1, Vector.LEFT)
        }
        if(y < BOARD_HEIGHT - 1){
            isMatch(y + 1, x, Vector.UP)
        }

        if(isMatchesFound){

            val item = gameBoard[y][x]?.item

            item?.let {
                obtainedItems.add(
                    SpecialItem(
                        type = it,
                        row = y,
                        col = x
                    )
                )
            }

            gameBoard[y][x] = null

            sendMergingTargetBox(boxesToMerge)

            handleObtainedItems(obtainedItems)

            this.fallingBoxes = fallingBoxes

            gameStateCallbacks?.apply {
                onFallingBoxesUpdate(fallingBoxes)

            }

            mergingBoxes = boxesToMerge.also {
                gameStateCallbacks?.onMergingBoxesUpdate(it)
            }

            _board.value = getBoardCopy()

        }


        return isMatchesFound

    }


    private fun handleObtainedItems(items : List<SpecialItem>){

        if(items.isEmpty()) return


        items.forEach {

            when(it.type){
                SpecialItemsType.QUEUE_EXPANDER -> queueToExpand ++
                SpecialItemsType.QUBE_DESTROYER -> _destroyers.update { destroyers -> destroyers + 1 }
                SpecialItemsType.EXTRA_LIFE -> _lives.update { lives -> lives + 1 }
                SpecialItemsType.SLOW_DOWN -> _freezers.update { freezers -> freezers + 1 }
            }
        }

        gameStateCallbacks?.onNewGameEvent(UserInputEffects.ObtainedItems(items))
    }


    private fun sendMergingTargetBox(matches: List<MergingBox>){

        val multiplier = matches.size
        var num = matches.first().numBox.number
        repeat(multiplier){
            num *= 2
        }
        val newNumBox = BoxTypes.entries.find {
            it.number == num
        } ?: return

        val targetBox = MergingTargetBox(
            startBox =  matches.first().numBox,
            x = matches.first().targetX,
            y = matches.first().targetY,
            targetBox = newNumBox
        )

        gameSpeed += (multiplier * 0.01f).coerceAtMost(3.5f)

        _gameSpeed.update {
            val speed = convertSpeedToPercentageValue()
            val color =
                if(speed < 25) Color.Blue
                else if (speed < 50) Color.Green
                else if (speed < 75) Color.Yellow
                else Color.Red
            GameSpeed(speed, color)
        }

        mergeTargetBox = targetBox.also {
            gameStateCallbacks?.onMergingTargetBoxUpdate(it)
        }

    }




    private fun convertSpeedToPercentageValue() : Int {
        val speed = (100 * gameSpeed) / MAX_GAME_SPEED
        return speed.toInt()
    }



    fun onUserBoardInput(posX: Int, isTap : Boolean){

        if(gameState != GameState.PLAYING) return

        curNumBox?.let {  box ->

            if(!isValidInput(posX, box)){

                gameStateCallbacks?.onNewGameEvent(UserInputEffects.InvalidInput)

                return
            }

            val updatedBox = box.copy(x = posX.toFloat(), targetY = getDepth(posX))

            if(isTap){

                gameStateCallbacks?.onNewGameEvent(
                    UserInputEffects.ClickHighlight(
                        col = posX,
                        color = box.numBox.color,
                        id = System.currentTimeMillis()
                    )
                )

                gameState = GameState.FALLING
                fallingBoxes = listOf(updatedBox).also {
                    gameStateCallbacks?.onFallingBoxesUpdate(it)
                }

                lastColumn = posX
                curNumBox = null
            } else {
                curNumBox = updatedBox
            }

            gameStateCallbacks?.onPlayableBoxUpdate(curNumBox)

        }
    }

    private fun isValidInput(posX: Int, curBox: FallingBox) : Boolean {

        if(posX < 0 || posX > BOARD_WIDTH - 1) return false

        val oldX = curBox.x.toInt()

        if(posX != oldX){
            val yIdx = curBox.y.toInt() + 1
            val range = if (posX > oldX) oldX + 1 ..posX
            else oldX - 1 downTo posX
            for(i in range) if(gameBoard[yIdx][i] != null) return false
        }

        return true
    }


    fun onTogglePlayStop(isOnPauseEvent : Boolean = false){

        if(gameState == GameState.PAUSED && !isOnPauseEvent){
            gameState = lastGameState
            _isGamePlaying.value = true
        }
        else {
            if(!isOnPauseEvent)
                lastGameState = gameState
            gameState = GameState.PAUSED
            _isGamePlaying.value = false
        }
    }


    private fun fillBoxesQueue(){

        while (boxesQueue.size < queueSize){
            val numBox = BoxTypes.entries.filter {
                it.number in minNumber..maxNumber
            }.random()
            boxesQueue.addLast(numBox)
        }

        _queueState.update {
            it.copy(queue = boxesQueue.toList(), freeSpots = 0)
        }
    }


    private fun getNextBox() : FallingBox {

        val numBox = boxesQueue.removeFirst()

        if(queueToExpand > 0){
            queueSize += queueToExpand
            queueToExpand = 0
        }

        _queueState.update {
            it.copy(queue = boxesQueue.toList(), freeSpots = queueSize - boxesQueue.size)
        }


        val item = getRandomItem()

        val x = lastColumn
        val targetY = getDepth(x)

        return FallingBox(
            numBox = numBox,
            x = x.toFloat(),
            y = 0f,
            targetY = targetY,
            scale = 0f,
            item = item
        )

    }


    private fun getRandomItem() : SpecialItemsType? {
        if(Random().nextInt(100) > 70){

            val items = SpecialItemsType.entries.filter {
                when(it){
                    SpecialItemsType.QUEUE_EXPANDER -> queueSize < BOXES_QUEUE_MAX
                    SpecialItemsType.QUBE_DESTROYER -> _destroyers.value < ITEM_DESTROYERS_MAX
                    SpecialItemsType.EXTRA_LIFE -> _lives.value < ITEM_LIVES_MAX
                    SpecialItemsType.SLOW_DOWN -> _freezers.value < ITEM_FREEZERS_MAX
                }
            }

            return items.randomOrNull()
        }
        else return null
    }





}