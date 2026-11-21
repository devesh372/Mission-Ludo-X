
package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
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
        Offset(2.5f, 11.5f),
        Offset(4.5f, 11.5f),
        Offset(2.5f, 13.5f),
        Offset(4.5f, 13.5f)
    ),
    listOf(
        Offset(2.5f, 2.5f),
        Offset(4.5f, 2.5f),
        Offset(2.5f, 4.5f),
        Offset(4.5f, 4.5f)
    ),
    listOf(
        Offset(10.5f, 2.5f),
        Offset(12.5f, 2.5f),
        Offset(10.5f, 4.5f),
        Offset(12.5f, 4.5f)
    ),
    listOf(
        Offset(10.5f, 10.5f),
        Offset(12.5f, 10.5f),
        Offset(10.5f, 12.5f),
        Offset(12.5f, 12.5f)
    )
)

private fun pawnCenter(
    playerIndex: Int,
    pawn: Int,
    position: Int
): Offset {
    return if (position == -1) {
        homes[playerIndex][pawn]
    } else if (position < 52) {
        val routeIndex =
            (players[playerIndex].start + position) % 52
        val (x, y) = route[routeIndex]
        Offset(x + 0.5f, y + 0.5f)
    } else {
        Offset(7.5f, 7.5f)
    }
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
                .background(Color(0xFF151525))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "MISSION LUDO X",
                color = Color.White,
                fontSize = 30.sp
            )

            Spacer(Modifier.height(35.dp))

            Text(
                "SELECT PLAYERS",
                color = Color.White,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (2..4).forEach { count ->
                    Button(onClick = { playerCount = count }) {
                        Text(
                            if (playerCount == count) "$count ✓"
                            else "$count"
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
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.height(30.dp))

            Button(onClick = { started = true }) {
                Text("START GAME")
            }
        }
    }
}

