package com.outlier.samplespace.game

import kotlin.random.Random

enum class GamePhase {
    SETUP,
    ROLE_REVEAL,
    CLUE_ROUND,
    ELIMINATION,
    POST_ELIMINATION_ANNOUNCEMENT,
    MR_WHITE_GUESS,
    GAME_OVER
}

data class EliminationAnnouncement(
    val eliminatedPlayer: String,
    val eliminatedRole: Role,
    val civiliansLeft: Int,
    val undercoversLeft: Int,
    val mrWhitesLeft: Int
)

data class GameState(
    val phase: GamePhase = GamePhase.SETUP,
    val config: GameConfig? = null,
    val pair: WordPair? = null,
    val assignments: List<AssignedRole> = emptyList(),
    val revealOrder: List<String> = emptyList(),
    val revealIndex: Int = 0,
    val roundNumber: Int = 0,
    val alivePlayers: List<String> = emptyList(),
    val selectedEliminationTarget: String? = null,
    val pendingMrWhitePlayer: String? = null,
    val eliminationAnnouncement: EliminationAnnouncement? = null,
    val winner: Winner = Winner.NONE,
    val lastEliminatedPlayer: String? = null,
    val countParts: Boolean = false
)

data class GameResult(
    val state: GameState,
    val civilianPoints: Int,
    val outlierPoints: Int
)

class GameSession(private val random: Random = Random.Default) {

    fun startGame(
        playerNames: List<String>,
        config: GameConfig,
        pair: WordPair,
        countParts: Boolean = false
    ): GameState {
        val configError = validateConfig(config)
        require(configError == null) { configError ?: "Invalid game config." }
        val namesError = validatePlayerNames(playerNames)
        require(namesError == null) { namesError ?: "Invalid player names." }
        require(playerNames.size == config.playerCount) {
            "Expected ${config.playerCount} players, got ${playerNames.size}."
        }

        val assignments = assignRoles(playerNames, config, pair, random)
        val revealOrder = buildRevealOrder(assignments)

        return GameState(
            phase = GamePhase.ROLE_REVEAL,
            config = config,
            pair = pair,
            assignments = assignments,
            revealOrder = revealOrder,
            revealIndex = 0,
            roundNumber = 1,
            alivePlayers = playerNames,
            countParts = countParts
        )
    }

    private fun buildRevealOrder(assignments: List<AssignedRole>): List<String> {
        val mrWhiteNames = assignments.filter { it.role == Role.MR_WHITE }.map { it.playerName }.toSet()
        val nonMrWhite = assignments.map { it.playerName }.filterNot { it in mrWhiteNames }

        if (nonMrWhite.isEmpty()) {
            return assignments.map { it.playerName }.shuffled(random)
        }

        val first = nonMrWhite.random(random)
        val remaining = assignments.map { it.playerName }
            .filterNot { it == first }
            .shuffled(random)

        return listOf(first) + remaining
    }

