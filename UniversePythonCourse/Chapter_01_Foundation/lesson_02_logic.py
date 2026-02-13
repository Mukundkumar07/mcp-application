# -----------------------------------------------------------------------------
# 🌌 CHAPTER 2: THE ART OF LOGIC (Booleans & Decisions)
# -----------------------------------------------------------------------------
# Computers are binary machines. Everything is either 1 (True) or 0 (False).
# In this lesson, we give your program the power to DECIDE.
# -----------------------------------------------------------------------------

print("--- 🟡 INITIATING LESSON 2: LOGIC & DECISIONS ---")

# 1. THE BOOLEAN TYPE
# A boolean variable can only hold two values: True or False.
# Note: The 'T' and 'F' must be capitalized!

is_jedi = True
is_sith = False

print(f"Is Jedi? {is_jedi}")

# 2. COMPARISON OPERATORS (Asking Questions)
# We can ask the computer to compare things. The answer is always a Boolean.

health = 50

print(f"Is health equal to 100? {health == 100}")  # == checks for equality
print(f"Is health greater than 0? {health > 0}")    # > checks greater than
print(f"Is health not 50? {health != 50}")          # != checks "not equal"

# 3. IF / ELSE (The Fork in the Road)
# This is how we make the code do different things based on the situation.

shield_integrity = 20

print(f"\nIncoming attack! Shield is at {shield_integrity}%")

if shield_integrity > 10:
    print("✅ Shields holding. No damage taken.")
else:
    print("🚨 WARNING: SHIELDS CRITICAL. HULL DAMAGE IMMINENT!")

# 4. LOGICAL GATES (AND, OR, NOT)
# and: BOTH must be True
# or:  AT LEAST ONE must be True
# not: Reverses the value

has_ticket = True
is_vip = False

if has_ticket or is_vip:
    print("\nWelcome to the Space Station party! 🥳")
else:
    print("\nAccess Denied. 👮")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE LAUNCH SEQUENCE
# -----------------------------------------------------------------------------
# 1. Create a variable 'fuel_level' and set it to any number you want (0-100).
# 2. Create a boolean variable 'systems_ready' and set it to True or False.
# 3. Write an 'if' statement that checks:
#    IF fuel is greater than 50 AND systems are ready:
#        Print "🚀 Launch initiated! To infinity..."
#    ELSE:
#        Print "❌ Launch aborted. Check systems."
#
# WRITE YOUR CODE BELOW THIS LINE:
fuel_level = 100
system_ready=True
if fuel_level >50 and system_ready:
    print("🚀 Launch initiated! To infinity...")
else:
    print("❌ Launch aborted. Check systems.")
