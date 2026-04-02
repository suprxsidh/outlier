package com.outlier.samplespace.game

import kotlin.random.Random

enum class GamePhase {
    SETUP,
    ROLE_REVEAL,
    CLUE_ROUND,
    ELIMINATION,
    MR_WHITE_GUESS,
    GAME_OVER
}

data class GameState(
    val phase: GamePhase = GamePhase.SETUP,
    val config: GameConfig? = null,
    val pair: WordPair? = null,
    val assignments: List<AssignedRole> = emptyList(),
    val revealIndex: Int = 0,
    val roundNumber: Int = 0,
    val alivePlayers: List<String> = emptyList(),
    val selectedEliminationTarget: String? = null,
    val pendingMrWhitePlayer: String? = null,
    val winner: Winner = Winner.NONE,
    val lastEliminatedPlayer: String? = null
)

class GameSession(private val random: Random = Random.Default) {

    fun startGame(
        playerNames: List<String>,
        config: GameConfig,
        pair: WordPair
    ): GameState {
        val configError = validateConfig(config)
        require(configError == null) { configError ?: "Invalid game config." }
        val namesError = validatePlayerNames(playerNames)
        require(namesError == null) { namesError ?: "Invalid player names." }
        require(playerNames.size == config.playerCount) {
            "Expected ${config.playerCount} players, got ${playerNames.size}."
        }

        val assignments = assignRoles(playerNames, config, pair, random)
        return GameState(
            phase = GamePhase.ROLE_REVEAL,
            config = config,
            pair = pair,
            assignments = assignments,
            revealIndex = 0,
            roundNumber = 1,
            alivePlayers = playerNames
        )
    }

    fun advanceReveal(state: GameState): GameState {
        require(state.phase == GamePhase.ROLE_REVEAL) { "Reveal is not active." }

        val nextIndex = state.revealIndex + 1
        return if (nextIndex >= state.assignments.size) {
            state.copy(phase = GamePhase.CLUE_ROUND, revealIndex = state.assignments.size)
        } else {
            state.copy(revealIndex = nextIndex)
        }
    }

    fun beginElimination(state: GameState): GameState {
        require(state.phase == GamePhase.CLUE_ROUND) { "Elimination can begin only from clue round." }
        require(state.alivePlayers.size >= 2) { "Need at least two players alive to eliminate." }
        return state.copy(
            phase = GamePhase.ELIMINATION,
            selectedEliminationTarget = null
        )
    }

    fun selectEliminationTarget(state: GameState, target: String): GameState {
        require(state.phase == GamePhase.ELIMINATION) { "Elimination selection is not active." }
        require(target in state.alivePlayers) { "Elimination target must be alive." }
        return state.copy(selectedEliminationTarget = target)
    }

    fun confirmElimination(state: GameState): GameState {
        require(state.phase == GamePhase.ELIMINATION) { "Elimination is not active." }
        val eliminated = state.selectedEliminationTarget
            ?: throw IllegalStateException("Select a player before elimination.")
        val assignment = state.assignments.firstOrNull { it.playerName == eliminated }
            ?: throw IllegalStateException("Eliminated player assignment not found.")
        val nextAlive = state.alivePlayers.filterNot { it == eliminated }

        if (assignment.role == Role.MR_WHITE) {
            return state.copy(
                phase = GamePhase.MR_WHITE_GUESS,
                alivePlayers = nextAlive,
                pendingMrWhitePlayer = eliminated,
                selectedEliminationTarget = null,
                lastEliminatedPlayer = eliminated,
                winner = Winner.NONE
            )
        }

        val winner = determineWinner(
            aliveRoles = state.assignments.filter { it.playerName in nextAlive }.map { it.role },
            mrWhiteGuessedWord = false
        )

        return if (winner != Winner.NONE) {
            state.copy(
                phase = GamePhase.GAME_OVER,
                alivePlayers = nextAlive,
                winner = winner,
                selectedEliminationTarget = null,
                lastEliminatedPlayer = eliminated
            )
        } else {
            state.copy(
                phase = GamePhase.CLUE_ROUND,
                alivePlayers = nextAlive,
                roundNumber = state.roundNumber + 1,
                selectedEliminationTarget = null,
                lastEliminatedPlayer = eliminated
            )
        }
    }

    fun submitMrWhiteGuess(state: GameState, guess: String): GameState {
        require(state.phase == GamePhase.MR_WHITE_GUESS) { "Mr White guess is not active." }
        val pair = state.pair ?: throw IllegalStateException("Word pair missing.")
        val guessedCorrectly = guess.trim().equals(pair.civilianWord, ignoreCase = true)

        return if (guessedCorrectly) {
            state.copy(
                phase = GamePhase.GAME_OVER,
                winner = Winner.MR_WHITE,
                pendingMrWhitePlayer = null
            )
        } else {
            val winner = determineWinner(
                aliveRoles = state.assignments
                    .filter { it.playerName in state.alivePlayers }
                    .map { it.role },
                mrWhiteGuessedWord = false
            )
            if (winner != Winner.NONE) {
                state.copy(
                    phase = GamePhase.GAME_OVER,
                    winner = winner,
                    pendingMrWhitePlayer = null
                )
            } else {
                state.copy(
                    phase = GamePhase.CLUE_ROUND,
                    roundNumber = state.roundNumber + 1,
                    pendingMrWhitePlayer = null
                )
            }
        }
    }
}
