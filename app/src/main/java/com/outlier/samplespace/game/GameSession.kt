package com.outlier.samplespace.game

import kotlin.random.Random

enum class GamePhase {
    SETUP,
    ROLE_REVEAL,
    CLUE_ROUND,
    VOTING,
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
    val voteOrder: List<String> = emptyList(),
    val currentVoteIndex: Int = 0,
    val votes: Map<String, String> = emptyMap(),
    val tiedPlayers: List<String> = emptyList(),
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

    fun beginVoting(state: GameState): GameState {
        require(state.phase == GamePhase.CLUE_ROUND) { "Voting can begin only from clue round." }
        val alive = state.alivePlayers
        require(alive.size >= 2) { "Need at least two players alive to vote." }
        return state.copy(
            phase = GamePhase.VOTING,
            voteOrder = alive,
            currentVoteIndex = 0,
            votes = emptyMap(),
            tiedPlayers = emptyList()
        )
    }

    fun currentVoter(state: GameState): String? {
        if (state.phase != GamePhase.VOTING) return null
        if (state.currentVoteIndex !in state.voteOrder.indices) return null
        return state.voteOrder[state.currentVoteIndex]
    }

    fun castVoteForCurrentVoter(state: GameState, target: String): GameState {
        require(state.phase == GamePhase.VOTING) { "Voting is not active." }
        val voter = currentVoter(state) ?: throw IllegalStateException("No current voter available.")
        require(target in state.alivePlayers) { "Vote target must be alive." }
        require(target != voter) { "Self-vote is not allowed." }

        val nextVotes = state.votes + (voter to target)
        val nextIndex = state.currentVoteIndex + 1
        return state.copy(votes = nextVotes, currentVoteIndex = nextIndex)
    }

    fun resolveVoting(state: GameState): GameState {
        require(state.phase == GamePhase.VOTING) { "Voting is not active." }
        require(state.votes.size == state.voteOrder.size) { "All alive players must vote before resolving." }

        val result = resolveVotes(state.votes)
        if (result.tiedPlayers.isNotEmpty()) {
            return state.copy(
                phase = GamePhase.CLUE_ROUND,
                roundNumber = state.roundNumber + 1,
                votes = emptyMap(),
                voteOrder = emptyList(),
                currentVoteIndex = 0,
                tiedPlayers = result.tiedPlayers
            )
        }

        val eliminated = result.eliminatedPlayer
            ?: throw IllegalStateException("Expected eliminated player when no tie exists.")
        val assignment = state.assignments.firstOrNull { it.playerName == eliminated }
            ?: throw IllegalStateException("Eliminated player assignment not found.")
        val nextAlive = state.alivePlayers.filterNot { it == eliminated }
        val winner = determineWinner(
            aliveRoles = state.assignments.filter { it.playerName in nextAlive }.map { it.role },
            mrWhiteGuessedWord = false
        )

        if (assignment.role == Role.MR_WHITE) {
            return state.copy(
                phase = GamePhase.MR_WHITE_GUESS,
                alivePlayers = nextAlive,
                pendingMrWhitePlayer = eliminated,
                lastEliminatedPlayer = eliminated,
                votes = emptyMap(),
                voteOrder = emptyList(),
                currentVoteIndex = 0,
                tiedPlayers = emptyList(),
                winner = Winner.NONE
            )
        }

        return if (winner != Winner.NONE) {
            state.copy(
                phase = GamePhase.GAME_OVER,
                alivePlayers = nextAlive,
                winner = winner,
                lastEliminatedPlayer = eliminated,
                votes = emptyMap(),
                voteOrder = emptyList(),
                currentVoteIndex = 0,
                tiedPlayers = emptyList()
            )
        } else {
            state.copy(
                phase = GamePhase.CLUE_ROUND,
                alivePlayers = nextAlive,
                roundNumber = state.roundNumber + 1,
                lastEliminatedPlayer = eliminated,
                votes = emptyMap(),
                voteOrder = emptyList(),
                currentVoteIndex = 0,
                tiedPlayers = emptyList()
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
