package compose
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solid.number2048.game.ANIM_SPEED
import com.solid.number2048.game.BOARD_HEIGHT
import com.solid.number2048.game.BOARD_WIDTH
import com.solid.number2048.game.BOXES_QUEUE_MAX
import com.solid.number2048.game.FALL_SPEED
import com.solid.number2048.presenter.GameVM
import com.solid.number2048.ui.theme.BG_6
import com.solid.number2048.ui.theme.BG_7
import com.solid.number2048.ui.theme.BG_8
import com.solid.number2048.ui.theme.BG_9
import com.solid.number2048.game.entities.BoxTypes
import com.solid.number2048.game.entities.FallingBox
import com.solid.number2048.game.entities.GameSpeed
import com.solid.number2048.game.entities.MergingBox
import com.solid.number2048.game.entities.MergingTargetBox
import com.solid.number2048.game.entities.QueueState
import com.solid.number2048.game.entities.SpecialItemsType
import com.solid.number2048.game.entities.StaticBox
import com.solid.number2048.game.entities.UserInputEffects
import com.solid.number2048.game.entities.bronze
import com.solid.number2048.presenter.GameViewModel
import com.solid.number2048.ui.compose.CalcRecomposes
import com.solid.number2048.ui.compose.Thermometer
import com.solid.number2048.ui.compose.WithMeasures
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



@Composable
fun DrawGameScreen(
    gameVM: GameViewModel
){

    val density = LocalDensity.current.density

    var boardWidth : Dp = 0.dp

    val isInvalidInput = remember { mutableStateOf(false) }

    val clickHighlight : MutableState<UserInputEffects.ClickHighlight?> = remember {
        mutableStateOf(null)
    }

    val itemsObtained : MutableState<UserInputEffects.ObtainedItems?> = remember {

        mutableStateOf(null)
    }

    val isGamePlaying = gameVM.isGamePlaying.collectAsState()


    LifecycleEventEffect(event = Lifecycle.Event.ON_PAUSE) {
        gameVM.onTogglePlayStop(true)
    }


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

                is UserInputEffects.ObtainedItems -> {
                    itemsObtained.value = it
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
                gameVM = gameVM
            )

            BoxWithConstraints(
                modifier = Modifier
                    .heightIn(max = (heightDp - 222).dp)
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
                    itemsObtained = itemsObtained,
                    onUserInput = { offset: Offset, isTap: Boolean ->
                        onUserInput(
                            offset, isTap
                        )
                    }
                )

                CalcRecomposes("game_screen", "R_GAME_SCREEN")

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
    gameVM: GameViewModel,
    rowWidth: Dp
){

    val fallingBoxes = gameVM.fallingBoxes.collectAsStateWithLifecycle()

    val curNumBox = gameVM.curNumBox.collectAsStateWithLifecycle()

    val mergingBoxes = gameVM.mergingBoxes.collectAsStateWithLifecycle()

    val board = gameVM.board.collectAsStateWithLifecycle()

    val mergeTargetBox = gameVM.mergeTargetBox.collectAsStateWithLifecycle()

    CalcRecomposes("ALL BOXES", "R_ALL_BOXES")


    DrawMergingBoxes(
        mergingBoxes = mergingBoxes,
        rowWidth = rowWidth
    )


    DrawCurNum(
        curNum = curNumBox,
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
    itemsObtained: State<UserInputEffects.ObtainedItems?>,
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

    AnimateObtainedItems(
        itemsObtained = itemsObtained,
        rowWidth = rowWidth
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
fun AnimateObtainedItems(
    itemsObtained: State<UserInputEffects.ObtainedItems?>,
    rowWidth: Dp
){

    itemsObtained.value?.let {

        val scale = remember(it) { androidx.compose.animation.core.Animatable(1f) }
        val alphaA = remember(it) { androidx.compose.animation.core.Animatable(1f) }

        LaunchedEffect(key1 = it) {
            scale.animateTo(targetValue = 4f, animationSpec = tween(300))
        }

        LaunchedEffect(key1 = it) {
            alphaA.animateTo(0f, animationSpec = tween(300, delayMillis = 150))
        }


        it.items.forEach {

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (it.col * rowWidth.value * this.density).toInt(),
                            y = (it.row * rowWidth.value * this.density).toInt()
                        )
                    }
                    .size(rowWidth)
                    .zIndex(99999f)
                    .padding(rowWidth * 0.05f)
            ) {

                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            alpha = alphaA.value
                        }
                        .fillMaxSize(0.4f)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}


@Composable
fun ColumnScope.DrawHeader(
    gameVM: GameViewModel
){

    val gameScore : State<Int> = gameVM.gameScore.collectAsStateWithLifecycle()
    val gameSpeed: State<GameSpeed> = gameVM.gameSpeedState.collectAsStateWithLifecycle()
    val queueState = gameVM.queueState.collectAsStateWithLifecycle()

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Color.DarkGray)
        .weight(1f)
        .zIndex(666f)
    ){

        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ){
            Thermometer(gameSpeed)
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp),
            contentAlignment = Alignment.TopCenter
        ){
            DrawScore(gameScore = gameScore)
        }


        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp),
            contentAlignment = Alignment.BottomEnd
        ){
            DrawBoxesQueueBG()
            DrawBoxesQueue(
            queueState = queueState
            )
        }
    }
}


