package com.outlier.samplespace.ui

import org.junit.Assert.assertEquals
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
}
