package com.outlier.samplespace.game

import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
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
    fun generatedPairs_includeBrandedAndNeutralExamples() {
        fun hasPair(wordA: String, wordB: String): Boolean {
            return WordBank.allPairs.any {
                (it.civilianWord.equals(wordA, ignoreCase = true) &&
                    it.undercoverWord.equals(wordB, ignoreCase = true)) ||
                    (it.civilianWord.equals(wordB, ignoreCase = true) &&
                        it.undercoverWord.equals(wordA, ignoreCase = true))
            }
        }

        assertTrue(hasPair("Coke", "Fanta"))
        assertTrue(hasPair("Elevator", "Escalator"))
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

    @Test
    fun generatedPairs_useShortNaturalTerms() {
        val hasLongPhrases = WordBank.allPairs.any {
            it.civilianWord.trim().split("\\s+".toRegex()).size > 2 ||
                it.undercoverWord.trim().split("\\s+".toRegex()).size > 2
        }

        assertTrue(!hasLongPhrases)
    }

    @Test
    fun lexicalSimilarity_isSymmetricAndBounded() {
        val a = WordBank.lexicalSimilarity("Elevator", "Escalator")
        val b = WordBank.lexicalSimilarity("Escalator", "Elevator")

        assertTrue(a in 0.0..1.0)
        assertEquals(a, b, 0.000001)
    }

    @Test
    fun lexicalSimilarity_scoresRelatedWordsHigherThanUnrelatedWords() {
        val related = WordBank.lexicalSimilarity("Elevator", "Escalator")
        val unrelated = WordBank.lexicalSimilarity("Elevator", "Galaxy")

        assertTrue(related > unrelated)
    }

    @Test
    fun generatedPairs_passMinimumQualityThreshold() {
        val lowQuality = WordBank.allPairs.filter {
            WordBank.qualityScore(it) < WordBank.MIN_PAIR_QUALITY
        }

        assertTrue(lowQuality.isEmpty())
    }
}
