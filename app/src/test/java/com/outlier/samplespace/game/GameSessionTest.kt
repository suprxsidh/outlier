package com.outlier.samplespace.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun startGame_entersRevealPhase() {
        val session = GameSession(random = Random(7))
        val config = GameConfig(playerCount = 5, undercoverCount = 1, mrWhiteCount = 0)

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
    fun beginElimination_fromClueRound_entersEliminationPhase() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }

        state = session.beginElimination(state)

        assertEquals(GamePhase.ELIMINATION, state.phase)
        assertEquals(null, state.selectedEliminationTarget)
    }

    @Test
    fun selectingAndConfirmingElimination_removesChosenPlayer() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        state = session.selectEliminationTarget(state, "C")
        state = session.confirmElimination(state)

        assertTrue("C" !in state.alivePlayers)
        assertEquals("C", state.lastEliminatedPlayer)
    }

    @Test
    fun confirmElimination_withoutSelection_fails() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        val failure = runCatching { session.confirmElimination(state) }

        assertTrue(failure.isFailure)
    }

    @Test
    fun eliminatingMrWhite_opensGuessPhase() {
        val session = GameSession(random = Random(1))
        val config = GameConfig(playerCount = 6, undercoverCount = 2, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(6) { state = session.advanceReveal(state) }

        val mrWhiteName = state.assignments.first { it.role == Role.MR_WHITE }.playerName
        state = session.beginElimination(state)
        state = session.selectEliminationTarget(state, mrWhiteName)
        state = session.confirmElimination(state)

        assertEquals(GamePhase.MR_WHITE_GUESS, state.phase)
        assertEquals(mrWhiteName, state.pendingMrWhitePlayer)
    }

    @Test
    fun mrWhiteCorrectGuess_winsGame() {
        val session = GameSession(random = Random(1))
        val config = GameConfig(playerCount = 6, undercoverCount = 2, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(6) { state = session.advanceReveal(state) }

        val mrWhiteName = state.assignments.first { it.role == Role.MR_WHITE }.playerName
        state = session.beginElimination(state)
        state = session.selectEliminationTarget(state, mrWhiteName)
        state = session.confirmElimination(state)

        state = session.submitMrWhiteGuess(state, "Pizza")

        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertEquals(Winner.MR_WHITE, state.winner)
    }

    @Test
    fun selectingNonAliveTarget_fails() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 4, undercoverCount = 1, mrWhiteCount = 0)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(4) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        val failure = runCatching { session.selectEliminationTarget(state, "Z") }

        assertTrue(failure.isFailure)
    }
}
