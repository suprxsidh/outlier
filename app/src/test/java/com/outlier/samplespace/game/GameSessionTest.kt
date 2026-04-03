package com.outlier.samplespace.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun startGame_entersRevealPhase_withRandomRevealOrder() {
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
        assertEquals(5, state.revealOrder.size)
        assertEquals(state.alivePlayers.toSet(), state.revealOrder.toSet())
    }

    @Test
    fun startGame_firstRevealPlayer_isNeverMrWhite() {
        val session = GameSession(random = Random(11))
        val config = GameConfig(playerCount = 7, undercoverCount = 2, mrWhiteCount = 1)

        val state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F", "G"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )

        val first = state.revealOrder.first()
        val firstRole = state.assignments.first { it.playerName == first }.role

        assertTrue(firstRole != Role.MR_WHITE)
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
    fun confirmElimination_movesToAnnouncementAndPublishesCounts() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 6, undercoverCount = 2, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(6) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        val civilian = state.assignments.first { it.role == Role.CIVILIAN }.playerName
        state = session.selectEliminationTarget(state, civilian)
        state = session.confirmElimination(state)

        assertTrue(civilian !in state.alivePlayers)
        assertEquals(GamePhase.POST_ELIMINATION_ANNOUNCEMENT, state.phase)
        assertEquals(civilian, state.lastEliminatedPlayer)
        assertEquals(Role.CIVILIAN, state.eliminationAnnouncement?.eliminatedRole)
        assertEquals(2, state.eliminationAnnouncement?.civiliansLeft)
        assertEquals(2, state.eliminationAnnouncement?.undercoversLeft)
        assertEquals(1, state.eliminationAnnouncement?.mrWhitesLeft)
    }

    @Test
    fun continueAfterAnnouncement_goesToGuessWhenMrWhiteEliminated() {
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

        assertEquals(GamePhase.POST_ELIMINATION_ANNOUNCEMENT, state.phase)
        state = session.continueAfterAnnouncement(state)

        assertEquals(GamePhase.MR_WHITE_GUESS, state.phase)
        assertEquals(mrWhiteName, state.pendingMrWhitePlayer)
    }

    @Test
    fun continueAfterAnnouncement_goesToClueRoundWhenNoWinner() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 10, undercoverCount = 2, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(10) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        val civilian = state.assignments.first { it.role == Role.CIVILIAN }.playerName
        state = session.selectEliminationTarget(state, civilian)
        state = session.confirmElimination(state)
        state = session.continueAfterAnnouncement(state)

        assertEquals(GamePhase.CLUE_ROUND, state.phase)
        assertEquals(2, state.roundNumber)
    }

    @Test
    fun continueAfterAnnouncement_goesToGameOverWhenWinnerDetermined() {
        val session = GameSession(random = Random(3))
        val config = GameConfig(playerCount = 7, undercoverCount = 2, mrWhiteCount = 1)

        var state = session.startGame(
            playerNames = listOf("A", "B", "C", "D", "E", "F", "G"),
            config = config,
            pair = WordPair("Food", "Pizza", "Burger")
        )
        repeat(7) { state = session.advanceReveal(state) }
        state = session.beginElimination(state)

        val civilian = state.assignments.first { it.role == Role.CIVILIAN }.playerName
        state = session.selectEliminationTarget(state, civilian)
        state = session.confirmElimination(state)
        state = session.continueAfterAnnouncement(state)

        assertEquals(GamePhase.GAME_OVER, state.phase)
        assertEquals(Winner.OUTLIERS, state.winner)
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
        state = session.continueAfterAnnouncement(state)

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