    fun advanceReveal(state: GameState): GameState {
        require(state.phase == GamePhase.ROLE_REVEAL) { "Reveal is not active." }

        val nextIndex = state.revealIndex + 1
        return if (nextIndex >= state.revealOrder.size) {
            state.copy(phase = GamePhase.CLUE_ROUND, revealIndex = state.revealOrder.size)
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
        val announcement = buildAnnouncement(
            assignments = state.assignments,
            nextAlive = nextAlive,
            eliminatedPlayer = eliminated,
            eliminatedRole = assignment.role
        )

        return state.copy(
            phase = GamePhase.POST_ELIMINATION_ANNOUNCEMENT,
            alivePlayers = nextAlive,
            pendingMrWhitePlayer = if (assignment.role == Role.MR_WHITE) eliminated else null,
            selectedEliminationTarget = null,
            lastEliminatedPlayer = eliminated,
            eliminationAnnouncement = announcement
        )
    }

    private fun buildAnnouncement(
        assignments: List<AssignedRole>,
        nextAlive: List<String>,
        eliminatedPlayer: String,
        eliminatedRole: Role
    ): EliminationAnnouncement {
        val aliveRoles = assignments
            .filter { it.playerName in nextAlive }
            .map { it.role }

        return EliminationAnnouncement(
            eliminatedPlayer = eliminatedPlayer,
            eliminatedRole = eliminatedRole,
            civiliansLeft = aliveRoles.count { it == Role.CIVILIAN },
            undercoversLeft = aliveRoles.count { it == Role.UNDERCOVER },
            mrWhitesLeft = aliveRoles.count { it == Role.MR_WHITE }
        )
    }

fun continueAfterAnnouncement(state: GameState): GameResult {
        require(state.phase == GamePhase.POST_ELIMINATION_ANNOUNCEMENT) {
            "Announcement is not active."
        }

        val winner = determineWinner(
            aliveRoles = state.assignments.filter { it.playerName in state.alivePlayers }.map { it.role },
            mrWhiteGuessedWord = false
        )

        val (civPoints, outlPoints) = scoreFromWinner(winner)

        if (winner != Winner.NONE) {
            return GameResult(
                state = state.copy(phase = GamePhase.GAME_OVER, winner = winner),
                civilianPoints = civPoints,
                outlierPoints = outlPoints
            )
        }

        return if (state.pendingMrWhitePlayer != null) {
            GameResult(
                state = state.copy(phase = GamePhase.MR_WHITE_GUESS),
                civilianPoints = 0,
                outlierPoints = 0
            )
        } else {
            GameResult(
                state = state.copy(phase = GamePhase.CLUE_ROUND, roundNumber = state.roundNumber + 1),
                civilianPoints = 0,
                outlierPoints = 0
            )
        }
    }

    fun submitMrWhiteGuess(state: GameState, guess: String): GameResult {
        require(state.phase == GamePhase.MR_WHITE_GUESS) { "Mr White guess is not active." }
        val pair = state.pair ?: throw IllegalStateException("Word pair missing.")
        val guessedCorrectly = guess.trim().equals(pair.civilianWord, ignoreCase = true)

        return if (guessedCorrectly) {
            GameResult(
                state = state.copy(
                    phase = GamePhase.GAME_OVER,
                    winner = Winner.MR_WHITE,
                    pendingMrWhitePlayer = null
                ),
                civilianPoints = 0,
                outlierPoints = 3
            )
        } else {
            val winner = determineWinner(
                aliveRoles = state.assignments
                    .filter { it.playerName in state.alivePlayers }
                    .map { it.role },
                mrWhiteGuessedWord = false
            )
            if (winner != Winner.NONE) {
                val (civPoints, outlPoints) = scoreFromWinner(winner)
                GameResult(
                    state = state.copy(
                        phase = GamePhase.GAME_OVER,
                        winner = winner,
                        pendingMrWhitePlayer = null
                    ),
                    civilianPoints = civPoints,
                    outlierPoints = outlPoints
                )
            } else {
                GameResult(
                    state = state.copy(
                        phase = GamePhase.CLUE_ROUND,
                        roundNumber = state.roundNumber + 1,
                        pendingMrWhitePlayer = null
                    ),
                    civilianPoints = 0,
                    outlierPoints = 0
                )
            }
        }
    }

    private fun scoreFromWinner(winner: Winner): Pair<Int, Int> {
        return when (winner) {
            Winner.CIVILIANS -> Pair(1, 0)
            Winner.OUTLIERS -> Pair(0, 3)
            Winner.MR_WHITE -> Pair(0, 3)
            Winner.NONE -> Pair(0, 0)
        }
    }
}

        val winner = determineWinner(
            aliveRoles = state.assignments.filter { it.playerName in state.alivePlayers }.map { it.role },
            mrWhiteGuessedWord = false
        )

        if (winner != Winner.NONE) {
            val (civilianScore, outlierScore) = calculateScores(winner, state)
            return state.copy(
                phase = GamePhase.GAME_OVER,
                winner = winner,
                civilianScore = civilianScore,
                outlierScore = outlierScore
            )
        }

        return if (state.pendingMrWhitePlayer != null) {
            state.copy(phase = GamePhase.MR_WHITE_GUESS)
        } else {
            state.copy(
                phase = GamePhase.CLUE_ROUND,
                roundNumber = state.roundNumber + 1
            )
        }
    }

    fun submitMrWhiteGuess(state: GameState, guess: String): GameState {
        require(state.phase == GamePhase.MR_WHITE_GUESS) { "Mr White guess is not active." }
        val pair = state.pair ?: throw IllegalStateException("Word pair missing.")
        val guessedCorrectly = guess.trim().equals(pair.civilianWord, ignoreCase = true)

        return if (guessedCorrectly) {
            val (civilianScore, outlierScore) = calculateScores(Winner.MR_WHITE, state)
            state.copy(
                phase = GamePhase.GAME_OVER,
                winner = Winner.MR_WHITE,
                pendingMrWhitePlayer = null,
                civilianScore = civilianScore,
                outlierScore = outlierScore
            )
        } else {
            val winner = determineWinner(
                aliveRoles = state.assignments
                    .filter { it.playerName in state.alivePlayers }
                    .map { it.role },
                mrWhiteGuessedWord = false
            )
            if (winner != Winner.NONE) {
                val (civilianScore, outlierScore) = calculateScores(winner, state)
                state.copy(
                    phase = GamePhase.GAME_OVER,
                    winner = winner,
                    pendingMrWhitePlayer = null,
                    civilianScore = civilianScore,
                    outlierScore = outlierScore
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

    private fun calculateScores(winner: Winner, state: GameState): Pair<Int, Int> {
        return when (winner) {
            Winner.CIVILIANS -> Pair(state.civilianScore + 1, state.outlierScore)
            Winner.OUTLIERS -> Pair(state.civilianScore, state.outlierScore + 3)
            Winner.MR_WHITE -> Pair(state.civilianScore, state.outlierScore + 3)
            Winner.NONE -> Pair(state.civilianScore, state.outlierScore)
        }
    }
}
