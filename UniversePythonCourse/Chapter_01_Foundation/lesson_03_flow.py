import time

# -----------------------------------------------------------------------------
# 🌌 CHAPTER 3: THE FLOW OF TIME (Loops & Control Flow)
# -----------------------------------------------------------------------------
# Programs usually run from top to bottom. But what if we want to repeat something?
# Or what if we want to wait for something to happen? That is "Control Flow".
# -----------------------------------------------------------------------------

print("--- 🔵 INITIATING LESSON 3: LOOPS & FLOW ---")

# 1. THE 'FOR' LOOP (Iterating over a sequence)
# Imagine a checklist. You go through items one by one.
# That is a 'for' loop.

print("\nCounting down the launch sequence:")
for number in range(1, 6): # range(1, 6) gives numbers 1, 2, 3, 4, 5 (up to but not including 6)
    print(f"T-minus {number}...")

# 2. THE 'WHILE' LOOP (Repeating until a condition changes)
# Imagine waiting for a download to finish. You wait WHILE it is not done.
# Be careful! If the condition never changes, you get an INFINITE LOOP.

battery_charge = 0
print("\nCharging battery...")

while battery_charge < 100:
    battery_charge = battery_charge + 25 # Add 25% charge
    print(f"Charge at {battery_charge}%")

print("Battery Full! 🔋")

# 3. CONTROLLING THE FLOW (Break & Continue)

print("\nSearching for the golden ticket...")
for ticket_number in range(1, 11):
    if ticket_number == 5:
        print("🎉 FOUND IT! Ticket #5 is the winner!")
        break # STOP the loop immediately. We found what we wanted.
    
    print(f"Checking ticket #{ticket_number}... nope.")

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE COUNTDOWN
# -----------------------------------------------------------------------------
# 1. Create a variable called 'countdown' and set it to 10.
# 2. Write a 'while' loop that runs as long as 'countdown' is greater than 0.
# 3. Inside the loop:
#    - Print the current number.
#    - Subtract 1 from 'countdown'.
# 4. After the loop finishes (outside the loop), print "Liftoff! 🚀".
#
# EXTRA CREDIT: Use a 'time.sleep(1)' inside the loop to make it count down in real-time!
# (You'll need to add 'import time' at the top of the file for this)
#
# WRITE YOUR CODE BELOW THIS LINE:
countdown = 10
while countdown > 0:
   print(countdown)
   countdown = countdown - 1
   time.sleep(1)

print("Liftoff! 🚀")
