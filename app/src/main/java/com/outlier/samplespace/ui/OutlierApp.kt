package com.outlier.samplespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outlier.samplespace.game.AssignedRole
import com.outlier.samplespace.game.GamePhase
import com.outlier.samplespace.game.GameState
import com.outlier.samplespace.game.Role

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
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF0B1020))
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

            GamePhase.CLUE_ROUND -> ClueRoundScreen(uiState.game, onStartVoting = viewModel::startVoting)
            GamePhase.VOTING -> VotingScreen(
                state = uiState.game,
                currentVoter = viewModel.currentVoter(),
                onVote = viewModel::castVote
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
    val state by viewModel.uiState.collectAsState()
    val setup = state.setup
    val maxUndercover = (setup.playerCount - setup.mrWhiteCount - 2).coerceAtLeast(1)
    val maxMrWhite = (setup.playerCount - setup.undercoverCount - 2).coerceAtLeast(0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Sample Space",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                text = "Find the Outliers",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFBFDBFE)
            )
        }

        item {
            SetupCard(title = "Players (${setup.playerCount})") {
                Slider(
                    value = setup.playerCount.toFloat(),
                    onValueChange = { viewModel.updatePlayerCount(it.toInt()) },
                    valueRange = 4f..15f,
                    steps = 10
                )
            }
        }

        item {
            SetupCard(title = "Undercovers (${setup.undercoverCount})") {
                Slider(
                    value = setup.undercoverCount.toFloat(),
                    onValueChange = { viewModel.updateUndercoverCount(it.toInt()) },
                    valueRange = 1f..maxUndercover.toFloat(),
                    steps = (maxUndercover - 1).coerceAtLeast(0)
                )
            }
        }

        item {
            SetupCard(title = "Mr White (${setup.mrWhiteCount})") {
                Slider(
                    value = setup.mrWhiteCount.toFloat(),
                    onValueChange = { viewModel.updateMrWhiteCount(it.toInt()) },
                    valueRange = 0f..maxMrWhite.toFloat(),
                    steps = (maxMrWhite - 1).coerceAtLeast(0)
                )
            }
        }

        item {
            SetupCard(title = "Category") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.categories.forEach { category ->
                        AssistChip(
                            onClick = { viewModel.updateCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Selected: ${setup.selectedCategory}", color = Color(0xFFCBD5E1))
            }
        }

        item {
            SetupCard(title = "Player Names") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    setup.playerNames.forEachIndexed { index, name ->
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.updatePlayerName(index, it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Player ${index + 1}") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                        )
                    }
                }
            }
        }

        setup.errorMessage?.let { error ->
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D))) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White
                    )
                }
            }
        }

        item {
            Button(
                onClick = { viewModel.startGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Start Game")
            }
        }
    }
}

@Composable
private fun SetupCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = title, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
            content()
        }
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
    val role = assignment?.role ?: Role.CIVILIAN
    val word = assignment?.word
    val isLast = state.revealIndex >= state.assignments.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pass the device to", color = Color(0xFFBFDBFE))
        Text(player, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!revealShown) {
                    Text("Tap reveal when ready", color = Color(0xFFE2E8F0))
                    Button(onClick = onReveal) { Text("Reveal") }
                } else {
                    Text("Role: ${roleLabel(role)}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        if (role == Role.MR_WHITE) "No word for you" else "Your word: $word",
                        color = Color(0xFFBFDBFE)
                    )
                    Button(onClick = onContinue) {
                        Text(if (isLast) "Start Round" else "Next Player")
                    }
                }
            }
        }
    }
}

private fun roleLabel(role: Role): String = when (role) {
    Role.CIVILIAN -> "Civilian"
    Role.UNDERCOVER -> "Undercover"
    Role.MR_WHITE -> "Mr White"
}

@Composable
private fun ClueRoundScreen(state: GameState, onStartVoting: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Round ${state.roundNumber}",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            Text(
                "Alive players: ${state.alivePlayers.joinToString(", ")}",
                color = Color(0xFFBFDBFE)
            )
        }
        state.lastEliminatedPlayer?.let {
            item {
                Text("Last eliminated: $it", color = Color(0xFFFECACA))
            }
        }
        if (state.tiedPlayers.isNotEmpty()) {
            item {
                Text("Previous vote tied: ${state.tiedPlayers.joinToString(", ")}", color = Color(0xFFFDE68A))
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF))) {
                Text(
                    text = "Take turns giving one clue. When everyone has spoken, start voting.",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFE2E8F0)
                )
            }
        }
        item {
            Button(onClick = onStartVoting, modifier = Modifier.fillMaxWidth()) {
                Text("Start Voting")
            }
        }
    }
}

@Composable
private fun VotingScreen(state: GameState, currentVoter: String?, onVote: (String) -> Unit) {
    val voter = currentVoter ?: ""

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Voting", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text("Current voter: $voter", color = Color(0xFFBFDBFE))
        }

        itemsIndexed(state.alivePlayers) { _, target ->
            val alreadyVoted = state.votes.containsKey(voter)
            TextButton(
                onClick = { if (!alreadyVoted) onVote(target) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vote $target")
            }
        }

        item {
            Text(
                text = "Votes cast: ${state.votes.size}/${state.voteOrder.size}",
                color = Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
private fun MrWhiteGuessScreen(guess: String, onGuessChange: (String) -> Unit, onSubmitGuess: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Mr White Guess", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Guess the civilians' word to steal the win.", color = Color(0xFFBFDBFE))
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
private fun ResultScreen(state: GameState, winnerLabel: String, roleSummary: String, onPlayAgain: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Game Over", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(winnerLabel, style = MaterialTheme.typography.titleLarge, color = Color(0xFFBFDBFE))
        Spacer(modifier = Modifier.height(8.dp))
        Text(roleSummary, color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Rounds played: ${state.roundNumber}", color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
            Text("Play Again")
        }
    }
}
