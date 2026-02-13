# -----------------------------------------------------------------------------
# 🌌 CHAPTER 1: THE CONTAINER CONCEPT (Variables)
# -----------------------------------------------------------------------------
# Welcome. In this lesson, you will learn how computers "remember" things.
# Use this file to study. Read the comments carefully.
# -----------------------------------------------------------------------------

print("--- 🟢 INITIATING LESSON 1: VARIABLES ---")

# 1. WHAT IS A VARIABLE?
# Imagine a vast warehouse. A "variable" is simply a labelled box in that warehouse.
# You can put things in the box, and you can take them out.

# Here, we create a box labelled 'hero_name' and put the text "Iron Man" inside it.
hero_name = "Iron Man" 

# Now, we ask Python to look inside the box and show us what's there.
print(f"The hero is: {hero_name}")

# 2. THE DYNAMIC NATURE OF PYTHON
# Unlike some strict languages, Python boxes are magic. They can change shape.
# We can take "Iron Man" out and put a number in instead.

power_level = 100        # Initially an Integer (whole number)
print(f"Power Level: {power_level}")

power_level = 99.5       # Now it's a Float (decimal number)
print(f"Power Level adjusted: {power_level}")

# 3. VARIABLE NAMING RULES (The Laws of the Universe)
# - No spaces (use_underscores)
# - Cannot start with a number (1st_place is BAD, place_1 is GOOD)
# - Case sensitive (Energy is different from energy)

energy = 50
Energy = 9000
print(f"Small energy: {energy}")
print(f"BIG ENERGY: {Energy}")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE
# -----------------------------------------------------------------------------
# 1. Create a variable called 'universe_age' and set it to 14000000000 (14 billion).
# 2. Create a variable called 'creator' and put your name in it.
# 3. Print a sentence like: "Mukund created the universe 14000000000 years ago."
# 
# WRITE YOUR CODE BELOW THIS LINE:

universe_age = 14000000000
creator = "Mukund"
print(f"{creator} created the universe {universe_age} years ago.")


