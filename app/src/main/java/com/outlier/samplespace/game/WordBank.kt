package com.outlier.samplespace.game

import kotlin.math.sqrt

object WordBank {
    private val categoryWordGroups: Map<String, List<List<String>>> = mapOf(
        "Food" to listOf(
            listOf("Pizza", "Burger", "Taco", "Burrito", "Sandwich", "Salad", "Pasta", "Soup"),
            listOf("Pancake", "Waffle", "Donut", "Cookie", "Cupcake", "Brownie", "Muffin", "Pie")
        ),
        "Beverages" to listOf(
            listOf("Coke", "Pepsi", "Fanta", "Sprite", "Seven Up", "Mirinda", "Mountain Dew", "Dr Pepper"),
            listOf("Coffee", "Tea", "Juice", "Smoothie", "Lemonade", "Milkshake", "Soda", "Water")
        ),
        "Animals" to listOf(
            listOf("Dog", "Cat", "Rabbit", "Hamster", "Parrot", "Turtle", "Goldfish", "Guinea Pig"),
            listOf("Lion", "Tiger", "Leopard", "Cheetah", "Wolf", "Fox", "Bear", "Panther")
        ),
        "Places" to listOf(
            listOf("Library", "Museum", "Cinema", "Theater", "Aquarium", "Gallery", "Stadium", "Park"),
            listOf("Airport", "Station", "Harbor", "Terminal", "Runway", "Gate", "Platform", "Ticket Counter")
        ),
        "Buildings" to listOf(
            listOf("Elevator", "Escalator", "Stairs", "Hallway", "Lobby", "Corridor", "Balcony", "Atrium"),
            listOf("Classroom", "Laboratory", "Office", "Conference Room", "Cafeteria", "Gymnasium", "Auditorium", "Library Room")
        ),
        "Objects" to listOf(
            listOf("Laptop", "Tablet", "Phone", "Monitor", "Keyboard", "Mouse", "Headphones", "Webcam"),
            listOf("Backpack", "Suitcase", "Wallet", "Umbrella", "Bottle", "Notebook", "Pen", "Pencil")
        ),
        "Sports" to listOf(
            listOf("Soccer", "Basketball", "Volleyball", "Tennis", "Badminton", "Baseball", "Cricket", "Rugby"),
            listOf("Swimming", "Running", "Cycling", "Rowing", "Skating", "Skiing", "Surfing", "Archery")
        ),
        "Music" to listOf(
            listOf("Guitar", "Piano", "Violin", "Cello", "Drums", "Flute", "Trumpet", "Saxophone"),
            listOf("Melody", "Rhythm", "Harmony", "Chorus", "Verse", "Tempo", "Beat", "Tune")
        ),
        "Nature" to listOf(
            listOf("Forest", "River", "Ocean", "Lake", "Waterfall", "Meadow", "Valley", "Canyon"),
            listOf("Sunrise", "Sunset", "Rainbow", "Breeze", "Cloud", "Thunder", "Lightning", "Mist")
        ),
        "Jobs" to listOf(
            listOf("Doctor", "Nurse", "Dentist", "Pharmacist", "Surgeon", "Paramedic", "Therapist", "Pediatrician"),
            listOf("Teacher", "Principal", "Librarian", "Counselor", "Tutor", "Professor", "Coach", "Instructor")
        ),
        "Home" to listOf(
            listOf("Kitchen", "Bedroom", "Bathroom", "Living Room", "Dining Room", "Garage", "Basement", "Attic"),
            listOf("Sofa", "Armchair", "Table", "Cabinet", "Curtain", "Pillow", "Blanket", "Lamp")
        ),
        "Travel" to listOf(
            listOf("Passport", "Ticket", "Itinerary", "Luggage", "Boarding Pass", "Visa", "Checkpoint", "Customs"),
            listOf("Taxi", "Bus", "Train", "Subway", "Tram", "Ferry", "Cruise", "Shuttle")
        ),
        "School" to listOf(
            listOf("Notebook", "Workbook", "Homework", "Project", "Essay", "Exam", "Quiz", "Syllabus"),
            listOf("Student", "Teacher", "Classroom", "Locker", "Semester", "Timetable", "Diploma", "Graduation")
        ),
        "Weather" to listOf(
            listOf("Sunny", "Cloudy", "Rainy", "Windy", "Foggy", "Stormy", "Snowy", "Humid"),
            listOf("Drizzle", "Shower", "Downpour", "Blizzard", "Hail", "Frost", "Monsoon", "Heatwave")
        ),
        "Entertainment" to listOf(
            listOf("Movie", "Series", "Cartoon", "Documentary", "Podcast", "Radio", "Concert", "Festival"),
            listOf("Puzzle", "Riddle", "Board Game", "Card Game", "Trivia", "Story", "Comic", "Novel")
        ),
        "Science" to listOf(
            listOf("Atom", "Molecule", "Cell", "Neuron", "Tissue", "Organ", "Genome", "Protein"),
            listOf("Planet", "Galaxy", "Comet", "Asteroid", "Orbit", "Telescope", "Satellite", "Nebula")
        ),
        "Technology" to listOf(
            listOf("Browser", "Website", "App", "Server", "Database", "Cloud", "Firewall", "Password"),
            listOf("Robot", "Drone", "Sensor", "Processor", "Algorithm", "Code", "Console", "Router")
        )
    )

