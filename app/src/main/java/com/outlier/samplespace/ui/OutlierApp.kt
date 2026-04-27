package com.outlier.samplespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
                        MaterialTheme.colorScheme.surfaceContainerLow,
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
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.23f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
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
                civilianScore = uiState.civilianScore,
                outlierScore = uiState.outlierScore,
                onPlayAgain = viewModel::resetToSetup
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
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
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Case HQ",
                subtitle = "Find the Outlier",
                caption = "Build your squad, hide identities, and crack the case.",
                icon = Icons.Filled.ManageSearch,
                accent = MaterialTheme.colorScheme.primary
            )
        }

        item {
            PanelCard(
                title = "Case Setup",
                icon = Icons.Filled.Policy,
                accent = MaterialTheme.colorScheme.tertiary
            ) {
                CounterRow(
                    label = "Detectives",
                    value = setup.playerCount,
                    min = 4,
                    max = 15,
                    onDecrease = { viewModel.updatePlayerCount(setup.playerCount - 1) },
                    onIncrease = { viewModel.updatePlayerCount(setup.playerCount + 1) }
                )
                CounterRow(
                    label = "Undercover",
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
                    text = "Limits: undercover ≤ $maxUndercover, Mr White ≤ $maxMrWhite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                setup.helperMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item {
            PanelCard(
                title = "Clue Board Category",
                icon = Icons.Filled.TipsAndUpdates,
                accent = MaterialTheme.colorScheme.secondary
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.categories.forEach { category ->
                        val selected = setup.selectedCategory == category
                        AssistChip(
                            onClick = { viewModel.updateCategory(category) },
                            label = { Text(category) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                                labelColor = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }
                            )
                        )
                    }
                }
                Text(
                    text = "Selected board: ${setup.selectedCategory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            PanelCard(
                title = "Game Options",
                icon = Icons.Filled.TipsAndUpdates,
                accent = MaterialTheme.colorScheme.secondary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Count Clue Parts",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Track word part count during discussion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = setup.countPartsEnabled,
                        onCheckedChange = { viewModel.toggleCountParts() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        item {
            PanelCard(
                title = "Suspect Roster",
                icon = Icons.Filled.Groups,
                accent = MaterialTheme.colorScheme.primary
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    setup.playerNames.forEachIndexed { index, name ->
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.updatePlayerName(index, it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Name") },
                            placeholder = { Text("Player ${index + 1}") }
                        )
                    }
                    Text(
                        text = "Leave blank to keep default player labels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        setup.errorMessage?.let { message ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
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
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.ManageSearch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Secret Case")
            }
        }
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    caption: String,
    icon: ImageVector,
    accent: Color
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.24f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PanelCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.tertiary
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
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
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
        HeroCard(
            title = "Confidential Reveal",
            subtitle = "Eyes only for one detective",
            caption = "Keep this hidden and pass the device when done.",
            icon = Icons.Filled.PersonSearch,
            accent = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Detective ${state.revealIndex + 1} of ${state.revealOrder.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        text = "Tap reveal and protect your identity.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onReveal,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
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
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Button(
                        onClick = onContinue,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Text(if (isLast) "Begin Investigation" else "Hide and Pass")
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(
                title = "Investigation Round ${state.roundNumber}",
                subtitle = "Clue Exchange",
                caption = "Each surviving detective gives one clue.",
                icon = Icons.Filled.TipsAndUpdates,
                accent = MaterialTheme.colorScheme.tertiary
            )
        }

        if (state.countParts) {
            item {
                PanelCard(
                    title = "Clue Parts Counter",
                    icon = Icons.Filled.TipsAndUpdates,
                    accent = MaterialTheme.colorScheme.primary
                ) {
                    val partCount = state.alivePlayers.size
                    Text(
                        text = "Total clues given: $partCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Each player should give $partCount word parts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            PanelCard(
                title = "Active Detectives",
                icon = Icons.Filled.Groups,
                accent = MaterialTheme.colorScheme.secondary
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.alivePlayers.forEach { player ->
                        AssistChip(
                            onClick = { },
                            label = { Text(player) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonSearch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Move to Elimination")
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeroCard(
            title = "Suspect Vote",
            subtitle = "Who gets eliminated?",
            caption = "Pick one suspect and confirm the arrest.",
            icon = Icons.Filled.PersonSearch,
            accent = MaterialTheme.colorScheme.error
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
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.errorContainer
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
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Suspect",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onError
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
            Text("Confirm Elimination", color = MaterialTheme.colorScheme.onError)
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
        Role.UNDERCOVER -> "Undercover"
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Policy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Case Update",
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
                CountLine("Undercover left", announcement?.undercoversLeft ?: 0)
                CountLine("Mr White left", announcement?.mrWhitesLeft ?: 0)

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary
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
            title = "Mr White Final Guess",
            subtitle = "One shot to flip the case",
            caption = "Guess the civilians' clue word exactly.",
            icon = Icons.Filled.TipsAndUpdates,
            accent = MaterialTheme.colorScheme.tertiary
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
        Button(
            onClick = onSubmitGuess,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
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
    civilianScore: Int,
    outlierScore: Int,
    onPlayAgain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(
                title = "Case Closed",
                subtitle = winnerLabel,
                caption = "Review the file and prep the next mystery.",
                icon = Icons.Filled.Policy,
                accent = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            PanelCard(
                title = "Case Summary",
                icon = Icons.Filled.TipsAndUpdates,
                accent = MaterialTheme.colorScheme.tertiary
            ) {
                Text(roleSummary)
                Text("Rounds played: ${state.roundNumber}")
            }
        }

        item {
            PanelCard(
                title = "Score Board",
                icon = Icons.Filled.Policy,
                accent = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Civilians",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$civilianScore",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Outliers",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$outlierScore",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        item {
            PanelCard(
                title = "Full Role Reveal",
                icon = Icons.Filled.Groups,
                accent = MaterialTheme.colorScheme.primary
            ) {
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Back to Setup")
            }
        }
    }
}
