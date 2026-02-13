# -----------------------------------------------------------------------------
# 🌌 CHAPTER 4: THE SCROLL & THE TOME (Lists & Tuples)
# -----------------------------------------------------------------------------
# You have learned to store single items in variables.
# But what if you need to carry an entire inventory?
# That is where LISTS come in.
# -----------------------------------------------------------------------------

print("--- 📜 INITIATING LESSON 4: LISTS & TUPLES ---")

# 1. THE LIST (A Mutable Scroll)
# A list is an ordered collection of items. You can add, remove, and change items.
# Lists are defined with square brackets [].

inventory = ["Potion", "Sword", "Shield", "Map"]
print(f"\nOriginal Inventory: {inventory}")

# 2. INDEXING (Reading the Scroll)
# Computers start counting at 0.
# "Potion" is at index 0. "Sword" is at index 1.

print(f"First item: {inventory[0]}")
print(f"Last item: {inventory[-1]}")  # -1 gives the last item!

# 3. MODIFYING THE LIST (Writing to the Scroll)

inventory.append("Key")         # Add to the end
print(f"Added Key: {inventory}")

inventory[1] = "Laser Sword"    # Upgrade the sword!
print(f"Upgraded weapon: {inventory}")

inventory.remove("Potion")      # Use the potion
print(f"Used Potion: {inventory}")

# 4. THE TUPLE (The Stone Tablet)
# A tuple is like a list, but it CANNOT be changed (immutable).
# Tuples are defined with parentheses ().
# Use them for things that should never change, like coordinates.

coordinates = (10, 20)
print(f"\nDestination Coordinates: {coordinates}")
# coordinates[0] = 50  <-- This would cause an ERROR! You cannot change a stone tablet.

# 5. ITERATING THROUGH A LIST (Reading the whole Scroll)

print("\nReviewing gear:")
for item in inventory:
    print(f"- {item}")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE BLACKSMITH'S ORDER
# -----------------------------------------------------------------------------
# 1. Create a list called 'blacksmith_requests' with these items: "Iron", "Coal", "Wood".
# 2. Print the list.
# 3. Add "Steel" to the end of the list.
# 4. Change the SECOND item ("Coal") to "Diamond". (Remember: 0 is first, 1 is second).
# 5. Remove the FIRST item ("Iron").
# 6. Print the final list.
#
# WRITE YOUR CODE BELOW THIS LINE:
