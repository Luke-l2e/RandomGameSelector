package de.hhn.randomgameselector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.hhn.randomgameselector.ui.theme.RandomGameSelectorTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val games = listOf("Quickmatch", "Stormleague", "Battlegrounds", "ARAM", "Chess")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomGameSelectorTheme {
                var visible by rememberSaveable { mutableStateOf(false) }
                var currentGame by rememberSaveable { mutableStateOf("") }
                var isRolling by rememberSaveable { mutableStateOf(false) }
                val history = rememberMutableStateListOf<String>()

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
                                if (isRolling) {
                                    return@FloatingActionButton
                                }
                                visible = true
                                if (currentGame.isNotEmpty()) {
                                    history.add(currentGame)
                                }
                                isRolling = true
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
                            .padding(vertical = 5.dp)
                            .background(MaterialTheme.colorScheme.background),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(!visible, exit = ExitTransition.None) {
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
                                item {
                                    AnimatedVisibility(visible) {
                                        SlotMachine(
                                            currentGame = currentGame,
                                            games = games,
                                            isRolling = isRolling,
                                            onRollComplete = { selectedGame ->
                                                currentGame = selectedGame
                                                isRolling = false
                                            }
                                        )
                                    }
                                }
                                createHistoryItems(history)
                            },
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                    }
                }
            }
        }
    }


    /**
     * Create the history list
     * @param history The history list to be used
     */
    private fun LazyListScope.createHistoryItems(
        history: SnapshotStateList<String>
    ) {
        var itemCount = 0
        history.reversed().forEach { game ->
            itemCount++
            createHistoryItem(game)
        }
    }

    /**
     * Create an item with a Text
     * @param itemText The text to be shown
     */
    private fun LazyListScope.createHistoryItem(
        itemText: String = " "
    ) {
        item {
            val backgroundColor = MaterialTheme.colorScheme.background
            val textColor = MaterialTheme.colorScheme.onBackground
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

    /**
     * Create a text showing an element out of a list and rolling between the elements for a random duration in order starting with a random element
     * @param currentGame The currently shown/chosen element
     * @param games The list with the elements to be rolled
     * @param isRolling True if the text is rolling
     * @param onRollComplete Is executed when the rolling is over and a game was chosen
     */
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
                val rollDuration = Random.nextInt(1500, 6200) // Total roll time
                var interval = 50L // Interval between each game change
                val endTime = System.currentTimeMillis() + rollDuration
                var gameNumber = Random.nextInt(0, games.size)
                while (System.currentTimeMillis() < endTime) {
                    interval += 10
                    randomGame = games[gameNumber]
                    gameNumber = (gameNumber + 1) % games.size
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
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .clip(shape = RoundedCornerShape(30))
                    .background(if (isRolling) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary)
            )
        }
    }

    /**
     * Creates a rememberSaveAble list of a type T with or without elements
     * @param elements The elements of the list
     * @return SnapshotStateList<T>
     */
    @Composable
    fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
        return rememberSaveable(saver = snapshotStateListSaver()) {
            elements.toList().toMutableStateList()
        }
    }

    /**
     * Returns a Saver for a List
     */
    private fun <T : Any> snapshotStateListSaver() = listSaver<SnapshotStateList<T>, T>(
        save = { stateList -> stateList.toList() },
        restore = { it.toMutableStateList() },
    )
}
