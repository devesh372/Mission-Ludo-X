
package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

            Text("SELECT PLAYERS", color = Color.White, fontSize = 20.sp)

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

    // Four pawns for each of the four possible players.
    val positions = remember {
        List(4) { mutableStateListOf(-1, -1, -1, -1) }
    }

    var currentTurn by remember { mutableIntStateOf(0) }
    var dice by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Roll the dice!") }

    val currentPlayer = activePlayers[currentTurn]

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

        LudoBoard(positions)

        Spacer(Modifier.height(12.dp))

        Text(
            "🎲  $dice",
            color = Color.White,
            fontSize = 42.sp
        )

        Text(
            message,
            color = Color.LightGray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (dice == 0) {
                    dice = Random.nextInt(1, 7)
                    message = "You rolled $dice. Select a pawn."
                }
            },
            enabled = dice == 0
        ) {
            Text("ROLL DICE")
        }

        Text(
            "SELECT YOUR PAWN",
            color = Color.White,
            fontSize = 16.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..3).forEach { pawn ->
                Button(
                    onClick = {
                        val oldPosition = positions[currentTurn][pawn]

                        if (oldPosition == 52) {
                            message = "That pawn has finished!"
                        } else if (oldPosition == -1 && dice != 6) {
                            message = "You need a 6 to unlock this pawn."
                        } else if (dice == 0) {
                            message = "Roll the dice first!"
                        } else {
                            positions[currentTurn][pawn] =
                                if (oldPosition == -1) 0
                                else (oldPosition + dice).coerceAtMost(52)

                            message = "Pawn ${pawn + 1} moved!"

                            if (dice != 6) {
                                currentTurn =
                                    (currentTurn + 1) % playerCount
                            } else {
                                message += " Roll again!"
                            }

                            dice = 0
                        }
                    },
                    enabled = dice != 0
                ) {
                    Text("${pawn + 1}")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "PLAYERS: ${activePlayers.joinToString(" → ") { it.name }}",
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
fun LudoBoard(positions: List<List<Int>>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color.White)
    ) {
        val cell = size.width / 15f

        // Four home areas
        drawRect(red, Offset(0f, 0f), Size(cell * 6, cell * 6))
        drawRect(green, Offset(cell * 9, 0f), Size(cell * 6, cell * 6))
        drawRect(blue, Offset(0f, cell * 9), Size(cell * 6, cell * 6))
        drawRect(yellow, Offset(cell * 9, cell * 9), Size(cell * 6, cell * 6))

        // Home interiors
        val homeData = listOf(
            Triple(0, 0, red),
            Triple(9, 0, green),
            Triple(0, 9, blue),
            Triple(9, 9, yellow)
        )

        homeData.forEach { (x, y, color) ->
            drawRect(
                Color.White,
                Offset((x + 1) * cell, (y + 1) * cell),
                Size(cell * 4, cell * 4)
            )
        }

        // Track
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

        // Center finish
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

        // Pawn home positions
        val homes = listOf(
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

        players.forEachIndexed { playerIndex, player ->
            for (pawn in 0..3) {
                val position = positions[playerIndex][pawn]

                val center = if (position == -1) {
                    homes[playerIndex][pawn]
                } else if (position < 52) {
                    val routeIndex = (player.start + position) % 52
                    val (x, y) = route[routeIndex]
                    Offset(x + 0.5f, y + 0.5f)
                } else {
                    Offset(7.5f, 7.5f)
                }

                val point = Offset(
                    center.x * cell,
                    center.y * cell
                )

                drawCircle(Color.Black, cell * 0.43f, point)
                drawCircle(player.color, cell * 0.34f, point)
                drawCircle(Color.White, cell * 0.12f, point)
            }
        }
    }
}
