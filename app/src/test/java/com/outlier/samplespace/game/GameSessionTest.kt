package com.outlier.samplespace.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun startGame_entersRevealPhase() {
        val session = GameSession(random = Random(7))
        val config = GameConfig(playerCount = 5, undercoverCount = 1, mrWhiteCount = 1)

        val state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )

        assertEquals(GamePhase.ROLE_REVEAL, state.phase)
        assertEquals(0, state.revealIndex)
        assertEquals(5, state.alivePlayers.size)
    }

    @Test
    fun completeReveal_movesToClueRound() {
        val session = GameSession(random = Random(7))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )

        repeat(4) {
            state = session.advanceReveal(state)
        }

        assertEquals(GamePhase.CLUE_ROUND, state.phase)
        assertEquals(1, state.roundNumber)
    }

    @Test
    fun tiedVote_returnsToNextClueRound() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }
        state = session.beginVoting(state)
        state = session.castVoteForCurrentVoter(state, "B")
        state = session.castVoteForCurrentVoter(state, "A")
        state = session.castVoteForCurrentVoter(state, "A")
        state = session.castVoteForCurrentVoter(state, "B")

        state = session.resolveVoting(state)

        assertEquals(GamePhase.CLUE_ROUND, state.phase)
        assertEquals(2, state.roundNumber)
        assertEquals(listOf("A", "B"), state.tiedPlayers)
    }

    @Test
    fun mrWhiteEliminated_opensGuessPhase() {
        val session = GameSession(random = Random(1))
        val config = GameConfig(playerCount = 5, undercoverCount = 1, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(5) { state = session.advanceReveal(state) }

        val mrWhiteName = state.assignments.first { it.role == Role.MR_WHITE }.playerName
        state = session.beginVoting(state)
        repeat(state.alivePlayers.size) {
            val voter = session.currentVoter(state)!!
            val target = if (voter == mrWhiteName) {
                state.alivePlayers.first { it != mrWhiteName }
            } else {
                mrWhiteName
            }
            state = session.castVoteForCurrentVoter(state, target)
        }

        state = session.resolveVoting(state)

        assertEquals(GamePhase.MR_WHITE_GUESS, state.phase)
        assertEquals(mrWhiteName, state.pendingMrWhitePlayer)
    }

    @Test
    fun mrWhiteCorrectGuess_winsGame() {
        val session = GameSession(random = Random(1))
        val config = GameConfig(playerCount = 5, undercoverCount = 1, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(5) { state = session.advanceReveal(state) }

        val mrWhiteName = state.assignments.first { it.role == Role.MR_WHITE }.playerName
        state = session.beginVoting(state)
        repeat(state.alivePlayers.size) {
            val voter = session.currentVoter(state)!!
            val target = if (voter == mrWhiteName) {
                state.alivePlayers.first { it != mrWhiteName }
            } else {
                mrWhiteName
            }
            state = session.castVoteForCurrentVoter(state, target)
        }
        state = session.resolveVoting(state)

        state = session.submitMrWhiteGuess(state, "Pizza")

        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertEquals(Winner.MR_WHITE, state.winner)
    }

    @Test
    fun invalidVoteForSelf_isRejected() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }
        state = session.beginVoting(state)

        val voter = session.currentVoter(state)!!
        val failure = runCatching { session.castVoteForCurrentVoter(state, voter) }

        assertTrue(failure.isFailure)
    }
}
