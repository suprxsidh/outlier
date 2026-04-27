package com.outlier.samplespace.ui

import androidx.lifecycle.ViewModel
import com.outlier.samplespace.game.AssignedRole
import com.outlier.samplespace.game.EliminationAnnouncement
import com.outlier.samplespace.game.GameConfig
import com.outlier.samplespace.game.GamePhase
import com.outlier.samplespace.game.GameSession
import com.outlier.samplespace.game.GameState
import com.outlier.samplespace.game.Role
import com.outlier.samplespace.game.Winner
import com.outlier.samplespace.game.WordBank
import com.outlier.samplespace.game.WordPair
import com.outlier.samplespace.game.maxMrWhiteCount
import com.outlier.samplespace.game.maxUndercoverCount
import com.outlier.samplespace.game.validateConfig
import com.outlier.samplespace.game.validatePlayerNames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val DEFAULT_PLAYER_COUNT = 7
const val DEFAULT_UNDERCOVER_COUNT = 2
const val DEFAULT_MR_WHITE_COUNT = 1

data class SetupState(
    val playerCount: Int = DEFAULT_PLAYER_COUNT,
    val undercoverCount: Int = DEFAULT_UNDERCOVER_COUNT,
    val mrWhiteCount: Int = DEFAULT_MR_WHITE_COUNT,
    val playerNames: List<String> = defaultNames(DEFAULT_PLAYER_COUNT),
    val selectedCategory: String = "All",
    val countPartsEnabled: Boolean = false,
    val errorMessage: String? = null,
    val helperMessage: String? = null
)

data class OutlierUiState(
    val setup: SetupState = SetupState(),
    val game: GameState = GameState(),
    val revealShown: Boolean = false,
    val guessInput: String = "",
    val transientMessage: String? = null,
    val civilianScore: Int = 0,
    val outlierScore: Int = 0
)

private fun defaultNames(count: Int): List<String> =
    List(count) { "" }

private fun fallbackName(index: Int): String = "Player ${index + 1}"

class OutlierViewModel : ViewModel() {
    private val session = GameSession()
    private val _uiState = MutableStateFlow(OutlierUiState())
    val uiState: StateFlow<OutlierUiState> = _uiState.asStateFlow()

    val categories: List<String> = listOf("All") + WordBank.allPairs.map { it.category }.distinct().sorted()

    fun maxUndercoverForCurrentSetup(): Int {
        val setup = _uiState.value.setup
        return maxUndercoverFor(setup.playerCount, setup.mrWhiteCount)
    }

    fun maxMrWhiteForCurrentSetup(): Int {
        val setup = _uiState.value.setup
        return maxMrWhiteFor(setup.playerCount, setup.undercoverCount)
    }

