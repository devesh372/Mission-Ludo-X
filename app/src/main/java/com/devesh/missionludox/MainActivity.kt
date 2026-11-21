
package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                GameScreen()
            }
        }
    }
}

private val red = Color(0xFFE53935)
private val green = Color(0xFF43A047)
private val yellow = Color(0xFFFFD600)
private val blue = Color(0xFF1E88E5)
private val boardWhite = Color(0xFFFFFFFF)
private val boardLine = Color(0xFF777777)

@Composable
fun GameScreen() {
    var dice by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151525))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = "MISSION LUDO X",
            color = Color.White,
            fontSize = 27.sp
        )

        Text(
            text = "2 - 4 PLAYERS",
            color = Color.LightGray,
            fontSize = 14.sp
        )

        LudoBoard(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Text(
            text = "🎲  $dice",
            color = Color.White,
            fontSize = 42.sp
        )

        Button(
            onClick = {
                dice = Random.nextInt(1, 7)
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("ROLL DICE", fontSize = 18.sp)
        }

        Text(
            text = "CREATED BY DEVESH",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LudoBoard(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .background(Color.White)
    ) {
        val cell = size.width / 15f

        // Draw the board cells
        for (row in 0 until 15) {
            for (col in 0 until 15) {

                val color = when {
                    row < 6 && col < 6 -> red
                    row < 6 && col >= 9 -> green
                    row >= 9 && col < 6 -> blue
                    row >= 9 && col >= 9 -> yellow

                    row in 6..8 || col in 6..8 -> boardWhite

                    else -> Color(0xFFF5F5F5)
                }

                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        col * cell,
                        row * cell
                    ),
                    size = androidx.compose.ui.geometry.Size(cell, cell)
                )

                drawRect(
                    color = boardLine,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        col * cell,
                        row * cell
                    ),
                    size = androidx.compose.ui.geometry.Size(cell, cell),
                    style = Stroke(width = 1f)
                )
            }
        }

        // Home areas
        fun home(x: Int, y: Int, color: Color) {
            val inset = cell * 1.1f

            drawRect(
                color = boardWhite,
                topLeft = androidx.compose.ui.geometry.Offset(
                    x * cell + inset,
                    y * cell + inset
                ),
                size = androidx.compose.ui.geometry.Size(
                    cell * 3.8f,
                    cell * 3.8f
                )
            )

            val positions = listOf(
                2.0f to 2.0f,
                4.0f to 2.0f,
                2.0f to 4.0f,
                4.0f to 4.0f
            )

            positions.forEach { (px, py) ->
                drawCircle(
                    color = color,
                    radius = cell * 0.55f,
                    center = androidx.compose.ui.geometry.Offset(
                        (x + px) * cell,
                        (y + py) * cell
                    )
                )
                drawCircle(
                    color = boardWhite,
                    radius = cell * 0.23f,
                    center = androidx.compose.ui.geometry.Offset(
                        (x + px) * cell,
                        (y + py) * cell
                    )
                )
            }
        }

        home(0, 0, red)
        home(9, 0, green)
        home(0, 9, blue)
        home(9, 9, yellow)

        // Colored entry lanes
        for (i in 1..5) {
            drawRect(red, androidx.compose.ui.geometry.Offset(7 * cell, i * cell),
                androidx.compose.ui.geometry.Size(cell, cell))

            drawRect(green, androidx.compose.ui.geometry.Offset((13 - i) * cell, 7 * cell),
                androidx.compose.ui.geometry.Size(cell, cell))

            drawRect(yellow, androidx.compose.ui.geometry.Offset(7 * cell, (13 - i) * cell),
                androidx.compose.ui.geometry.Size(cell, cell))

            drawRect(blue, androidx.compose.ui.geometry.Offset(i * cell, 7 * cell),
                androidx.compose.ui.geometry.Size(cell, cell))
        }

        // Center finish area
        val center = androidx.compose.ui.geometry.Offset(
            7.5f * cell,
            7.5f * cell
        )

        drawCircle(
            color = Color(0xFF222222),
            radius = cell * 1.35f,
            center = center
        )

        drawCircle(
            color = yellow,
            radius = cell * 1.0f,
            center = center
        )

        drawCircle(
            color = Color.White,
            radius = cell * 0.55f,
            center = center
        )
    }
}
