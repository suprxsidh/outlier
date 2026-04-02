package com.outlier.samplespace.ui

import androidx.lifecycle.ViewModel
import com.outlier.samplespace.game.AssignedRole
import com.outlier.samplespace.game.GameConfig
import com.outlier.samplespace.game.GamePhase
import com.outlier.samplespace.game.GameSession
import com.outlier.samplespace.game.GameState
import com.outlier.samplespace.game.Role
import com.outlier.samplespace.game.Winner
import com.outlier.samplespace.game.WordBank
import com.outlier.samplespace.game.WordPair
import com.outlier.samplespace.game.validateConfig
import com.outlier.samplespace.game.validatePlayerNames
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SetupState(
    val playerCount: Int = 6,
    val undercoverCount: Int = 1,
    val mrWhiteCount: Int = 1,
    val playerNames: List<String> = defaultNames(6),
    val selectedCategory: String = "All",
    val errorMessage: String? = null
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

    fun updatePlayerCount(count: Int) {
        val clamped = count.coerceIn(4, 15)
        val current = _uiState.value.setup
        val updatedNames = current.playerNames
            .take(clamped)
            .toMutableList()
            .apply {
                while (size < clamped) {
                    add("Player ${size + 1}")
                }
            }
        val undercover = current.undercoverCount.coerceIn(1, clamped - 2)
        val maxMrWhite = clamped - undercover - 2
        val mrWhite = current.mrWhiteCount.coerceIn(0, maxMrWhite.coerceAtLeast(0))

        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                playerCount = clamped,
                undercoverCount = undercover,
                mrWhiteCount = mrWhite,
                playerNames = updatedNames,
                errorMessage = null
            )
        )
    }

    fun updateUndercoverCount(count: Int) {
        val current = _uiState.value.setup
        val maxUndercover = (current.playerCount - current.mrWhiteCount - 2).coerceAtLeast(1)
        val undercover = count.coerceIn(1, maxUndercover)
        val maxMrWhite = (current.playerCount - undercover - 2).coerceAtLeast(0)
        val mrWhite = current.mrWhiteCount.coerceAtMost(maxMrWhite)
        _uiState.value = _uiState.value.copy(
            setup = current.copy(
                undercoverCount = undercover,
                mrWhiteCount = mrWhite,
                errorMessage = null
            )
        )
    }

    fun updateMrWhiteCount(count: Int) {
        val current = _uiState.value.setup
        val maxMrWhite = (current.playerCount - current.undercoverCount - 2).coerceAtLeast(0)
        val mrWhite = count.coerceIn(0, maxMrWhite)
        _uiState.value = _uiState.value.copy(
            setup = current.copy(mrWhiteCount = mrWhite, errorMessage = null)
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
            setup = setup.copy(errorMessage = null),
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

    fun startVoting() {
        val next = session.beginVoting(_uiState.value.game)
        _uiState.value = _uiState.value.copy(game = next)
    }

    fun currentVoter(): String? = session.currentVoter(_uiState.value.game)

    fun castVote(target: String) {
        val current = _uiState.value.game
        val next = runCatching { session.castVoteForCurrentVoter(current, target) }
            .getOrElse {
                _uiState.value = _uiState.value.copy(transientMessage = it.message ?: "Invalid vote")
                return
            }

        if (next.votes.size == next.voteOrder.size) {
            val resolved = session.resolveVoting(next)
            _uiState.value = _uiState.value.copy(game = resolved, transientMessage = null)
        } else {
            _uiState.value = _uiState.value.copy(game = next, transientMessage = null)
        }
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
        _uiState.value = OutlierUiState(setup = _uiState.value.setup.copy(errorMessage = null))
    }

    fun clearTransientMessage() {
        _uiState.value = _uiState.value.copy(transientMessage = null)
    }

    fun revealAssignment(): AssignedRole? {
        val state = _uiState.value.game
        if (state.phase != GamePhase.ROLE_REVEAL) return null
        return state.assignments.getOrNull(state.revealIndex)
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
}
