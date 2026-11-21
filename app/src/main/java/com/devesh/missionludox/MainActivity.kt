
package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GameScreen()
            }
        }
    }
}

private val red = Color(0xFFE53935)
private val green = Color(0xFF43A047)
private val yellow = Color(0xFFFFD600)
private val blue = Color(0xFF1E88E5)

private val route = buildList {
    for (x in 0..13) add(x to 0)
    for (y in 1..13) add(13 to y)
    for (x in 12 downTo 0) add(x to 13)
    for (y in 12 downTo 1) add(0 to y)
}

@Composable
fun GameScreen() {
    var dice by remember { mutableIntStateOf(0) }
    var message by remember { mutableStateOf("Roll a 6 to unlock a pawn!") }
    val pawns = remember { mutableStateListOf(-1, -1, -1, -1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151525))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            "MISSION LUDO X",
            color = Color.White,
            fontSize = 27.sp
        )

        Text(
            "RED PLAYER",
            color = red,
            fontSize = 17.sp
        )

        LudoBoard(pawns)

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

        Button(onClick = {
            dice = Random.nextInt(1, 7)
            message = if (dice == 6) {
                "You rolled 6! Move a pawn."
            } else {
                "You rolled $dice. Roll a 6 to unlock."
            }
        }) {
            Text("ROLL DICE")
        }

        Button(
            onClick = {
                if (dice == 0) {
                    message = "Roll the dice first!"
                } else {
                    val locked = pawns.indexOfFirst { it == -1 }

                    if (locked != -1 && dice == 6) {
                        pawns[locked] = 0
                        message = "Pawn ${locked + 1} unlocked!"
                    } else {
                        val moving = pawns.indexOfFirst { it in 0 until 52 }

                        if (moving != -1) {
                            pawns[moving] =
                                (pawns[moving] + dice).coerceAtMost(52)

                            message = if (pawns[moving] == 52) {
                                "Pawn ${moving + 1} reached the finish!"
                            } else {
                                "Pawn ${moving + 1} moved $dice steps!"
                            }
                        } else {
                            message = "Roll a 6 to unlock a pawn!"
                        }
                    }
                    dice = 0
                }
            }
        ) {
            Text("MOVE PAWN")
        }

        Text(
            "CREATED BY DEVESH",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LudoBoard(pawns: List<Int>) {
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
        listOf(
            Triple(0, 0, red),
            Triple(9, 0, green),
            Triple(0, 9, blue),
            Triple(9, 9, yellow)
        ).forEach { (x, y, color) ->
            drawRect(
                Color.White,
                Offset((x + 1) * cell, (y + 1) * cell),
                Size(cell * 4, cell * 4)
            )

            for (i in 0..1) {
                for (j in 0..1) {
                    drawCircle(
                        color,
                        cell * 0.55f,
                        Offset(
                            (x + 2 + i * 2) * cell,
                            (y + 2 + j * 2) * cell
                        )
                    )
                }
            }
        }

        // Track cells
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
                style = androidx.compose.ui.graphics.drawscope.Stroke(1f)
            )
        }

        // Center
        drawRect(
            Color(0xFF222222),
            Offset(cell * 6, cell * 6),
            Size(cell * 3, cell * 3)
        )
        drawCircle(
            Color.White,
            cell * 1.1f,
            Offset(cell * 7.5f, cell * 7.5f)
        )

        // Four red pawns
        val homePositions = listOf(
            Offset(cell * 2.5f, cell * 2.5f),
            Offset(cell * 4.5f, cell * 2.5f),
            Offset(cell * 2.5f, cell * 4.5f),
            Offset(cell * 4.5f, cell * 4.5f)
        )

        pawns.forEachIndexed { index, position ->
            val pawnPosition = if (position == -1) {
                homePositions[index]
            } else if (position < route.size) {
                val (x, y) = route[position]
                Offset((x + 0.5f) * cell, (y + 0.5f) * cell)
            } else {
                Offset(cell * 7.5f, cell * 7.5f)
            }

            drawCircle(Color.Black, cell * 0.43f, pawnPosition)
            drawCircle(red, cell * 0.34f, pawnPosition)
            drawCircle(Color.White, cell * 0.12f, pawnPosition)
        }
    }
}
