package com.outlier.samplespace.game

import kotlin.random.Random

enum class Role {
    CIVILIAN,
    UNDERCOVER,
    MR_WHITE
}

enum class Winner {
    NONE,
    CIVILIANS,
    OUTLIERS,
    MR_WHITE
}

data class GameConfig(
    val playerCount: Int,
    val undercoverCount: Int,
    val mrWhiteCount: Int
)

data class WordPair(
    val category: String,
    val civilianWord: String,
    val undercoverWord: String
)

data class AssignedRole(
    val playerName: String,
    val role: Role,
    val word: String?
)

data class VoteResolution(
    val eliminatedPlayer: String?,
    val tiedPlayers: List<String>
)

fun maxUndercoverCount(playerCount: Int): Int = playerCount / 2

fun maxMrWhiteCount(playerCount: Int): Int = playerCount / 2

fun validateConfig(config: GameConfig): String? {
    if (config.playerCount !in 4..15) {
        return "Player count must be between 4 and 15."
    }

    val undercoverCap = maxUndercoverCount(config.playerCount)
    if (config.undercoverCount !in 1..undercoverCap) {
        return "Undercovers must be between 1 and $undercoverCap for ${config.playerCount} players."
    }

    if (config.mrWhiteCount < 0) {
        return "Mr White count cannot be negative."
    }

    val mrWhiteCap = maxMrWhiteCount(config.playerCount)
    if (config.mrWhiteCount !in 0..mrWhiteCap) {
        return "Mr White must be between 0 and $mrWhiteCap for ${config.playerCount} players."
    }

    val civilians = config.playerCount - config.undercoverCount - config.mrWhiteCount
    if (civilians < 2) {
        return "At least two civilians are required."
    }
    return null
}

fun validatePlayerNames(playerNames: List<String>): String? {
    if (playerNames.isEmpty()) {
        return "At least one player is required."
    }
    if (playerNames.any { it.isBlank() }) {
        return "Player names must be non-blank and unique."
    }
    val normalized = playerNames.map { it.trim().lowercase() }
    if (normalized.distinct().size != normalized.size) {
        return "Player names must be non-blank and unique."
    }
    return null
}

fun assignRoles(
    playerNames: List<String>,
    config: GameConfig,
    pair: WordPair,
    random: Random = Random.Default
): List<AssignedRole> {
    val configError = validateConfig(config)
    require(configError == null) { configError ?: "Invalid game config." }
    val namesError = validatePlayerNames(playerNames)
    require(namesError == null) { namesError ?: "Invalid player names." }
    require(playerNames.size == config.playerCount) {
        "Expected ${config.playerCount} players, got ${playerNames.size}."
    }

    val civilianCount = config.playerCount - config.undercoverCount - config.mrWhiteCount
    val rolePool = MutableList(config.mrWhiteCount) { Role.MR_WHITE } +
            MutableList(config.undercoverCount) { Role.UNDERCOVER } +
            MutableList(civilianCount) { Role.CIVILIAN }

    val shuffledRoles = rolePool.shuffled(random)

    return playerNames.mapIndexed { index, name ->
        val role = shuffledRoles[index]
        AssignedRole(
            playerName = name,
            role = role,
            word = when (role) {
                Role.MR_WHITE -> null
                Role.UNDERCOVER -> pair.undercoverWord
                Role.CIVILIAN -> pair.civilianWord
            }
        )
    }
}

fun resolveVotes(votes: Map<String, String>): VoteResolution {
    if (votes.isEmpty()) {
        return VoteResolution(eliminatedPlayer = null, tiedPlayers = emptyList())
    }
    val counts = votes.values.groupingBy { it }.eachCount()
    val maxCount = counts.values.maxOrNull() ?: 0
    val topPlayers = counts
        .filterValues { it == maxCount }
        .keys
        .sorted()

    return if (topPlayers.size == 1) {
        VoteResolution(eliminatedPlayer = topPlayers.first(), tiedPlayers = emptyList())
    } else {
        VoteResolution(eliminatedPlayer = null, tiedPlayers = topPlayers)
    }
}

fun determineWinner(
    aliveRoles: List<Role>,
    mrWhiteGuessedWord: Boolean
): Winner {
    if (mrWhiteGuessedWord) {
        return Winner.MR_WHITE
    }

    val civilians = aliveRoles.count { it == Role.CIVILIAN }
    val undercovers = aliveRoles.count { it == Role.UNDERCOVER }
    val mrWhiteAlive = aliveRoles.count { it == Role.MR_WHITE } > 0

    if (!mrWhiteAlive && undercovers == 0) {
        return Winner.CIVILIANS
    }
    if (civilians == undercovers) {
        return Winner.OUTLIERS
    }
    return Winner.NONE
}
