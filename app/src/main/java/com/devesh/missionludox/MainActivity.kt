Kotlin

package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MissionLudoApp()
            }
        }
    }
}

private val red = Color(0xFFE53935)
private val green = Color(0xFF43A047)
private val yellow = Color(0xFFFFD600)
private val blue = Color(0xFF2196F3)

data class Player(
    val name: String,
    val color: Color,
    val start: Int
)

private val players = listOf(
    Player("Blue", blue, 39),
    Player("Red", red, 0),
    Player("Green", green, 13),
    Player("Yellow", yellow, 26)
)

private val route = buildList {
    for (x in 0..13) add(x to 0)
    for (y in 1..13) add(13 to y)
    for (x in 12 downTo 0) add(x to 13)
    for (y in 12 downTo 1) add(0 to y)
}

private val homes = listOf(
    listOf(
        Offset(2.5f, 11.5f), Offset(4.5f, 11.5f),
        Offset(2.5f, 13.5f), Offset(4.5f, 13.5f)
    ),
    listOf(
        Offset(2.5f, 2.5f), Offset(4.5f, 2.5f),
        Offset(2.5f, 4.5f), Offset(4.5f, 4.5f)
    ),
    listOf(
        Offset(10.5f, 2.5f), Offset(12.5f, 2.5f),
        Offset(10.5f, 4.5f), Offset(12.5f, 4.5f)
    ),
    listOf(
        Offset(10.5f, 10.5f), Offset(12.5f, 10.5f),
        Offset(10.5f, 12.5f), Offset(12.5f, 12.5f)
    )
)

