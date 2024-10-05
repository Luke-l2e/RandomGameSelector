package de.hhn.randomgameselector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.hhn.randomgameselector.data.Colors
import de.hhn.randomgameselector.ui.theme.RandomGameSelectorTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    //TODO: Layout: Strict LazyColumn or first item is slot machine
    private val games = listOf("Quickmatch", "Stormleague", "Battlegrounds", "ARAM")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomGameSelectorTheme {
                var visible by remember { mutableStateOf(false) }
                var currentGame by remember { mutableStateOf(games.random()) }
                var isRolling by remember { mutableStateOf(false) }
                val history = remember { mutableStateListOf<String>() }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Random Game Selector") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                visible = true
                                if (!isRolling) {
                                    isRolling = true
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Roll")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(!visible) {
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .fillMaxWidth(0.93f)
                                    .clip(shape = RoundedCornerShape(30))
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Text(
                                    text = "What will it be?",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth(),
                            content = {
                                var itemCount = 0
                                history.takeLast(10).reversed()
                                    .forEach { game ->
                                        itemCount++
                                        if (itemCount == 1) {
                                            createHistoryItem(true, game)
                                        } else {
                                            createHistoryItem(itemText = game)
                                        }
                                    }
                                if (visible) {
                                    while (itemCount < 10) {
                                        itemCount++
                                        createHistoryItem()
                                    }
                                }
                            },
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        AnimatedVisibility(visible, modifier = Modifier.fillMaxHeight()) {
                            SlotMachine(
                                currentGame = currentGame,
                                games = games,
                                isRolling = isRolling,
                                onRollComplete = { selectedGame ->
                                    currentGame = selectedGame
                                    isRolling = false
                                    history.add(selectedGame)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Create an item with a Text
     * @param itemText The text to be shown
     */
    private fun LazyListScope.createHistoryItem(
        isFirstItem: Boolean = false,
        itemText: String = " "
    ) {
        item {
            val backgroundColor =
                if (isFirstItem) Colors.item else MaterialTheme.colorScheme.background
            val textColor = if (isFirstItem) Colors.font else MaterialTheme.colorScheme.onBackground
            Text(
                text = itemText,
                style = MaterialTheme.typography.displaySmall,
                color = textColor, textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .clip(shape = RoundedCornerShape(30))
                    .background(backgroundColor)
            )
        }
    }

    @Composable
    fun SlotMachine(
        currentGame: String,
        games: List<String>,
        isRolling: Boolean,
        onRollComplete: (String) -> Unit
    ) {
        var randomGame by remember { mutableStateOf(currentGame) }

        // Trigger rolling effect when isRolling is true
        LaunchedEffect(isRolling) {
            if (isRolling) {
                val rollDuration = 2000L // Total roll time
                val interval = 100L // Interval between each game change
                val endTime = System.currentTimeMillis() + rollDuration
                while (System.currentTimeMillis() < endTime) {
                    randomGame = games[Random.nextInt(games.size)]
                    delay(interval) // Change the game at each interval
                }
                onRollComplete(randomGame) // Return the final selected game
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = randomGame,
                style = MaterialTheme.typography.displayMedium, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .clip(shape = RoundedCornerShape(30))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
