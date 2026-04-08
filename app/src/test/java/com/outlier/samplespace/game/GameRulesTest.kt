package com.outlier.samplespace.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test
    fun maxUndercoverCount_followsFormula() {
        assertEquals(2, maxUndercoverCount(4))
        assertEquals(2, maxUndercoverCount(5))
        assertEquals(3, maxUndercoverCount(6))
        assertEquals(7, maxUndercoverCount(15))
    }

    @Test
    fun maxMrWhiteCount_followsFormula() {
        assertEquals(2, maxMrWhiteCount(4))
        assertEquals(2, maxMrWhiteCount(5))
        assertEquals(5, maxMrWhiteCount(10))
    }

    @Test
    fun validateConfig_acceptsBoundaryValidConfiguration() {
        val config = GameConfig(playerCount = 8, undercoverCount = 4, mrWhiteCount = 2)

        val error = validateConfig(config)

        assertNull(error)
    }

    @Test
    fun validateConfig_rejectsUndercoverAboveCap() {
        val config = GameConfig(playerCount = 8, undercoverCount = 5, mrWhiteCount = 0)

        val error = validateConfig(config)

        assertEquals("Undercovers must be between 1 and 4 for 8 players.", error)
    }

    @Test
    fun validateConfig_rejectsMrWhiteAboveCap() {
        val config = GameConfig(playerCount = 10, undercoverCount = 3, mrWhiteCount = 6)

        val error = validateConfig(config)

        assertEquals("Mr White must be between 0 and 5 for 10 players.", error)
    }

    @Test
    fun validateConfig_acceptsMrWhiteBasedOnPlayerHalf() {
        val config = GameConfig(playerCount = 10, undercoverCount = 1, mrWhiteCount = 5)

        val error = validateConfig(config)

        assertNull(error)
    }

    @Test
    fun assignRoles_distributesExactRoleCounts() {
        val names = listOf("A", "B", "C", "D", "E", "F")
        val config = GameConfig(playerCount = 6, undercoverCount = 2, mrWhiteCount = 1)
        val pair = WordPair(category = "Food", civilianWord = "Pizza", undercoverWord = "Burger")

        val assigned = assignRoles(names, config, pair, Random(42))

        assertEquals(6, assigned.size)
        assertEquals(2, assigned.count { it.role == Role.UNDERCOVER })
        assertEquals(1, assigned.count { it.role == Role.MR_WHITE })
        assertEquals(3, assigned.count { it.role == Role.CIVILIAN })
        assertEquals(1, assigned.count { it.role == Role.MR_WHITE && it.word == null })
    }

    @Test
    fun determineWinner_reportsOutliersWhenTheyReachParity() {
        val winner = determineWinner(
            aliveRoles = listOf(Role.CIVILIAN, Role.UNDERCOVER, Role.MR_WHITE),
            mrWhiteGuessedWord = false
        )

        assertEquals(Winner.OUTLIERS, winner)
    }

    @Test
    fun determineWinner_reportsMrWhiteWhenGuessSucceeded() {
        val winner = determineWinner(
            aliveRoles = listOf(Role.CIVILIAN, Role.UNDERCOVER),
            mrWhiteGuessedWord = true
        )

        assertEquals(Winner.MR_WHITE, winner)
    }

    @Test
    fun validatePlayerNames_rejectsBlanksAndDuplicates() {
        val message = validatePlayerNames(listOf("Alex", "", "Alex", "Dana"))

        assertTrue(message!!.contains("unique"))
    }
}
