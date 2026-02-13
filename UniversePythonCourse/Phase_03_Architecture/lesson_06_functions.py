# -----------------------------------------------------------------------------
# 🌌 CHAPTER 6: THE FACTORY (Functions)
# -----------------------------------------------------------------------------
# Up to now, we have written code line by line.
# But what if we want to reuse a block of code multiple times?
# We use FUNCTIONS. Think of them as mini-factories that do one specific job.
# -----------------------------------------------------------------------------

print("--- ⚙️ INITIATING LESSON 6: FUNCTIONS ---")

# 1. DEFINING A FUNCTION
# We use the 'def' keyword.
# The code inside the function does NOT run until we "call" the function.

def greet_crew():
    print("Welcome aboard, Captain.")
    print("Systems are online.")

# Calling the function
print("\nScanning bridge...")
greet_crew() 
greet_crew() # We can call it as many times as we want!

# 2. ARGUMENTS (Inputs)
# Functions are more useful if we can give them data.
# These inputs are called "arguments" or "parameters".

def announce_arrival(planet_name):
    print(f"Now arriving at {planet_name}.")

print("\nTraveling...")
announce_arrival("Tatooine")
announce_arrival("Coruscant")

# 3. RETURN VALUES (Outputs)
# Most functions do some work and then give back a result.
# We use the 'return' keyword.

def calculate_hyperspace_jump(distance):
    fuel_needed = distance * 5
    return fuel_needed # Send this value back to whoever asked

# Using the return value
trip_distance = 100
fuel = calculate_hyperspace_jump(trip_distance)
print(f"\nFuel required for {trip_distance} lightyears: {fuel} units")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE DROID FABRICATOR
# -----------------------------------------------------------------------------
# 1. Create a function called 'build_droid' that takes two arguments: 'droid_type' and 'color'.
# 2. Inside the function:
#    - Print a message: "Building a [color] [droid_type]..."
#    - Return a string that combines them, e.g., "Red Astromech".
# 3. Call your function to build a "Blue" "R2 Unit" and save the result in a variable 'my_droid'.
# 4. Print "New droid completed: [my_droid]".
# 5. Call the function again for a "Gold" "Protocol Droid" and print the result directly.
#
# WRITE YOUR CODE BELOW THIS LINE:
def build_droid(droid_type,color):
    print(f"Building a {color} {droid_type}...")
    return f"{color} {droid_type}"
my_droid = build_droid("Blue" ,"R2 Unit")
print(f"New droid completed: {my_droid}")
print(build_droid("Protocol Droid","Gold"))


