package compose
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solid.number2048.game.ANIM_SPEED
import com.solid.number2048.game.BOARD_HEIGHT
import com.solid.number2048.game.BOARD_WIDTH
import com.solid.number2048.game.BOXES_QUEUE_SIZE
import com.solid.number2048.game.FALL_SPEED
import com.solid.number2048.presenter.GameVM
import com.solid.number2048.ui.theme.BG_6
import com.solid.number2048.ui.theme.BG_7
import com.solid.number2048.ui.theme.BG_8
import com.solid.number2048.ui.theme.BG_9
import com.solid.number2048.game.entities.BoxTypes
import com.solid.number2048.game.entities.FallingBox
import com.solid.number2048.game.entities.MergingBox
import com.solid.number2048.game.entities.MergingTargetBox
import com.solid.number2048.game.entities.UserInputEffects
import com.solid.number2048.game.entities.bronze
import com.solid.number2048.ui.compose.CalcRecomposes
import com.solid.number2048.ui.compose.WithMeasures
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun DrawGameScreen(){
    val gameVM = GameVM()

    val boxesQueue = gameVM.boxesQueueState.collectAsState()

    val gameScore = gameVM.gameScore.collectAsState()

    val density = LocalDensity.current.density

    var boardWidth : Dp = 0.dp

    val isInvalidInput = remember { mutableStateOf(false) }

    val clickHighlight : MutableState<UserInputEffects.ClickHighlight?> = remember {
        mutableStateOf(null)
    }

    val isGamePlaying = gameVM.isGamePlaying.collectAsState()


    LaunchedEffect(Unit){
        while (true){
            withFrameMillis {
                gameVM.onNewFrame(it)
            }
        }
    }

    LaunchedEffect(Unit){

        var errorShowTime : Job? = null

        gameVM.userInputEffects.collectLatest {

            when(it){
                is UserInputEffects.ClickHighlight -> {
                    clickHighlight.value = it
                }

                UserInputEffects.InvalidInput -> {
                    isInvalidInput.value = true
                    errorShowTime?.cancel()
                    errorShowTime = launch {
                        delay(300)
                        isInvalidInput.value = false
                    }
                }
            }
        }
    }

    fun onUserInput(offset : Offset, isTap: Boolean){
        val x = offset.x / density
        val pos = x / (boardWidth / BOARD_WIDTH).value
        gameVM.onUserBoardInput(pos.toInt(), isTap)
    }


    WithMeasures(modifier = Modifier.fillMaxSize()){ widthDp, heightDp ->

        Column (modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            DrawHeader(
                gameScore = gameScore,
                boxesQueue = boxesQueue
            )

            BoxWithConstraints(
                modifier = Modifier
                    .heightIn(max = (heightDp - 200).dp)
                    .aspectRatio(BOARD_WIDTH / BOARD_HEIGHT.toFloat())
            ) {

                SideEffect {
                    boardWidth = maxWidth
                }

                val rowWidth = maxWidth / BOARD_WIDTH


                DrawBoardBG(
                    rowWidth = rowWidth,
                    maxHeight = maxHeight,
                    clickHighlight = clickHighlight,
                    onUserInput = { offset: Offset, isTap: Boolean ->
                        onUserInput(
                            offset, isTap
                        )
                    }
                )

                CalcRecomposes("main board")

                DrawBoxes(gameVM, rowWidth)

                ShowInvalidInput(isInvalidInput)

            }


            DrawFooter(
                isPlaying = isGamePlaying,
                playStopClick = gameVM::onTogglePlayStop,
                save = gameVM::save
            )
        }
    }
}


