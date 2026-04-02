package com.outlier.samplespace.game

object WordBank {
    private val categoryWords: Map<String, List<String>> = mapOf(
        "Food" to listOf(
            "Pizza", "Burger", "Pasta", "Sushi", "Taco", "Burrito", "Sandwich", "Salad",
            "Soup", "Pancake", "Waffle", "Donut", "Cookie", "Cupcake", "Brownie",
            "Noodles", "Dumpling", "Omelet", "Cereal", "Yogurt"
        ),
        "Animals" to listOf(
            "Dog", "Cat", "Horse", "Rabbit", "Squirrel", "Fox", "Wolf", "Bear", "Lion",
            "Tiger", "Elephant", "Giraffe", "Dolphin", "Whale", "Penguin", "Eagle",
            "Parrot", "Otter", "Turtle", "Kangaroo"
        ),
        "Places" to listOf(
            "Airport", "Library", "Museum", "Beach", "Park", "Hospital", "School",
            "Bakery", "Cafe", "Cinema", "Stadium", "Bridge", "Castle", "Harbor",
            "Mountain", "Forest", "Village", "Temple", "Market", "Station"
        ),
        "Objects" to listOf(
            "Laptop", "Tablet", "Phone", "Backpack", "Suitcase", "Pencil", "Pen",
            "Chair", "Clock", "Mirror", "Camera", "Umbrella", "Helmet", "Lantern",
            "Notebook", "Scissors", "Compass", "Bottle", "Basket", "Headphones"
        ),
        "Sports" to listOf(
            "Soccer", "Basketball", "Tennis", "Cricket", "Baseball", "Hockey", "Rugby",
            "Cycling", "Running", "Swimming", "Skating", "Boxing", "Wrestling", "Golf",
            "Badminton", "Volleyball", "Surfing", "Skiing", "Archery", "Rowing"
        ),
        "Music" to listOf(
            "Guitar", "Piano", "Violin", "Drums", "Trumpet", "Flute", "Cello",
            "Saxophone", "Melody", "Rhythm", "Concert", "Choir", "Microphone", "Speaker",
            "Headset", "Keyboard", "Ukulele", "Banjo", "Harp", "Tambourine"
        ),
        "Nature" to listOf(
            "Forest", "River", "Ocean", "Desert", "Valley", "Canyon", "Island", "Glacier",
            "Waterfall", "Rainbow", "Sunrise", "Sunset", "Cloud", "Breeze", "Thunder",
            "Lightning", "Meadow", "Lagoon", "Volcano", "Dune"
        ),
        "Jobs" to listOf(
            "Doctor", "Nurse", "Teacher", "Chef", "Farmer", "Pilot", "Engineer",
            "Architect", "Artist", "Writer", "Lawyer", "Firefighter", "Detective",
            "Dentist", "Mechanic", "Plumber", "Librarian", "Florist", "Baker", "Carpenter"
        ),
        "Home" to listOf(
            "Kitchen", "Bedroom", "Bathroom", "Balcony", "Garden", "Garage", "Sofa",
            "Armchair", "Curtain", "Pillow", "Blanket", "Toaster", "Kettle", "Fridge",
            "Microwave", "Freezer", "Cabinet", "Table", "Chimney", "Hallway"
        ),
        "Travel" to listOf(
            "Passport", "Ticket", "Backpack", "Map", "Compass", "Taxi", "Bus", "Train",
            "Subway", "Tram", "Ferry", "Cruise", "Hostel", "Hotel", "Postcard",
            "Souvenir", "Luggage", "Route", "Itinerary", "Checkpoint"
        ),
        "School" to listOf(
            "Notebook", "Workbook", "Homework", "Project", "Exam", "Quiz", "Classroom",
            "Lecture", "Teacher", "Student", "Marker", "Chalk", "Backpack", "Lunchbox",
            "Locker", "Semester", "Timetable", "Syllabus", "Essay", "Diploma"
        ),
        "Weather" to listOf(
            "Sunny", "Cloudy", "Rainy", "Windy", "Storm", "Thunder", "Lightning", "Drizzle",
            "Blizzard", "Snowfall", "Frost", "Fog", "Rainbow", "Heatwave", "Drought",
            "Hail", "Breeze", "Forecast", "Humidity", "Monsoon"
        ),
        "Entertainment" to listOf(
            "Movie", "Series", "Cartoon", "Comic", "Puzzle", "Riddle", "Theater", "Festival",
            "Parade", "Museum", "Aquarium", "Podcast", "Radio", "Story", "Poem",
            "Dance", "Ballet", "Magic", "Circus", "Carnival"
        ),
        "Science" to listOf(
            "Atom", "Molecule", "Cell", "Neuron", "Planet", "Galaxy", "Telescope",
            "Microscope", "Gravity", "Orbit", "Energy", "Magnet", "Voltage", "Prism",
            "Fossil", "Lab", "Experiment", "Hypothesis", "Theorem", "Spectrum"
        ),
        "Technology" to listOf(
            "Browser", "Server", "Database", "App", "Website", "Router", "Sensor", "Drone",
            "Console", "Monitor", "Keyboard", "Mouse", "Cloud", "Algorithm", "Code",
            "Password", "Firewall", "Camera", "Processor", "Bluetooth"
        )
    )

    private fun canonicalKey(pair: WordPair): String {
        val a = pair.civilianWord.trim().lowercase()
        val b = pair.undercoverWord.trim().lowercase()
        val ordered = if (a <= b) "$a|$b" else "$b|$a"
        return "${pair.category.lowercase()}|$ordered"
    }

    val allPairs: List<WordPair> = categoryWords.flatMap { (category, rawWords) ->
        val words = rawWords.map { it.trim() }.distinct()
        buildList {
            for (index in words.indices) {
                val first = words[index]
                val second = words[(index + 1) % words.size]
                val third = words[(index + 2) % words.size]
                add(WordPair(category = category, civilianWord = first, undercoverWord = second))
                add(WordPair(category = category, civilianWord = first, undercoverWord = third))
            }
        }
    }
        .filter {
            it.civilianWord.isNotBlank() &&
                it.undercoverWord.isNotBlank() &&
                !it.civilianWord.equals(it.undercoverWord, ignoreCase = true)
        }
        .distinctBy(::canonicalKey)
}