private fun pawnCenter(
    playerIndex: Int,
    pawn: Int,
    position: Int
): Offset {
    return when {
        position == -1 -> homes[playerIndex][pawn]
        position < 52 -> {
            val routeIndex =
                (players[playerIndex].start + position) % 52
            val point = route[routeIndex]
            Offset(point.first + 0.5f, point.second + 0.5f)
        }
        else -> Offset(7f, 7f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHomeArea(
    x: Int,
    y: Int,
    color: Color,
    cell: Float
) {
    drawRect(
        color = color,
        topLeft = Offset(x * cell, y * cell),
        size = Size(6 * cell, 6 * cell)
    )

    drawRect(
        color = Color.White,
        topLeft = Offset((x + 1) * cell, (y + 1) * cell),
        size = Size(4 * cell, 4 * cell)
    )

    drawRect(
        color = color,
        topLeft = Offset((x + 1.5f) * cell, (y + 1.5f) * cell),
        size = Size(3 * cell, 3 * cell)
    )

    drawRect(
        color = Color(0xFF30394D),
        topLeft = Offset(x * cell, y * cell),
        size = Size(6 * cell, 6 * cell),
        style = Stroke(width = 2f)
    )
}

@Composable
fun MissionLudoApp() {
    var playerCount by remember { mutableIntStateOf(2) }
    var started by remember { mutableStateOf(false) }

    if (started) {
        LocalGameScreen(playerCount) {
            started = false
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101522))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "MISSION",
                color = Color.White,
                fontSize = 19.sp,
                letterSpacing = 5.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "LUDO X",
                color = Color(0xFFFFD54F),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(35.dp))

            Text(
                "SELECT PLAYERS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (2..4).forEach { count ->
                    Button(
                        onClick = { playerCount = count },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (playerCount == count)
                                Color(0xFFFFB300)
                            else
                                Color(0xFF30394D)
                        )
                    ) {
                        Text(
                            "$count",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
            Text("TURN ORDER", color = Color.LightGray)

            players.take(playerCount).forEach { player ->
                Text(
                    player.name,
                    color = player.color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = { started = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "START GAME",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlayerPanel(
    player: Player,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active)
                Color(0xFF303B50)
            else
                Color(0xFF1D2636)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (active) 5.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .shadow(3.dp, CircleShape)
                    .background(player.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    player.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(Modifier.width(8.dp))

            Column {
                Text(
                    player.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    if (active) "YOUR TURN" else "WAITING",
                    color = if (active)
                        Color(0xFFFFD54F)
                    else
                        Color.LightGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun LocalGameScreen(
    playerCount: Int,
    onBack: () -> Unit
) {
    val positions = remember {
        List(4) {
            mutableStateListOf(-1, -1, -1, -1)
        }
    }

    val unlockOrders = remember {
        List(4) {
            mutableStateListOf<Int>()
        }
    }

    val sequenceIndices = remember {
        List(4) {
            mutableIntStateOf(0)
        }
    }

    var currentTurn by remember { mutableIntStateOf(0) }
    var dice by remember { mutableIntStateOf(0) }
    var isRolling by remember { mutableStateOf(false) }

    var message by remember {
        mutableStateOf("Tap the dice to roll!")
    }

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val activePlayers = players.take(playerCount)
    val currentPlayer = activePlayers[currentTurn]

    fun hasLegalMove(
        playerIndex: Int,
        value: Int
    ): Boolean {
        return positions[playerIndex].any { position ->
            when {
                position == -1 -> value == 6
                position in 0..51 -> position + value <= 52
                else -> false
            }
        }
    }

    fun rollDice() {
        if (dice != 0 || isRolling) return

        val order = unlockOrders[currentTurn].toList()

        // SPECIAL DICE RULES - DO NOT CHANGE.
        val specialSequence = when (order) {
            listOf(1, 2, 3, 4) ->
                listOf(6, 6, 2, 1)

            listOf(4, 2, 1, 3) ->
                listOf(6, 4, 2)

            else -> null
        }

        val result = if (specialSequence != null) {
            val index = sequenceIndices[currentTurn].intValue
            sequenceIndices[currentTurn].intValue = index + 1
            specialSequence[index % specialSequence.size]
        } else {
            Random.nextInt(1, 7)
        }

        isRolling = true

        scope.launch {
            rotation.animateTo(
                targetValue = rotation.value + 720f,
                animationSpec = tween(700)
            )

            dice = result
            isRolling = false

            if (!hasLegalMove(currentTurn, result)) {
                message = "No valid moves. Next player's turn!"
                dice = 0
                currentTurn = (currentTurn + 1) % playerCount
            } else {
                message =
                    "${currentPlayer.name} rolled $result. Move a pawn!"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101522))
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MISSION LUDO X",
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Text(
            "THE ULTIMATE LUDO EXPERIENCE",
            color = Color(0xFFFFD54F),
            fontSize = 9.sp,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(15.dp))

        activePlayers.chunked(2).forEach { rowPlayers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPlayers.forEach { player ->
                    val index = players.indexOf(player)

                    PlayerPanel(
                        player = player,
                        active = currentTurn == index,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowPlayers.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF202A3C)
            )
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${currentPlayer.name.uppercase()}'S TURN",
                    color = currentPlayer.color,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(8.dp))

                LudoBoard(
                    positions = positions,
                    currentPlayerIndex = currentTurn,
                    playerCount = playerCount,
                    onPawnTap = { pawn ->
                        if (isRolling) {
                            message = "Wait for the dice!"
                        } else if (dice == 0) {
                            message = "Tap the dice first!"
                        } else {
                            val oldPosition =
                                positions[currentTurn][pawn]

                            when {
                                oldPosition == 52 -> {
                                    message =
                                        "That pawn has already finished!"
                                }

                                oldPosition == -1 && dice != 6 -> {
                                    message =
                                        "You need a 6 to unlock this pawn."
                                }

                                oldPosition >= 0 &&
                                    oldPosition + dice > 52 -> {
                                    message =
                                        "You need an exact number to finish!"
                                }

                                else -> {
                                    val rolledValue = dice
                                    val wasLocked = oldPosition == -1

                                    positions[currentTurn][pawn] =
                                        if (wasLocked) 0
                                        else oldPosition + rolledValue

                                    if (wasLocked) {
                                        unlockOrders[currentTurn].add(
                                            pawn + 1
                                        )
                                    }

                                    val order =
                                        unlockOrders[currentTurn].toList()

                                    message = when (order) {
                                        listOf(1, 2, 3, 4) ->
                                            if (wasLocked && order.size == 4)
                                                "SPECIAL MODE A ACTIVATED!"
                                            else
                                                "Pawn ${pawn + 1} moved!"

                                        listOf(4, 2, 1, 3) ->
                                            if (wasLocked && order.size == 4)
                                                "SPECIAL MODE B ACTIVATED!"
                                            else
                                                "Pawn ${pawn + 1} moved!"

                                        else ->
                                            if (wasLocked)
                                                "Pawn ${pawn + 1} unlocked!"
                                            else
                                                "Pawn ${pawn + 1} moved!"
                                    }

                                    dice = 0

                                    if (rolledValue != 6) {
                                        currentTurn =
                                            (currentTurn + 1) % playerCount
                                    } else {
                                        message += " Roll again!"
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        Box(
            modifier = Modifier
                .size(90.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .graphicsLayer {
                    rotationY = rotation.value
                    rotationZ = rotation.value
                    cameraDistance = 12 * density
                }
                .clickable(enabled = dice == 0 && !isRolling) {
                    rollDice()
                },
            contentAlignment = Alignment.Center
        ) {
            if (dice == 0 || isRolling) {
                Text("🎲", fontSize = 48.sp)
            } else {
                DiceFace(dice)
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            if (isRolling) "ROLLING..."
            else if (dice != 0) "MOVE YOUR PAWN"
            else "TAP THE DICE TO ROLL",
            color = Color(0xFFFFD54F),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            message,
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBack) {
            Text("BACK TO MENU", color = Color.LightGray)
        }

        Text(
            "CREATED BY DEVESH",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

@Composable
fun LudoBoard(
    positions: List<List<Int>>,
    currentPlayerIndex: Int,
    playerCount: Int,
    onPawnTap: (Int) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(
                positions,
                currentPlayerIndex,
                playerCount
            ) {
                detectTapGestures { tap ->
                    val cell = size.width / 14f
                    var selectedPawn = -1
                    var nearestDistance = Float.MAX_VALUE

                    for (pawn in 0..3) {
                        val center = pawnCenter(
                            currentPlayerIndex,
                            pawn,
                            positions[currentPlayerIndex][pawn]
                        )

                        val dx = tap.x - center.x * cell
                        val dy = tap.y - center.y * cell
                        val distance = dx * dx + dy * dy

                        if (distance < nearestDistance) {
                            nearestDistance = distance
                            selectedPawn = pawn
                        }
                    }

                    if (
                        selectedPawn >= 0 &&
                        nearestDistance < cell * cell * 0.5f
                    ) {
                        onPawnTap(selectedPawn)
                    }
                }
            }
    ) {
        val cell = size.width / 14f

        drawRect(Color.White)

        drawHomeArea(0, 8, blue, cell)
        drawHomeArea(0, 0, red, cell)
        drawHomeArea(8, 0, green, cell)
        drawHomeArea(8, 8, yellow, cell)

        route.forEach { point ->
            drawRect(
                color = Color(0xFFE8EAF0),
                topLeft = Offset(
                    poin
