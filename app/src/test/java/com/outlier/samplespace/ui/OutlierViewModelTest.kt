package com.outlier.samplespace.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutlierViewModelTest {

    @Test
    fun defaults_areSevenTwoOne() {
        val vm = OutlierViewModel()

        val setup = vm.uiState.value.setup

        assertEquals(7, setup.playerCount)
        assertEquals(2, setup.undercoverCount)
        assertEquals(1, setup.mrWhiteCount)
        assertEquals(7, setup.playerNames.size)
    }

    @Test
    fun resetToSetup_restoresSevenTwoOne() {
        val vm = OutlierViewModel()
        vm.updatePlayerCount(10)
        vm.updateUndercoverCount(3)
        vm.updateMrWhiteCount(2)

        vm.resetToSetup()

        val setup = vm.uiState.value.setup
        assertEquals(7, setup.playerCount)
        assertEquals(2, setup.undercoverCount)
        assertEquals(1, setup.mrWhiteCount)
        assertEquals(7, setup.playerNames.size)
    }

    @Test
    fun updatePlayerCount_capsBothOutlierRolesAtHalfPlayers() {
        val vm = OutlierViewModel()
        vm.updatePlayerCount(6)
        vm.updateUndercoverCount(3)
        vm.updateMrWhiteCount(3)

        vm.updatePlayerCount(4)

        val setup = vm.uiState.value.setup
        assertTrue(setup.undercoverCount <= 2)
        assertTrue(setup.mrWhiteCount <= 2)
        assertTrue(setup.undercoverCount + setup.mrWhiteCount <= 2)
    }

    @Test
    fun startGame_usesFallbackNamesForBlankInputs() {
        val vm = OutlierViewModel()
        vm.updatePlayerName(0, "")
        vm.updatePlayerName(1, "  ")
        vm.updatePlayerName(2, "Alex")

        vm.startGame()

        val assignedNames = vm.uiState.value.game.assignments.map { it.playerName }
        assertTrue("Player 1" in assignedNames)
        assertTrue("Player 2" in assignedNames)
        assertTrue("Alex" in assignedNames)
    }
}
