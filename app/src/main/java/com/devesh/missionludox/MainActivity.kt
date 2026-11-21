
package com.devesh.missionludox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

data class Player(
    val name: String,
    val color: Color
)

val players = listOf(
    Player("Blue", Color(0xFF2196F3)),
    Player("Red", Color(0xFFE53935)),
    Player("Green", Color(0xFF43A047)),
    Player("Yellow", Color(0xFFFFD600))
)

@Composable
fun MissionLudoApp() {
    var playerCount by remember { mutableIntStateOf(2) }
    var gameStarted by remember { mutableStateOf(false) }

    if (!gameStarted) {
        PlayerSelectionScreen(
            playerCount = playerCount,
            onPlayerCountChange = { playerCount = it },
            onStart = { gameStarted = true }
        )
    } else {
        LocalGameScreen(
            playerCount = playerCount,
            onBack = { gameStarted = false }
        )
    }
}

@Composable
fun PlayerSelectionScreen(
    playerCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151525))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "MISSION LUDO X",
            color = Color.White,
            fontSize = 30.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "LOCAL MULTIPLAYER",
            color = Color.LightGray,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(45.dp))

        Text(
            "SELECT PLAYERS",
            color = Color.White,
            fontSize = 21.sp
        )

        Spacer(Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            (2..4).forEach { count ->
                Button(
                    onClick = { onPlayerCountChange(count) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (playerCount == count)
                            Color(0xFFFF3155)
                        else Color(0xFF303044)
                    )
                ) {
                    Text("$count")
                }
            }
        }

        Spacer(Modifier.height(35.dp))

        Text(
            "PLAYER TURN ORDER",
            color = Color.White,
            fontSize = 17.sp
        )

        Spacer(Modifier.height(15.dp))

        players.take(playerCount).forEachIndexed { index, player ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(
                        Color(0xFF252538),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${index + 1}.",
                    color = Color.White,
                    fontSize = 18.sp
                )

                Spacer(Modifier.width(14.dp))

                Box(
                    Modifier
                        .size(18.dp)
                        .background(player.color, RoundedCornerShape(50))
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    player.name,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(Modifier.height(35.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF3155)
            )
        ) {
            Text("START GAME", fontSize = 18.sp)
        }

        Spacer(Modifier.height(25.dp))

        Text(
            "CREATED BY DEVESH",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LocalGameScreen(
    playerCount: Int,
    onBack: () -> Unit
) {
    val activePlayers = players.take(playerCount)
    var currentTurn by remember { mutableIntStateOf(0) }
    var dice by remember { mutableIntStateOf(0) }

    val currentPlayer = activePlayers[currentTurn]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151525))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "MISSION LUDO X",
            color = Color.White,
            fontSize = 27.sp
        )

        Spacer(Modifier.height(35.dp))

        Text(
            "CURRENT PLAYER",
            color = Color.LightGray,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            currentPlayer.name.uppercase(),
            color = currentPlayer.color,
            fontSize = 32.sp
        )

        Spacer(Modifier.height(30.dp))

        Text(
            "🎲 $dice",
            color = Color.White,
            fontSize = 55.sp
        )

        Spacer(Modifier.height(25.dp))

        Button(
            onClick = {
                dice = (1..6).random()
            }
        ) {
            Text("ROLL DICE")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                currentTurn = (currentTurn + 1) % playerCount
                dice = 0
            },
            enabled = dice != 0
        ) {
            Text("NEXT PLAYER")
        }

        Spacer(Modifier.height(35.dp))

        Text(
            "TURN ORDER",
            color = Color.LightGray
        )

        Text(
            activePlayers.joinToString(" → ") { it.name },
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(30.dp))

        TextButton(onClick = onBack) {
            Text("BACK TO PLAYER SELECTION")
        }
    }
}
