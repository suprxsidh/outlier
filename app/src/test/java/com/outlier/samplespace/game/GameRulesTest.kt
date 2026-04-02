package com.outlier.samplespace.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test
    fun validateConfig_acceptsValidConfiguration() {
        val config = GameConfig(playerCount = 8, undercoverCount = 2, mrWhiteCount = 1)

        val error = validateConfig(config)

        assertNull(error)
    }

    @Test
    fun validateConfig_rejectsTooManyOutliers() {
        val config = GameConfig(playerCount = 6, undercoverCount = 3, mrWhiteCount = 2)

        val error = validateConfig(config)

        assertEquals("At least two civilians are required.", error)
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
    fun resolveVotes_returnsTieWhenTopVotesMatch() {
        val votes = mapOf("A" to "B", "B" to "A", "C" to "A", "D" to "B")

        val result = resolveVotes(votes)

        assertEquals(null, result.eliminatedPlayer)
        assertEquals(listOf("A", "B"), result.tiedPlayers)
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