    fun updatePlayerCount(count: Int) {
        val current = _uiState.value.setup
        val clampedPlayers = count.coerceIn(4, 15)
        val updatedNames = current.playerNames.take(clampedPlayers).toMutableList().apply {
            while (size < clampedPlayers) {
                add("")
            }
        }

        var undercovers = current.undercoverCount.coerceIn(1, maxUndercoverCount(clampedPlayers))
        var mrWhite = current.mrWhiteCount.coerceIn(0, maxMrWhiteCount(clampedPlayers))

        val outlierCap = clampedPlayers - 2
        if (undercovers + mrWhite > outlierCap) {
            val overflow = undercovers + mrWhite - outlierCap
            mrWhite = (mrWhite - overflow).coerceAtLeast(0)
            if (undercovers + mrWhite > outlierCap) {
                undercovers = (outlierCap - mrWhite).coerceAtLeast(1)
            }
        }

        undercovers = undercovers.coerceIn(1, maxUndercoverFor(clampedPlayers, mrWhite))
        mrWhite = mrWhite.coerceIn(0, maxMrWhiteFor(clampedPlayers, undercovers))

        val helper = if (undercovers != current.undercoverCount || mrWhite != current.mrWhiteCount) {
            "Role counts adjusted to stay within limits."
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                playerCount = clampedPlayers,
                undercoverCount = undercovers,
                mrWhiteCount = mrWhite,
                playerNames = updatedNames,
                errorMessage = null,
                helperMessage = helper
            ),
            civilianScore = 0,
            outlierScore = 0
        )
    }

    fun updateUndercoverCount(count: Int) {
        val current = _uiState.value.setup
        val undercovers = count.coerceIn(1, maxUndercoverCount(current.playerCount))
        val mrWhite = current.mrWhiteCount.coerceIn(0, maxMrWhiteFor(current.playerCount, undercovers))

        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                undercoverCount = undercovers,
                mrWhiteCount = mrWhite,
                errorMessage = null,
                helperMessage = if (mrWhite != current.mrWhiteCount) {
                    "Mr White adjusted to keep at least two civilians in play."
                } else {
                    null
                }
            )
        )
    }

    fun updateMrWhiteCount(count: Int) {
        val current = _uiState.value.setup
        val mrWhite = count.coerceIn(0, maxMrWhiteCount(current.playerCount))
        val undercovers = current.undercoverCount.coerceIn(1, maxUndercoverFor(current.playerCount, mrWhite))
        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                undercoverCount = undercovers,
                mrWhiteCount = mrWhite,
                errorMessage = null,
                helperMessage = if (undercovers != current.undercoverCount) {
                    "Undercover count adjusted to keep at least two civilians in play."
                } else {
                    null
                }
            )
        )
    }

    fun updatePlayerName(index: Int, value: String) {
        val current = _uiState.value.setup
        if (index !in current.playerNames.indices) return
        val names = current.playerNames.toMutableList()
        names[index] = value
        _uiState.value = _uiState.value.copy(
            setup = current.copy(playerNames = names, errorMessage = null),
            civilianScore = 0,
            outlierScore = 0
        )
    }

    fun updateCategory(category: String) {
        val current = _uiState.value.setup
        _uiState.value = _uiState.value.copy(
            setup = current.copy(selectedCategory = category, errorMessage = null)
        )
    }

    fun toggleCountParts() {
        val current = _uiState.value.setup
        _uiState.value = _uiState.value.copy(
            setup = current.copy(countPartsEnabled = !current.countPartsEnabled)
        )
    }

    fun startGame() {
        val setup = _uiState.value.setup
        val names = setup.playerNames.mapIndexed { index, name ->
            name.trim().ifBlank { fallbackName(index) }
        }
        val nameError = validatePlayerNames(names)
        if (nameError != null) {
            _uiState.value = _uiState.value.copy(setup = setup.copy(errorMessage = nameError))
            return
        }

        val config = GameConfig(
            playerCount = setup.playerCount,
            undercoverCount = setup.undercoverCount,
            mrWhiteCount = setup.mrWhiteCount
        )
        val configError = validateConfig(config)
        if (configError != null) {
            _uiState.value = _uiState.value.copy(setup = setup.copy(errorMessage = configError))
            return
        }

        val pair = pickPair(setup.selectedCategory)
        val state = session.startGame(names, config, pair, setup.countPartsEnabled)
        _uiState.value = _uiState.value.copy(
            setup = setup.copy(errorMessage = null, helperMessage = null),
            game = state,
            revealShown = false,
            guessInput = "",
            transientMessage = null
        )
    }

    private fun pickPair(category: String): WordPair {
        val pool = if (category == "All") {
            WordBank.allPairs
        } else {
            WordBank.allPairs.filter { it.category == category }
        }
        return pool.random()
    }

    fun revealWord() {
        _uiState.value = _uiState.value.copy(revealShown = true)
    }

    fun continueAfterReveal() {
        val game = _uiState.value.game
        val next = if (game.phase == GamePhase.ROLE_REVEAL) {
            session.advanceReveal(game)
        } else {
            game
        }
        _uiState.value = _uiState.value.copy(game = next, revealShown = false)
    }

    fun startElimination() {
        val current = _uiState.value.game
        val next = runCatching { session.beginElimination(current) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Cannot start elimination")
                return
            }

        _uiState.value = _uiState.value.copy(game = next, transientMessage = null)
    }

    fun selectEliminationTarget(target: String) {
        val current = _uiState.value.game
        val next = runCatching { session.selectEliminationTarget(current, target) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Invalid elimination target")
                return
            }

        _uiState.value = _uiState.value.copy(game = next, transientMessage = null)
    }

    fun confirmElimination() {
        val current = _uiState.value.game
        val next = runCatching { session.confirmElimination(current) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Select a player first")
                return
            }

        _uiState.value = _uiState.value.copy(game = next, transientMessage = null)
    }

    fun continueAfterAnnouncement() {
        val current = _uiState.value.game
        val result = runCatching { session.continueAfterAnnouncement(current) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Cannot continue yet")
                return
            }

        _uiState.value = _uiState.value.copy(
            game = result.state,
            civilianScore = _uiState.value.civilianScore + result.civilianPoints,
            outlierScore = _uiState.value.outlierScore + result.outlierPoints,
            transientMessage = null
        )
    }

    fun updateGuess(value: String) {
        _uiState.value = _uiState.value.copy(guessInput = value)
    }

    fun submitGuess() {
        val guess = _uiState.value.guessInput.trim()
        if (guess.isEmpty()) return
        val result = session.submitMrWhiteGuess(_uiState.value.game, guess)
        _uiState.value = _uiState.value.copy(
            game = result.state,
            guessInput = "",
            civilianScore = _uiState.value.civilianScore + result.civilianPoints,
            outlierScore = _uiState.value.outlierScore + result.outlierPoints,
            transientMessage = null
        )
    }

    fun resetToSetup() {
        val current = _uiState.value
        _uiState.value = OutlierUiState(
            setup = SetupState(
                playerCount = current.setup.playerCount,
                playerNames = current.setup.playerNames,
                countPartsEnabled = current.setup.countPartsEnabled
            ),
            civilianScore = current.civilianScore,
            outlierScore = current.outlierScore
        )
    }

    fun clearTransientMessage() {
        _uiState.value = _uiState.value.copy(transientMessage = null)
    }

    fun revealAssignment(): AssignedRole? {
        val state = _uiState.value.game
        if (state.phase != GamePhase.ROLE_REVEAL) return null
        val currentPlayer = state.revealOrder.getOrNull(state.revealIndex) ?: return null
        return state.assignments.firstOrNull { it.playerName == currentPlayer }
    }

    fun currentRevealPlayer(): String? {
        val state = _uiState.value.game
        if (state.phase != GamePhase.ROLE_REVEAL) return null
        return state.revealOrder.getOrNull(state.revealIndex)
    }

    fun winnerLabel(): String {
        return when (_uiState.value.game.winner) {
            Winner.CIVILIANS -> "Civilians win"
            Winner.OUTLIERS -> "Outliers win"
            Winner.MR_WHITE -> "Mr White wins"
            Winner.NONE -> "No winner yet"
        }
    }

    fun roleCountsSummary(): String {
        val assignments = _uiState.value.game.assignments
        val undercovers = assignments.count { it.role == Role.UNDERCOVER }
        val mrWhite = assignments.count { it.role == Role.MR_WHITE }
        val civilians = assignments.count { it.role == Role.CIVILIAN }
        return "Civilians: $civilians | Undercovers: $undercovers | Mr White: $mrWhite"
    }

    fun roleRevealLinesForResult(): List<String> {
        val order = _uiState.value.game.assignments
        return order.map { assignment ->
            "${assignment.playerName} - ${roleName(assignment.role)}"
        }
    }

    fun eliminationTarget(): String? = _uiState.value.game.selectedEliminationTarget

    fun eliminationAnnouncement(): EliminationAnnouncement? = _uiState.value.game.eliminationAnnouncement

    private fun roleName(role: Role): String = when (role) {
        Role.CIVILIAN -> "Civilian"
        Role.UNDERCOVER -> "Undercover"
        Role.MR_WHITE -> "Mr White"
    }

    private fun maxUndercoverFor(playerCount: Int, mrWhiteCount: Int): Int {
        val roleCap = maxUndercoverCount(playerCount)
        val civilianCap = (playerCount - 2 - mrWhiteCount).coerceAtLeast(1)
        return minOf(roleCap, civilianCap)
    }

    private fun maxMrWhiteFor(playerCount: Int, undercoverCount: Int): Int {
        val roleCap = maxMrWhiteCount(playerCount)
        val civilianCap = (playerCount - 2 - undercoverCount).coerceAtLeast(0)
        return minOf(roleCap, civilianCap)
    }
}
