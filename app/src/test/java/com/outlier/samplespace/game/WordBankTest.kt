package com.outlier.samplespace.game

import org.junit.Assert.assertTrue
import org.junit.Test

class WordBankTest {

    @Test
    fun generatedPairs_hasAtLeastFiveHundredEntries() {
        assertTrue(WordBank.allPairs.size >= 500)
    }

    @Test
    fun generatedPairs_haveNoBlankOrEqualWords() {
        val invalid = WordBank.allPairs.any {
            it.civilianWord.isBlank() ||
                it.undercoverWord.isBlank() ||
                it.civilianWord.equals(it.undercoverWord, ignoreCase = true)
        }

        assertTrue(!invalid)
    }

    @Test
    fun generatedPairs_haveLargeCategoryCoverage() {
        val categories = WordBank.allPairs.map { it.category }.toSet()
        assertTrue(categories.size >= 12)
    }

    @Test
    fun generatedPairs_arePlayableWordsWithoutNumericSuffixes() {
        val hasNumbers = WordBank.allPairs.any {
            it.civilianWord.any(Char::isDigit) || it.undercoverWord.any(Char::isDigit)
        }

        assertTrue(!hasNumbers)
    }
}
