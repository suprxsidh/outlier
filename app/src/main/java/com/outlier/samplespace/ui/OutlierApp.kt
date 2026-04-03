package com.outlier.samplespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outlier.samplespace.game.AssignedRole
import com.outlier.samplespace.game.EliminationAnnouncement
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
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            Color.Transparent
                        )
                    )
                )
        )

        when (uiState.game.phase) {
            GamePhase.SETUP -> SetupScreen(viewModel)
            GamePhase.ROLE_REVEAL -> RevealScreen(
                state = uiState.game,
                playerName = viewModel.currentRevealPlayer(),
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

            GamePhase.POST_ELIMINATION_ANNOUNCEMENT -> AnnouncementScreen(
                announcement = viewModel.eliminationAnnouncement(),
                onContinue = viewModel::continueAfterAnnouncement
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
            HeroCard(
                title = "Sample Space",
                subtitle = "Find the Outliers"
            )
        }

        item {
            PanelCard(title = "Match Setup") {
                CounterRow(
                    label = "Players",
                    value = setup.playerCount,
                    min = 4,
                    max = 15,
                    onDecrease = { viewModel.updatePlayerCount(setup.playerCount - 1) },
                    onIncrease = { viewModel.updatePlayerCount(setup.playerCount + 1) }
                )
                CounterRow(
                    label = "Imposters",
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
                    text = "Limits: imposters <= $maxUndercover, Mr White <= $maxMrWhite",
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
            PanelCard(title = "Word Category") {
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
            PanelCard(title = "Player Names") {
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Secret Reveal")
            }
        }
    }
}

@Composable
private fun HeroCard(title: String, subtitle: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Pass-and-play social deduction for local groups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun PanelCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
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
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperButton(text = "-", enabled = value > min, onClick = onDecrease)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(12.dp))
            StepperButton(text = "+", enabled = value < max, onClick = onIncrease)
        }
    }
}

@Composable
private fun StepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = Modifier.size(38.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun RevealScreen(
    state: GameState,
    playerName: String?,
    assignment: AssignedRole?,
    revealShown: Boolean,
    onReveal: () -> Unit,
    onContinue: () -> Unit
) {
    val isLast = state.revealIndex >= state.revealOrder.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Secret Reveal",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Player ${state.revealIndex + 1} of ${state.revealOrder.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pass the device to",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = playerName ?: "",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.ExtraBold
                )

                if (!revealShown) {
                    Text(
                        text = "Tap reveal and keep this secret.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onReveal, shape = RoundedCornerShape(14.dp)) {
                        Text("Reveal Secret")
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
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onContinue, shape = RoundedCornerShape(14.dp)) {
                        Text(if (isLast) "Start Round" else "Hide and Pass")
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ClueRoundScreen(state: GameState, onStartElimination: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(
                title = "Round ${state.roundNumber}",
                subtitle = "Clue Phase"
            )
        }

        item {
            PanelCard(title = "Alive Players") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.alivePlayers.forEach { player ->
                        AssistChip(onClick = { }, label = { Text(player) })
                    }
                }
                state.lastEliminatedPlayer?.let {
                    Text(
                        text = "Last eliminated: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = onStartElimination,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Proceed to Elimination")
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
        HeroCard(
            title = "Elimination",
            subtitle = "Select one player and confirm"
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
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTarget(player) }
                        .border(
                            width = if (selected) 2.dp else 0.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(player, style = MaterialTheme.typography.titleMedium)
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Selected",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
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
private fun AnnouncementScreen(
    announcement: EliminationAnnouncement?,
    onContinue: () -> Unit
) {
    val roleLabel = when (announcement?.eliminatedRole) {
        Role.CIVILIAN -> "Civilian"
        Role.UNDERCOVER -> "Imposter"
        Role.MR_WHITE -> "Mr White"
        null -> "Unknown"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Elimination Announcement",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Eliminated role: $roleLabel",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                CountLine("Civilians left", announcement?.civiliansLeft ?: 0)
                CountLine("Imposters left", announcement?.undercoversLeft ?: 0)
                CountLine("Mr Whites left", announcement?.mrWhitesLeft ?: 0)

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun CountLine(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
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
        HeroCard(
            title = "Final Guess",
            subtitle = "Guess the civilians' clue word"
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
            HeroCard(
                title = "Game Over",
                subtitle = winnerLabel
            )
        }

        item {
            PanelCard(title = "Match Summary") {
                Text(roleSummary)
                Text("Rounds played: ${state.roundNumber}")
            }
        }

        item {
            PanelCard(title = "Role Reveal") {
                roleRevealLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Back to Setup")
            }
        }
    }
}