    const val MIN_PAIR_QUALITY: Double = 0.58

    private val categoryGroupLookup: Map<String, List<Set<String>>> = categoryWordGroups.mapValues { (_, groups) ->
        groups.map { group -> group.map { it.trim().lowercase() }.toSet() }
    }

    private fun canonicalKey(pair: WordPair): String {
        val a = pair.civilianWord.trim().lowercase()
        val b = pair.undercoverWord.trim().lowercase()
        val ordered = if (a <= b) "$a|$b" else "$b|$a"
        return "${pair.category.lowercase()}|$ordered"
    }

    private fun bigramVector(value: String): Map<String, Int> {
        val normalized = value.lowercase().filter { it.isLetterOrDigit() }
        if (normalized.length < 2) return emptyMap()
        val grams = mutableMapOf<String, Int>()
        for (i in 0 until normalized.length - 1) {
            val gram = normalized.substring(i, i + 2)
            grams[gram] = (grams[gram] ?: 0) + 1
        }
        return grams
    }

    private fun cosineSimilarity(left: Map<String, Int>, right: Map<String, Int>): Double {
        if (left.isEmpty() || right.isEmpty()) return 0.0
        val dot = left.entries.sumOf { (gram, value) -> value * (right[gram] ?: 0) }
        val leftNorm = sqrt(left.values.sumOf { it * it }.toDouble())
        val rightNorm = sqrt(right.values.sumOf { it * it }.toDouble())
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0.0
        return dot / (leftNorm * rightNorm)
    }

    private fun rootOverlap(left: String, right: String): Double {
        val a = left.lowercase().filter { it.isLetterOrDigit() }
        val b = right.lowercase().filter { it.isLetterOrDigit() }
        if (a.isEmpty() || b.isEmpty()) return 0.0

        var commonPrefix = 0
        val minLength = minOf(a.length, b.length)
        while (commonPrefix < minLength && a[commonPrefix] == b[commonPrefix]) {
            commonPrefix++
        }

        return sqrt(commonPrefix.toDouble() / minLength.toDouble())
    }

    internal fun lexicalSimilarity(first: String, second: String): Double {
        val cosine = cosineSimilarity(bigramVector(first), bigramVector(second))
        val root = rootOverlap(first, second)
        return (0.7 * cosine + 0.3 * root).coerceIn(0.0, 1.0)
    }

    private fun isSameSemanticGroup(pair: WordPair): Boolean {
        val groups = categoryGroupLookup[pair.category] ?: return false
        val a = pair.civilianWord.trim().lowercase()
        val b = pair.undercoverWord.trim().lowercase()
        return groups.any { group -> a in group && b in group }
    }

    internal fun qualityScore(pair: WordPair): Double {
        val semantic = if (isSameSemanticGroup(pair)) 1.0 else 0.0
        val lexical = lexicalSimilarity(pair.civilianWord, pair.undercoverWord)
        val left = pair.civilianWord.filter { it.isLetterOrDigit() }.length
        val right = pair.undercoverWord.filter { it.isLetterOrDigit() }.length
        val lengthBalance = if (maxOf(left, right) == 0) 0.0 else 1.0 - (kotlin.math.abs(left - right).toDouble() / maxOf(left, right))

        return (0.65 * semantic + 0.25 * lexical + 0.10 * lengthBalance).coerceIn(0.0, 1.0)
    }

    val allPairs: List<WordPair> = categoryWordGroups.flatMap { (category, groups) ->
        groups.flatMap { rawWords ->
            val words = rawWords.map { it.trim() }.filter { it.isNotBlank() }.distinct()
            buildList {
                for (i in words.indices) {
                    for (j in i + 1 until words.size) {
                        add(WordPair(category = category, civilianWord = words[i], undercoverWord = words[j]))
                    }
                }
            }
        }
    }
        .filter {
            it.civilianWord.isNotBlank() &&
                it.undercoverWord.isNotBlank() &&
                !it.civilianWord.equals(it.undercoverWord, ignoreCase = true) &&
                qualityScore(it) >= MIN_PAIR_QUALITY
        }
        .distinctBy(::canonicalKey)
}
