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
    val errorMessage: String? = null,
    val helperMessage: String? = null
)

data class OutlierUiState(
    val setup: SetupState = SetupState(),
    val game: GameState = GameState(),
    val revealShown: Boolean = false,
    val guessInput: String = "",
    val transientMessage: String? = null
)

private fun defaultNames(count: Int): List<String> =
    (1..count).map { "Player $it" }

class OutlierViewModel : ViewModel() {
    private val session = GameSession()
    private val _uiState = MutableStateFlow(OutlierUiState())
    val uiState: StateFlow<OutlierUiState> = _uiState.asStateFlow()

    val categories: List<String> = listOf("All") + WordBank.allPairs.map { it.category }.distinct().sorted()

    fun maxUndercoverForCurrentSetup(): Int = maxUndercoverCount(_uiState.value.setup.playerCount)

    fun maxMrWhiteForCurrentSetup(): Int = maxMrWhiteCount(_uiState.value.setup.undercoverCount).coerceAtLeast(0)

    fun updatePlayerCount(count: Int) {
        val current = _uiState.value.setup
        val clampedPlayers = count.coerceIn(4, 15)
        val updatedNames = current.playerNames.take(clampedPlayers).toMutableList().apply {
            while (size < clampedPlayers) {
                add("Player ${size + 1}")
            }
        }

        val underCap = maxUndercoverCount(clampedPlayers)
        val undercovers = current.undercoverCount.coerceIn(1, underCap)
        val mrCap = maxMrWhiteCount(undercovers).coerceAtLeast(0)
        val mrWhite = current.mrWhiteCount.coerceIn(0, mrCap)

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
            )
        )
    }

    fun updateUndercoverCount(count: Int) {
        val current = _uiState.value.setup
        val underCap = maxUndercoverCount(current.playerCount)
        val undercovers = count.coerceIn(1, underCap)
        val mrCap = maxMrWhiteCount(undercovers).coerceAtLeast(0)
        val mrWhite = current.mrWhiteCount.coerceIn(0, mrCap)

        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                undercoverCount = undercovers,
                mrWhiteCount = mrWhite,
                errorMessage = null,
                helperMessage = if (mrWhite != current.mrWhiteCount) {
                    "Mr White adjusted to fit undercover limit."
                } else {
                    null
                }
            )
        )
    }

    fun updateMrWhiteCount(count: Int) {
        val current = _uiState.value.setup
        val mrCap = maxMrWhiteCount(current.undercoverCount).coerceAtLeast(0)
        val mrWhite = count.coerceIn(0, mrCap)
        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                mrWhiteCount = mrWhite,
                errorMessage = null,
                helperMessage = null
            )
        )
    }

    fun updatePlayerName(index: Int, value: String) {
        val current = _uiState.value.setup
        if (index !in current.playerNames.indices) return
        val names = current.playerNames.toMutableList()
        names[index] = value
        _uiState.value = _uiState.value.copy(
            setup = current.copy(playerNames = names, errorMessage = null)
        )
    }

    fun updateCategory(category: String) {
        val current = _uiState.value.setup
        _uiState.value = _uiState.value.copy(
            setup = current.copy(selectedCategory = category, errorMessage = null)
        )
    }

    fun startGame() {
        val setup = _uiState.value.setup
        val names = setup.playerNames.map { it.trim() }
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
        val state = session.startGame(names, config, pair)
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
        val next = runCatching { session.continueAfterAnnouncement(current) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Cannot continue yet")
                return
            }

        _uiState.value = _uiState.value.copy(game = next, transientMessage = null)
    }

    fun updateGuess(value: String) {
        _uiState.value = _uiState.value.copy(guessInput = value)
    }

    fun submitGuess() {
        val guess = _uiState.value.guessInput.trim()
        if (guess.isEmpty()) return
        val next = session.submitMrWhiteGuess(_uiState.value.game, guess)
        _uiState.value = _uiState.value.copy(game = next, guessInput = "", transientMessage = null)
    }

    fun resetToSetup() {
        _uiState.value = OutlierUiState(setup = SetupState())
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
}
