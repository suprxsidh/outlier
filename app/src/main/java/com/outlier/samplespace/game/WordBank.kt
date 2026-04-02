package com.outlier.samplespace.game

object WordBank {
    private val basePairs: List<WordPair> = listOf(
        WordPair("Food", "Pizza", "Calzone"),
        WordPair("Food", "Burger", "Sandwich"),
        WordPair("Food", "Pasta", "Noodles"),
        WordPair("Food", "Sushi", "Sashimi"),
        WordPair("Food", "Taco", "Burrito"),
        WordPair("Food", "Cake", "Cupcake"),
        WordPair("Food", "Cookie", "Biscuit"),
        WordPair("Food", "Muffin", "Bagel"),
        WordPair("Food", "Orange", "Mandarin"),
        WordPair("Food", "Lemon", "Lime"),
        WordPair("Food", "Apple", "Pear"),
        WordPair("Food", "Tea", "Coffee"),

        WordPair("Animals", "Dog", "Wolf"),
        WordPair("Animals", "Cat", "Fox"),
        WordPair("Animals", "Horse", "Donkey"),
        WordPair("Animals", "Sheep", "Goat"),
        WordPair("Animals", "Duck", "Goose"),
        WordPair("Animals", "Frog", "Toad"),
        WordPair("Animals", "Shark", "Dolphin"),
        WordPair("Animals", "Lion", "Tiger"),
        WordPair("Animals", "Rabbit", "Hare"),
        WordPair("Animals", "Bee", "Wasp"),
        WordPair("Animals", "Eagle", "Hawk"),
        WordPair("Animals", "Whale", "Seal"),

        WordPair("Places", "Airport", "Train Station"),
        WordPair("Places", "Beach", "Lake"),
        WordPair("Places", "Library", "Bookstore"),
        WordPair("Places", "Museum", "Gallery"),
        WordPair("Places", "Park", "Playground"),
        WordPair("Places", "Hospital", "Clinic"),
        WordPair("Places", "School", "College"),
        WordPair("Places", "Farm", "Ranch"),
        WordPair("Places", "Mountain", "Hill"),
        WordPair("Places", "Desert", "Beach"),
        WordPair("Places", "Cinema", "Theater"),
        WordPair("Places", "Bakery", "Cafe"),

        WordPair("Objects", "Laptop", "Tablet"),
        WordPair("Objects", "Phone", "Walkie Talkie"),
        WordPair("Objects", "Backpack", "Suitcase"),
        WordPair("Objects", "Pencil", "Pen"),
        WordPair("Objects", "Chair", "Stool"),
        WordPair("Objects", "Clock", "Watch"),
        WordPair("Objects", "Mirror", "Window"),
        WordPair("Objects", "Camera", "Camcorder"),
        WordPair("Objects", "Blanket", "Towel"),
        WordPair("Objects", "Soap", "Shampoo"),
        WordPair("Objects", "Helmet", "Hat"),
        WordPair("Objects", "Umbrella", "Raincoat"),

        WordPair("Sports", "Soccer", "Rugby"),
        WordPair("Sports", "Tennis", "Badminton"),
        WordPair("Sports", "Baseball", "Cricket"),
        WordPair("Sports", "Basketball", "Netball"),
        WordPair("Sports", "Hockey", "Lacrosse"),
        WordPair("Sports", "Skating", "Skiing"),
        WordPair("Sports", "Surfing", "Sailing"),
        WordPair("Sports", "Boxing", "Wrestling"),
        WordPair("Sports", "Cycling", "Running"),
        WordPair("Sports", "Golf", "Mini Golf"),
        WordPair("Sports", "Yoga", "Pilates"),
        WordPair("Sports", "Chess", "Checkers"),

        WordPair("Music", "Guitar", "Ukulele"),
        WordPair("Music", "Piano", "Keyboard"),
        WordPair("Music", "Violin", "Viola"),
        WordPair("Music", "Drums", "Bongo"),
        WordPair("Music", "Trumpet", "Trombone"),
        WordPair("Music", "Song", "Lullaby"),
        WordPair("Music", "Choir", "Band"),
        WordPair("Music", "Concert", "Recital"),
        WordPair("Music", "Melody", "Rhythm"),
        WordPair("Music", "Headphones", "Earbuds"),
        WordPair("Music", "DJ", "Conductor"),
        WordPair("Music", "Microphone", "Speaker"),

        WordPair("Nature", "Forest", "Jungle"),
        WordPair("Nature", "River", "Stream"),
        WordPair("Nature", "Ocean", "Sea"),
        WordPair("Nature", "Cloud", "Fog"),
        WordPair("Nature", "Rain", "Snow"),
        WordPair("Nature", "Thunder", "Lightning"),
        WordPair("Nature", "Volcano", "Mountain"),
        WordPair("Nature", "Island", "Peninsula"),
        WordPair("Nature", "Tree", "Bush"),
        WordPair("Nature", "Flower", "Tulip"),
        WordPair("Nature", "Sun", "Moon"),
        WordPair("Nature", "Wind", "Breeze"),

        WordPair("Jobs", "Doctor", "Nurse"),
        WordPair("Jobs", "Teacher", "Tutor"),
        WordPair("Jobs", "Chef", "Baker"),
        WordPair("Jobs", "Farmer", "Gardener"),
        WordPair("Jobs", "Pilot", "Driver"),
        WordPair("Jobs", "Engineer", "Architect"),
        WordPair("Jobs", "Actor", "Comedian"),
        WordPair("Jobs", "Writer", "Editor"),
        WordPair("Jobs", "Painter", "Photographer"),
        WordPair("Jobs", "Dentist", "Surgeon"),
        WordPair("Jobs", "Firefighter", "Paramedic"),
        WordPair("Jobs", "Police", "Detective"),

        WordPair("Home", "Kitchen", "Dining Room"),
        WordPair("Home", "Bedroom", "Guest Room"),
        WordPair("Home", "Sofa", "Armchair"),
        WordPair("Home", "Lamp", "Lantern"),
        WordPair("Home", "Broom", "Vacuum"),
        WordPair("Home", "Fridge", "Freezer"),
        WordPair("Home", "Oven", "Microwave"),
        WordPair("Home", "Plate", "Bowl"),
        WordPair("Home", "Fork", "Spoon"),
        WordPair("Home", "Door", "Window"),
        WordPair("Home", "Garden", "Yard"),
        WordPair("Home", "Balcony", "Porch"),

        WordPair("Travel", "Passport", "Visa"),
        WordPair("Travel", "Map", "Globe"),
        WordPair("Travel", "Ticket", "Boarding Pass"),
        WordPair("Travel", "Taxi", "Bus"),
        WordPair("Travel", "Subway", "Tram"),
        WordPair("Travel", "Hostel", "Hotel"),
        WordPair("Travel", "Cruise", "Ferry"),
        WordPair("Travel", "Suitcase", "Duffel Bag"),
        WordPair("Travel", "Compass", "GPS"),
        WordPair("Travel", "Postcard", "Souvenir"),
        WordPair("Travel", "Camping", "Glamping"),
        WordPair("Travel", "Road Trip", "Picnic"),

        WordPair("School", "Notebook", "Workbook"),
        WordPair("School", "Homework", "Project"),
        WordPair("School", "Exam", "Quiz"),
        WordPair("School", "Classroom", "Lecture Hall"),
        WordPair("School", "Science", "Math"),
        WordPair("School", "History", "Geography"),
        WordPair("School", "Lunchbox", "Backpack"),
        WordPair("School", "Marker", "Chalk"),
        WordPair("School", "Principal", "Counselor"),
        WordPair("School", "Uniform", "Badge"),
        WordPair("School", "Library Card", "ID Card"),
        WordPair("School", "Semester", "Term"),

        WordPair("Weather", "Sunny", "Cloudy"),
        WordPair("Weather", "Storm", "Hurricane"),
        WordPair("Weather", "Drizzle", "Rain"),
        WordPair("Weather", "Blizzard", "Snowfall"),
        WordPair("Weather", "Thunder", "Hail"),
        WordPair("Weather", "Rainbow", "Sunset"),
        WordPair("Weather", "Forecast", "Prediction"),
        WordPair("Weather", "Thermometer", "Barometer"),
        WordPair("Weather", "Umbrella", "Poncho"),
        WordPair("Weather", "Heatwave", "Drought"),
        WordPair("Weather", "Breeze", "Gust"),
        WordPair("Weather", "Frost", "Ice"),

        WordPair("Entertainment", "Movie", "Series"),
        WordPair("Entertainment", "Cartoon", "Comic"),
        WordPair("Entertainment", "Puzzle", "Riddle"),
        WordPair("Entertainment", "Board Game", "Card Game"),
        WordPair("Entertainment", "Magic", "Illusion"),
        WordPair("Entertainment", "Circus", "Carnival"),
        WordPair("Entertainment", "Festival", "Parade"),
        WordPair("Entertainment", "Museum", "Aquarium"),
        WordPair("Entertainment", "Podcast", "Radio"),
        WordPair("Entertainment", "Diary", "Journal"),
        WordPair("Entertainment", "Story", "Poem"),
        WordPair("Entertainment", "Dance", "Ballet")
    )

