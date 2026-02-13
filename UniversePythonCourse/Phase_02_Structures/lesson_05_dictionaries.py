# -----------------------------------------------------------------------------
# 🌌 CHAPTER 5: THE GREAT LIBRARY (Dictionaries)
# -----------------------------------------------------------------------------
# You have mastered the List (ordered data).
# Now, meet the Dictionary. It is a collection of Key-Value pairs.
# Think of it like a real dictionary: You look up a "Word" (Key) to find its "Definition" (Value).
# -----------------------------------------------------------------------------

print("--- 📖 INITIATING LESSON 5: DICTIONARIES ---")

# 1. THE DICTIONARY (Key-Value Pairs)
# Dictionaries use curly braces {}.
# Format: {KEY : VALUE, KEY : VALUE}

spaceship = {
    "name": "Millennium Falcon",
    "captain": "Han Solo",
    "speed": "12 parsecs",
    "shields": 500
}

print(f"\nShip Data: {spaceship}")

# 2. ACCESSING DATA (The Lookup)
# We don't use numbers (0, 1) here. We use the KEY names.

print(f"Captain: {spaceship['captain']}")
print(f"Speed: {spaceship['speed']}")

# 3. MODIFYING THE LIBRARY
# Dictionaries are mutable. We can change values, add new keys, or delete them.

# Updating a value
spaceship["shields"] = 400
print(f"Shields dropped to: {spaceship['shields']}")

# Adding a new key-value pair
spaceship["cargo"] = "Smuggled Goods"
print(f"Added Cargo: {spaceship}")

# Deleting a key
del spaceship["speed"] # The hyperdrive is broken!
print(f"After damage: {spaceship}")

# 4. LOOPING THROUGH A DICTIONARY
# We can loop through Keys, Values, or Both.

print("\nScanning Ship Status:")
for key, value in spaceship.items():
    print(f"{key.capitalize()}: {value}")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE GALACTIC BESTIARY
# -----------------------------------------------------------------------------
# 1. Create a dictionary called 'alien_codex'.
# 2. Add two entries:
#    - Key: "Yoda", Value: "Unknown Species"
#    - Key: "Chewbacca", Value: "Wookiee"
#    - Key: "Jabba", Value: "Hutt"
# 3. Print "Yoda is a [Value]" using the dictionary to get the species.
# 4. Add a new entry: Key: "Ewok", Value: "Teddy Bear"
# 5. Yoda's species has been discovered! Change "Yoda"s value to "Grogu's Dad" (or whatever you like).
# 6. Print the entire 'alien_codex' dictionary.
#
# WRITE YOUR CODE BELOW THIS LINE:
alien_codex ={
    "Yoda" : "Unknown Species",
    "Chewbacca" : "Wookiee",
    "Jabba" : "Hutt"
}
print(f"Yoda is a {alien_codex['Yoda']}")
alien_codex["Ewok"]="Teddy Bear"
alien_codex["Yoda"]="Grogu's Dad"
print(f"alien_codex : {alien_codex}")