@Composable
fun ColumnScope.DrawFooter(
    playStopClick : () -> Unit,
    isPlaying : State<Boolean>,
    save: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color.DarkGray)
        ){

        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Button(onClick = playStopClick){
                Icon(
                    if(isPlaying.value) Icons.Filled.Lock
                    else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }

            Button(onClick = save){
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Composable
fun DrawBoxes(
    gameVM: GameVM,
    rowWidth: Dp
){

    val curNum = gameVM.curNumBox.collectAsStateWithLifecycle()

    val fallingBoxes = gameVM.fallingBoxes.collectAsStateWithLifecycle()

    val mergingBoxes = gameVM.mergingBoxes.collectAsStateWithLifecycle()

    val board = gameVM.board.collectAsStateWithLifecycle()

    val mergeTargetBox = gameVM.mergeTargetBox.collectAsStateWithLifecycle()

    CalcRecomposes("ALL BOXES")


    DrawMergingBoxes(
        mergingBoxes = mergingBoxes,
        rowWidth = rowWidth
    )


    DrawCurNum(
        curNum = curNum,
        rowWidth = rowWidth
    )


    DrawFallingBoxes(
        fallingBoxes = fallingBoxes,
        rowWidth = rowWidth
    )

    DrawBoard(
        board = board,
        rowWidth = rowWidth
    )

    DrawMergeTargetBox(
        box = mergeTargetBox,
        rowWidth = rowWidth
    )
}


@Composable
fun DrawBoardBG(
    rowWidth: Dp,
    maxHeight: Dp,
    clickHighlight: State<UserInputEffects.ClickHighlight?>,
    onUserInput : (offset : Offset, isTap: Boolean) -> Unit
){

    Row (modifier = Modifier
        .fillMaxSize()
        .padding(top = rowWidth)
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                onUserInput(it, true)
            })
        }
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                onUserInput(change.position, false)
            }
        }

    ) {

        repeat(BOARD_WIDTH){

            val isEven = it % 2 == 0

            Box (modifier = Modifier
                .fillMaxHeight()
                .width(rowWidth)
                .background(color = if (isEven) BG_8 else BG_9)
            )
        }
    }


    HighlightClicks(
        click = clickHighlight,
        rowWidth = rowWidth,
        maxHeight = maxHeight
    )


    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(rowWidth)
            .background(Color.DarkGray)
    ) {
        repeat(BOARD_WIDTH){

            val isEven = it % 2 == 0

            Box (modifier = Modifier
                .fillMaxHeight()
                .width(rowWidth)
                .padding((rowWidth * 0.05f))
                .clip(RoundedCornerShape(rowWidth * 0.1f))
                .background(color = if (isEven) BG_6 else BG_7),
            ) {

                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.DarkGray,
                    modifier = Modifier.fillMaxSize()
                )

            }
        }
    }
}


@Composable
fun ColumnScope.DrawHeader(
    gameScore : State<Int>,
    boxesQueue: State<List<BoxTypes>>
){
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(Color.DarkGray)
        .weight(1f)
        .zIndex(666f),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(modifier = Modifier
            .align(Alignment.CenterHorizontally),
            text = "Score : ${gameScore.value}",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.heightIn(10.dp))

        Row (modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Next box",
                modifier = Modifier.padding(start = 10.dp),
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )

            DrawBoxesQueue(boxesQueue)

        }

    }
}


@Composable
fun DrawBoxesQueue(
    queue: State<List<BoxTypes>>
){

    val offsetX = animateDpAsState(
        targetValue = if(queue.value.size == BOXES_QUEUE_SIZE) 0.dp else 50.dp,
        animationSpec = tween(
            durationMillis = if(queue.value.size == BOXES_QUEUE_SIZE) 300 else 0, easing = LinearEasing
        )
    )

    Row(
        modifier = Modifier.offset(x = offsetX.value)
    ) {

        queue.value.forEach {

            DrawNumBox(
                numBox = it,
                x = 0f,
                y = 0f,
                rowWidth = 50.dp
            )
        }
    }
}

@Composable
fun HighlightClicks(
    click : State<UserInputEffects.ClickHighlight?>,
    rowWidth: Dp,
    maxHeight : Dp
){

    val animSpeed = remember { BOARD_HEIGHT * 1000 / FALL_SPEED }


    click.value?.let { cl ->

        val isToShow = remember(cl) { MutableTransitionState(false).apply {
            targetState = true
        } }


        AnimatedVisibility(
            visibleState = isToShow,
            enter = slideInVertically(animationSpec = tween(animSpeed, easing = LinearEasing), initialOffsetY = {
                -it * 2
            }),
            exit = fadeOut(animationSpec = tween(0)),
            modifier = Modifier.offset(x = rowWidth * cl.col, y = maxHeight)
        ){

            LaunchedEffect(cl){
                delay(animSpeed.toLong())
                isToShow.targetState = false
            }

            Box(modifier = Modifier
                .fillMaxHeight()
                .width(rowWidth)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Transparent, cl.color, cl.color, Color.Transparent
                        )
                    ), shape = RectangleShape, alpha = 0.2f
                )
            )
        }
    }
}