@Composable
fun LocalGameScreen(
    playerCount: Int,
    onBack: () -> Unit
) {
    val activePlayers = players.take(playerCount)

    val positions = remember {
        List(4) {
            mutableStateListOf(-1, -1, -1, -1)
        }
    }

    // Records the order in which each player unlocks pawns.
    val unlockOrders = remember {
        List(4) {
            mutableStateListOf<Int>()
        }
    }

    // Each player has independent sequence progress.
    val sequenceIndices = remember {
        List(4) {
            mutableIntStateOf(0)
        }
    }

    var currentTurn by remember { mutableIntStateOf(0) }
    var dice by remember { mutableIntStateOf(0) }
    var isRolling by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Tap the dice to roll!") }

    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val currentPlayer = activePlayers[currentTurn]

    fun rollDice() {
        if (dice != 0 || isRolling) return

        val order = unlockOrders[currentTurn].toList()

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

        scope.launch {
            isRolling = true

            rotation.animateTo(
                targetValue = rotation.value + 720f,
                animationSpec = tween(700)
            )

            dice = result
            isRolling = false

            message = "You rolled $result. Tap your pawn!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151525))
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "MISSION LUDO X",
            color = Color.White,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "${currentPlayer.name.uppercase()}'S TURN",
            color = currentPlayer.color,
            fontSize = 22.sp
        )

        Spacer(Modifier.height(12.dp))

        LudoBoard(
            positions = positions,
            currentPlayerIndex = currentTurn,
            onPawnTap = { pawn ->
                val oldPosition = positions[currentTurn][pawn]

                when {
                    isRolling -> {
                        message = "Wait for the dice!"
                    }

                    dice == 0 -> {
                        message = "Tap the dice first!"
                    }

                    oldPosition == 52 -> {
                        message = "That pawn has finished!"
                        dice = 0
                        currentTurn =
                            (currentTurn + 1) % playerCount
                    }

                    oldPosition == -1 && dice != 6 -> {
                        message = "You need a 6 to unlock this pawn."
                    }

                    else -> {
                        val wasLocked = oldPosition == -1

                        positions[currentTurn][pawn] =
                            if (wasLocked) {
                                0
                            } else {
                                (oldPosition + dice).coerceAtMost(52)
                            }

                        if (wasLocked) {
                            unlockOrders[currentTurn].add(pawn + 1)
                        }

                        val order =
                            unlockOrders[currentTurn].toList()

                        message = when (order) {
                            listOf(1, 2, 3, 4) ->
                                if (wasLocked && order.size == 4)
                                    "Special Mode A activated!"
                                else
                                    "Pawn ${pawn + 1} moved!"

                            listOf(4, 2, 1, 3) ->
                                if (wasLocked && order.size == 4)
                                    "Special Mode B activated!"
                                else
                                    "Pawn ${pawn + 1} moved!"

                            else -> {
                                if (wasLocked) {
                                    "Pawn ${pawn + 1} unlocked!"
                                } else {
                                    "Pawn ${pawn + 1} moved!"
                                }
                            }
                        }

                        if (dice == 6) {
                            message += " Roll again!"
                        } else {
                            currentTurn =
                                (currentTurn + 1) % playerCount
                        }

                        dice = 0
                    }
                }
            }
        )

        Spacer(Modifier.height(18.dp))

        // Tap the animated dice itself to roll.
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(10.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .graphicsLayer {
                    rotationY = rotation.value
                    rotationZ = rotation.value
                    cameraDistance = 12 * density
                }
                .pointerInput(dice, isRolling) {
                    detectTapGestures {
                        rollDice()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (dice == 0 || isRolling) {
                Text(
                    "🎲",
                    fontSize = 45.sp
                )
            } else {
                DiceFace(dice)
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            message,
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "TAP THE DICE TO ROLL",
            color = Color.White,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "PLAYERS: ${
                activePlayers.joinToString(" → ") { it.name }
            }",
            color = Color.White,
            fontSize = 13.sp
        )

        TextButton(onClick = onBack) {
            Text("BACK TO MENU")
        }

        Text(
            "CREATED BY DEVESH",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DiceFace(value: Int) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        val dotRadius = size.minDimension * 0.09f
        val left = size.width * 0.22f
        val centerX = size.width * 0.5f
        val right = size.width * 0.78f
        val top = size.height * 0.22f
        val centerY = size.height * 0.5f
        val bottom = size.height * 0.78f

        val dots = when (value) {
            1 -> listOf(Offset(centerX, centerY))
            2 -> listOf(
                Offset(left, top),
                Offset(right, bottom)
            )
            3 -> listOf(
                Offset(left, top),
                Offset(centerX, centerY),
                Offset(right, bottom)
            )
            4 -> listOf(
                Offset(left, top),
                Offset(right, top),
                Offset(left, bottom),
                Offset(right, bottom)
            )
            5 -> listOf(
                Offset(left, top),
                Offset(right, top),
                Offset(centerX, centerY),
                Offset(left, bottom),
                Offset(right, bottom)
            )
            else -> listOf(
                Offset(left, top),
                Offset(right, top),
                Offset(left, centerY),
                Offset(right, centerY),
                Offset(left, bottom),
                Offset(right, bottom)
            )
        }

        dots.forEach { point ->
            drawCircle(
                color = Color(0xFF171923),
                radius = dotRadius,
                center = point
            )
        }
    }
}

@Composable
fun LudoBoard(
    positions: List<List<Int>>,
    currentPlayerIndex: Int,
    onPawnTap: (Int) -> Unit
) {
    val positionSnapshot = positions.map { it.toList() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.White)
            .pointerInput(
                positionSnapshot,
                currentPlayerIndex,
                onPawnTap
            ) {
                detectTapGestures { tap ->
                    val cell = size.width / 15f

                    var selectedPawn = -1
                    var closestDistance = cell * 0.55f

                    for (pawn in 0..3) {
                        val center = pawnCenter(
                            currentPlayerIndex,
                            pawn,
                            positionSnapshot[currentPlayerIndex][pawn]
                        )

                        val point = Offset(
                            center.x * cell,
                            center.y * cell
                        )

                        val distance = (tap - point).getDistance()

                        if (distance < closestDistance) {
                            closestDistance = distance
                            selectedPawn = pawn
                        }
                    }

                    if (selectedPawn != -1) {
                        onPawnTap(selectedPawn)
                    }
                }
            }
    ) {
        val cell = size.width / 15f

        drawRect(
            red,
            Offset(0f, 0f),
            Size(cell * 6, cell * 6)
        )

        drawRect(
            green,
            Offset(cell * 9, 0f),
            Size(cell * 6, cell * 6)
        )

        drawRect(
            blue,
            Offset(0f, cell * 9),
            Size(cell * 6, cell * 6)
        )

        drawRect(
            yellow,
            Offset(cell * 9, cell * 9),
            Size(cell * 6, cell * 6)
        )

        val homeData = listOf(
            Triple(0, 0, red),
            Triple(9, 0, green),
            Triple(0, 9, blue),
            Triple(9, 9, yellow)
        )

        homeData.forEach { (x, y, _) ->
            drawRect(
                Color.White,
                Offset((x + 1) * cell, (y + 1) * cell),
                Size(cell * 4, cell * 4)
            )
        }

        route.forEach { (x, y) ->
            drawRect(
                Color(0xFFECECEC),
                Offset(x * cell, y * cell),
                Size(cell, cell)
            )

            drawRect(
                Color.DarkGray,
                Offset(x * cell, y * cell),
                Size(cell, cell),
                style = Stroke(1f)
            )
        }

        drawRect(
            Color(0xFF222222),
            Offset(cell * 6, cell * 6),
            Size(cell * 3, cell * 3)
        )

        drawCircle(
            Color.White,
            cell,
            Offset(cell * 7.5f, cell * 7.5f)
        )

        players.forEachIndexed { playerIndex, player ->
            for (pawn in 0..3) {
                val center = pawnCenter(
                    playerIndex,
                    pawn,
                    positions[playerIndex][pawn]
                )

                val point = Offset(
                    center.x * cell,
                    center.y * cell
                )

                drawCircle(
                    Color.Black,
                    cell * 0.43f,
                    point
                )

                drawCircle(
                    player.color,
                    cell * 0.34f,
                    point
                )

                drawCircle(
                    Color.White,
                    cell * 0.12f,
                    point
                )
            }
        }
    }
}