@Composable
fun DrawScore(
    gameScore : State<Int>,
){
    Text(
        text = "${gameScore.value}",
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White.copy(alpha = 0.6f)
    )
}


@Composable
fun DrawBoxesQueueBG(){
    Row {
        repeat(BOXES_QUEUE_MAX){
            Box(modifier = Modifier
                .size(50.dp)
                .padding((2.5.dp))
                .border(
                    width = (4.dp),
                    color = Color.LightGray.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(5.dp)
                )
            )
        }
    }
}


@Composable
fun DrawBoxesQueue(
    queueState: State<QueueState>
){


    val offsetX = animateDpAsState(
        targetValue = if(queueState.value.freeSpots == 0) 0.dp else 50.dp * (queueState.value.freeSpots),
        animationSpec = tween(
            durationMillis = if(queueState.value.freeSpots == 0) 300 else 0, easing = LinearEasing
        ), label = "queue animation"
    )
    
    CalcRecomposes(label = "Queue", "R_QUEUE")
    
    Row(
        modifier = Modifier
            .offset {
                IntOffset(x = (offsetX.value * this.density).value.toInt(), y = 0)
            }
    ) {

        queueState.value.queue.forEach {

            DrawNumBox(
                numBox = it,
                x = 0f,
                y = 0f,
                rowWidth = 50.dp,
                item = null
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
            }
        }


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
    
    CalcRecomposes(label = "merging target box", "R_TARGET_BOX")


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
            .drawBehind {
                drawRect(color.value)
            },
            contentAlignment = Alignment.Center
        ){
            AnimatedContent(targetState = count, transitionSpec = {
                scaleIn(animationSpec = tween(animSpeed)) togetherWith scaleOut(animationSpec = tween(animSpeed))
            }, label = "animating box number on merge"){ number ->

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

    CalcRecomposes(label = "merging boxes", "R_FALLING_BOXES")


    val boxesCount = remember {
        derivedStateOf {
            mergingBoxes.value.size
        }
    }

    repeat(boxesCount.value){

        DrawNumBox(
            getNumBox = { mergingBoxes.value[it].numBox },
            getX = { mergingBoxes.value[it].x },
            getY = { mergingBoxes.value[it].y },
            rowWidth = rowWidth,
        )
    }
}

@Composable
fun DrawFallingBoxes(
    fallingBoxes: State<List<FallingBox>>,
    rowWidth: Dp
){
    
    CalcRecomposes(label = "Drawing falling boxes", "R_FALLING_BOXES")

    val fallingBoxesCount = remember {
        derivedStateOf {
            fallingBoxes.value.size
        }
    }

    repeat(fallingBoxesCount.value){

        DrawNumBox(
            getNumBox = { fallingBoxes.value[it].numBox},
            getX = { fallingBoxes.value[it].x },
            getY = { fallingBoxes.value[it].y},
            rowWidth = rowWidth,
            getItem = {fallingBoxes.value[it].item}
        )
    }
}


@Composable
fun DrawBoard(
    board: State<Array<Array<StaticBox?>>>,
    rowWidth: Dp
){

//    println("BOARD DRAWING")

    board.value.forEachIndexed { y, numBoxes ->
        numBoxes.forEachIndexed { x, numBox ->
            if(numBox != null){
                DrawNumBox(
                    numBox = numBox.boxTypes,
                    rowWidth = rowWidth,
                    x = x.toFloat(),
                    y = y.toFloat(),
                    item = numBox.item
                )
            }
        }
    }
}



@Composable
fun DrawCurNum(
    curNum: State<FallingBox?>,
    rowWidth : Dp
){

    val isToDraw = remember {
        derivedStateOf {
            curNum.value != null
        }
    }

    CalcRecomposes(label = "drawCurBox", "R_CUR_BOX")

    if(isToDraw.value){

        DrawNumBox(
            getNumBox = { curNum.value?.numBox },
            getX = { curNum.value?.x ?: 0f },
            getY = { curNum.value?.targetY?.toFloat() ?: 0f},
            rowWidth = rowWidth,
            getAlpha = { 0.2f },
            getItem = { null }
        )
        DrawNumBox(
            getNumBox = { curNum.value?.numBox },
            getX = { curNum.value?.x ?: 0f},
            getY = { curNum.value?.y ?: 0f},
            rowWidth = rowWidth,
            getScale = { curNum.value?.scale ?: 0f},
            getItem = { curNum.value?.item},
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
    scale : Float = 1f,
    item : SpecialItemsType? = null
){

//    CalcRecomposes(label = "recomposing box")

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
        .background(numBox.color.copy(alpha = alpha))
    ){
        DrawNumberText(numBox.label, rowWidth, alpha)

        item?.let {
            DrawSpecialItem(item = it)
        }
    }
}


@Composable
fun DrawNumBox(
    getNumBox: () -> BoxTypes?,
    getX: () -> Float,
    getY: () -> Float,
    rowWidth: Dp,
    getAlpha: () -> Float = { 1f },
    getScale: () -> Float = { 1f },
    getItem : () -> SpecialItemsType? = { null }
){


    val numBox = remember {
        getNumBox()
    }

    val alpha = remember {
        getAlpha()
    }

    val item = remember {
        getItem()
    }


    numBox?.let {

        CalcRecomposes(label = "recomposing box", "R_DRAW_BOX_LAMBDA")


        Box(modifier = Modifier
            .offset {
                IntOffset(
                    x = (getX() * rowWidth.value * this.density).toInt(),
                    y = (getY() * rowWidth.value * this.density).toInt()
                )
            }
            .graphicsLayer {
                scaleX = getScale()
                scaleY = getScale()
            }

            .size(rowWidth)
            .padding((rowWidth * 0.05f))
            .clip(RoundedCornerShape((rowWidth * 0.1f)))

//        .border(width = (rowWidth * 0.03f), color = numBox.border.copy(alpha = alpha))
            .background(numBox.color.copy(alpha = alpha))
        ){
            DrawNumberText(numBox.label, rowWidth, alpha)

            item?.let {
                DrawSpecialItem(item = it)
            }
        }
    }
}



@Composable
fun BoxScope.DrawSpecialItem(
    item: SpecialItemsType
){

    var color = Color.Blue

    val icon = when(item){
        SpecialItemsType.QUEUE_EXPANDER -> {
            color = Color.Red
            Icons.Filled.Add
        }
        SpecialItemsType.QUBE_DESTROYER -> {
            color = Color.Green
            Icons.Filled.Build
        }
        SpecialItemsType.EXTRA_LIFE -> {
            color = Color.Magenta
            Icons.Filled.Favorite
        }
        SpecialItemsType.SLOW_DOWN -> {
            Icons.Filled.Star
        }
    }

    Icon(
        imageVector = icon,
        tint = color,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize(0.4f)
            .align(Alignment.TopEnd)
    )

}

@Composable
fun BoxScope.DrawNumberText(num : String, rowWidth: Dp, alpha: Float = 1f){
    Text(
        modifier = Modifier.align(Alignment.Center),
        text = num,
        fontSize = rowWidth.value.sp / (num.length).coerceAtLeast(2),
        fontWeight = FontWeight.ExtraBold,
        color = Color.White.copy(alpha = alpha)
    )
}