    private val modifiers = listOf(
        "Amber", "Autumn", "Azure", "Bamboo", "Berry", "Blossom", "Breezy", "Bright",
        "Calm", "Cedar", "Cherry", "Cloudy", "Coral", "Cozy", "Crimson", "Crystal",
        "Dawn", "Dewy", "Dreamy", "Dune", "Echo", "Emerald", "Feather", "Fern",
        "Floral", "Forest", "Frost", "Garden", "Gentle", "Golden", "Harbor", "Harmony",
        "Hazel", "Honey", "Ivy", "Jade", "Jolly", "Lagoon", "Lavender", "Lilac",
        "Lively", "Maple", "Marble", "Meadow", "Melody", "Mint", "Misty", "Moon",
        "Morning", "Moss", "Ocean", "Olive", "Opal", "Orchid", "Pebble", "Petal",
        "Pine", "Placid", "Prairie", "Quartz", "Rainy", "River", "Rose", "Ruby",
        "Saffron", "Sandy", "Scarlet", "Seaside", "Silky", "Silver", "Sky", "Soft",
        "Solar", "Spark", "Spring", "Star", "Stone", "Summer", "Sunny", "Sunset",
        "Swift", "Teal", "Thistle", "Timber", "Tranquil", "Tulip", "Velvet", "Violet",
        "Warm", "Willow", "Windy", "Winter", "Woodland", "Yonder", "Zephyr", "Zesty"
    )

    private fun modifierFor(index: Int): String {
        val first = modifiers[index % modifiers.size]
        val second = modifiers[(index / modifiers.size) % modifiers.size]
        return if (index < modifiers.size) first else "$second $first"
    }

    val allPairs: List<WordPair> = buildList {
        var index = 0
        while (size < 650) {
            val seed = basePairs[index % basePairs.size]
            val modifier = modifierFor(index)
            add(
                WordPair(
                    category = seed.category,
                    civilianWord = "${seed.civilianWord} $modifier",
                    undercoverWord = "${seed.undercoverWord} $modifier"
                )
            )
            index += 1
        }
    }
}
