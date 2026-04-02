package com.outlier.samplespace.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outlier.samplespace.game.AssignedRole
import com.outlier.samplespace.game.GamePhase
import com.outlier.samplespace.game.GameState

@Composable
fun OutlierApp(viewModel: OutlierViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.transientMessage) {
        val message = uiState.transientMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearTransientMessage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            )
    ) {
        when (uiState.game.phase) {
            GamePhase.SETUP -> SetupScreen(viewModel)
            GamePhase.ROLE_REVEAL -> RevealScreen(
                state = uiState.game,
                assignment = viewModel.revealAssignment(),
                revealShown = uiState.revealShown,
                onReveal = viewModel::revealWord,
                onContinue = viewModel::continueAfterReveal
            )

            GamePhase.CLUE_ROUND -> ClueRoundScreen(
                state = uiState.game,
                onStartElimination = viewModel::startElimination
            )

            GamePhase.ELIMINATION -> EliminationScreen(
                state = uiState.game,
                selectedTarget = viewModel.eliminationTarget(),
                onSelectTarget = viewModel::selectEliminationTarget,
                onConfirmElimination = viewModel::confirmElimination
            )

            GamePhase.MR_WHITE_GUESS -> MrWhiteGuessScreen(
                guess = uiState.guessInput,
                onGuessChange = viewModel::updateGuess,
                onSubmitGuess = viewModel::submitGuess
            )

            GamePhase.GAME_OVER -> ResultScreen(
                state = uiState.game,
                winnerLabel = viewModel.winnerLabel(),
                roleSummary = viewModel.roleCountsSummary(),
                roleRevealLines = viewModel.roleRevealLinesForResult(),
                onPlayAgain = viewModel::resetToSetup
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(snackbarData = data)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SetupScreen(viewModel: OutlierViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val setup = uiState.setup
    val maxUndercover = viewModel.maxUndercoverForCurrentSetup()
    val maxMrWhite = viewModel.maxMrWhiteForCurrentSetup()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Sample Space",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = "Find the Outliers",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            SetupCard("Game Setup") {
                CounterRow(
                    label = "Players",
                    value = setup.playerCount,
                    min = 4,
                    max = 15,
                    onDecrease = { viewModel.updatePlayerCount(setup.playerCount - 1) },
                    onIncrease = { viewModel.updatePlayerCount(setup.playerCount + 1) }
                )
                CounterRow(
                    label = "Undercovers",
                    value = setup.undercoverCount,
                    min = 1,
                    max = maxUndercover,
                    onDecrease = { viewModel.updateUndercoverCount(setup.undercoverCount - 1) },
                    onIncrease = { viewModel.updateUndercoverCount(setup.undercoverCount + 1) }
                )
                CounterRow(
                    label = "Mr White",
                    value = setup.mrWhiteCount,
                    min = 0,
                    max = maxMrWhite,
                    onDecrease = { viewModel.updateMrWhiteCount(setup.mrWhiteCount - 1) },
                    onIncrease = { viewModel.updateMrWhiteCount(setup.mrWhiteCount + 1) }
                )

                Text(
                    text = "Limits: Undercovers <= $maxUndercover, Mr White <= $maxMrWhite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                setup.helperMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            SetupCard("Category") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.categories.forEach { category ->
                        AssistChip(
                            onClick = { viewModel.updateCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
                Text(
                    text = "Selected: ${setup.selectedCategory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SetupCard("Player Names") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    setup.playerNames.forEachIndexed { index, name ->
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.updatePlayerName(index, it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Player ${index + 1}") }
                        )
                    }
                }
            }
        }

        setup.errorMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = viewModel::startGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}

@Composable
private fun SetupCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun CounterRow(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleButton(
                icon = "-",
                enabled = value > min,
                onClick = onDecrease
            )
            Spacer(modifier = Modifier.width(12.dp))
            AnimatedContent(
                targetState = value,
                label = "counter"
            ) { count ->
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            CircleButton(
                icon = "+",
                enabled = value < max,
                onClick = onIncrease
            )
        }
    }
}

@Composable
private fun CircleButton(icon: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(icon, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun RevealScreen(
    state: GameState,
    assignment: AssignedRole?,
    revealShown: Boolean,
    onReveal: () -> Unit,
    onContinue: () -> Unit
) {
    val player = assignment?.playerName ?: ""
    val isLast = state.revealIndex >= state.assignments.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pass the device to",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = player,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!revealShown) {
                    Text(
                        text = "Tap reveal when ready. Keep this private.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onReveal) {
                        Text("Reveal")
                    }
                } else {
                    val message = if (assignment?.word == null) {
                        "You have no clue word this round."
                    } else {
                        "Your clue word is: ${assignment.word}"
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onContinue) {
                        Text(if (isLast) "Start Round" else "Next Player")
                    }
                }
            }
        }
    }
}

@Composable
private fun ClueRoundScreen(state: GameState, onStartElimination: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Round ${state.roundNumber}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Alive players",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = state.alivePlayers.joinToString(", "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.lastEliminatedPlayer?.let {
                        Text(
                            text = "Last eliminated: $it",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = onStartElimination,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Open Elimination")
            }
        }
    }
}

@Composable
private fun EliminationScreen(
    state: GameState,
    selectedTarget: String?,
    onSelectTarget: (String) -> Unit,
    onConfirmElimination: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Elimination",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose one player and tap Eliminate.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.alivePlayers) { player ->
                val selected = selectedTarget == player
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTarget(player) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(player, style = MaterialTheme.typography.titleMedium)
                        if (selected) {
                            Text(
                                text = "Selected",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onConfirmElimination,
            enabled = selectedTarget != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Eliminate", color = MaterialTheme.colorScheme.onError)
        }
    }
}

@Composable
private fun MrWhiteGuessScreen(
    guess: String,
    onGuessChange: (String) -> Unit,
    onSubmitGuess: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Final Guess",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Guess the main clue word.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = guess,
            onValueChange = onGuessChange,
            label = { Text("Your guess") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onSubmitGuess, modifier = Modifier.fillMaxWidth()) {
            Text("Submit Guess")
        }
    }
}

@Composable
private fun ResultScreen(
    state: GameState,
    winnerLabel: String,
    roleSummary: String,
    roleRevealLines: List<String>,
    onPlayAgain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = winnerLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            SetupCard("Match Summary") {
                Text(roleSummary)
                Text("Rounds played: ${state.roundNumber}")
            }
        }

        item {
            SetupCard("Role Reveal") {
                roleRevealLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
                Text("Play Again")
            }
        }
    }
}