@Composable
fun ShowInvalidInput(
    isInvalidInput: MutableState<Boolean>
){

    AnimatedVisibility(
        visible = isInvalidInput.value,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(400))
    ){
        Box(modifier = Modifier
            .fillMaxSize()
            .border(
                width = 5.dp, brush = Brush.verticalGradient(
                    listOf(
                        Color.Red, bronze, Color.Transparent
                    )
                ), shape = RectangleShape
            )
        )
    }
}



@Composable
fun DrawMergeTargetBox(
    box: State<MergingTargetBox?>,
    rowWidth: Dp
){

    box.value?.let { b ->

        val color = remember { Animatable(b.startBox.color) }

        val animSpeed = remember { 1000 / ANIM_SPEED / 2 }

        LaunchedEffect(Unit){
            color.animateTo(b.targetBox.color, animationSpec = tween(1000 / ANIM_SPEED))
        }

        var count by remember { mutableStateOf(b.startBox.label)}

        Box(modifier = Modifier
            .offset(
                x = (rowWidth * b.x),
                y = (rowWidth * b.y)
            )
            .size(rowWidth)
            .padding((rowWidth * 0.05f))
            .clip(RoundedCornerShape((rowWidth * 0.1f)))
//            .border(width = (rowWidth * 0.03f), color = b.startBox.border)
            .background(color.value),
            contentAlignment = Alignment.Center
        ){
            AnimatedContent(targetState = count, transitionSpec = {
                scaleIn(animationSpec = tween(animSpeed)) togetherWith scaleOut(animationSpec = tween(animSpeed))
            }){ number ->

                LaunchedEffect(Unit){
                    count = b.targetBox.label
                }
                DrawNumberText(number, rowWidth)
            }
        }
    }
}


@Composable
fun DrawMergingBoxes(
    mergingBoxes: State<List<MergingBox>>,
    rowWidth: Dp
){

    mergingBoxes.value.forEach {
        DrawNumBox(
            numBox = it.numBox,
            rowWidth = rowWidth,
            x = it.x,
            y = it.y
        )
    }
}

@Composable
fun DrawFallingBoxes(
    fallingBoxes: State<List<FallingBox>>,
    rowWidth: Dp
){

    fallingBoxes.value.forEach {
        DrawNumBox(
            numBox = it.numBox,
            rowWidth = rowWidth,
            x = it.x,
            y = it.y
        )
    }
}


@Composable
fun DrawBoard(
    board: State<Array<Array<BoxTypes?>>>,
    rowWidth: Dp
){

//    println("BOARD DRAWING")

    board.value.forEachIndexed { y, numBoxes ->
        numBoxes.forEachIndexed { x, numBox ->
            if(numBox != null){
                DrawNumBox(
                    numBox = numBox,
                    rowWidth = rowWidth,
                    x = x.toFloat(),
                    y = y.toFloat()
                )
            }
        }
    }
}



@Composable
fun DrawCurNum(
    curNum : State<FallingBox?>,
    rowWidth : Dp
){

    curNum.value?.let{ box ->


        if(box.scale == 1f){
            DrawNumBox(
                numBox = box.numBox,
                rowWidth = rowWidth,
                x = box.x,
                y = box.targetY.toFloat(),
                alpha = 0.2f
            )
        }

        DrawNumBox(
            numBox = box.numBox,
            rowWidth = rowWidth,
            x = box.x,
            y = box.y,
            scale = box.scale
        )
    }
}


@Composable
fun DrawNumBox(
    numBox: BoxTypes,
    x: Float,
    y: Float,
    rowWidth: Dp,
    alpha: Float = 1f,
    scale : Float = 1f
){

    Box(modifier = Modifier
        .offset(
            x = (x * rowWidth.value).dp,
            y = (y * rowWidth.value).dp
        )
        .size(rowWidth)
        .padding((rowWidth * 0.05f))
        .clip(RoundedCornerShape((rowWidth * 0.1f)))
        .scale(scale)
//        .border(width = (rowWidth * 0.03f), color = numBox.border.copy(alpha = alpha))
        .background(numBox.color.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ){
        DrawNumberText(numBox.label, rowWidth, alpha)
    }
}

@Composable
fun DrawNumberText(num : String, rowWidth: Dp, alpha: Float = 1f){
    Text(
        text = num,
        fontSize = rowWidth.value.sp / (num.length).coerceAtLeast(2),
        fontWeight = FontWeight.ExtraBold,
        color = Color.White.copy(alpha = alpha)
    )
}

