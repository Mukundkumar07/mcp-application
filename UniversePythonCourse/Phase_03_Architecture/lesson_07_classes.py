# -----------------------------------------------------------------------------
# 🌌 CHAPTER 7: THE BLUEPRINTS (Classes & Objects)
# -----------------------------------------------------------------------------
# We have variables (data) and functions (actions).
# What if we want to combine them?
# A CLASS is a blueprint for creating OBJECTS.
# An OBJECT is a specific instance of that class.
# -----------------------------------------------------------------------------

print("--- 🏗️ INITIATING LESSON 7: CLASSES & OBJECTS ---")

# 1. DEFINING A CLASS (The Blueprint)
# We use 'class' keyword. By convention, class names are Capitalized.

class Spaceship:
    # The Constructor: Run automatically when we create a new object
    def __init__(self, name, fuel):
        self.name = name          # Attribute (Data)
        self.fuel = fuel          # Attribute (Data)
        self.is_flying = False    # Default Attribute
    
    # A Method (Action)
    def launch(self):
        if self.fuel > 0:
            self.is_flying = True
            self.fuel -= 10
            print(f"🚀 {self.name} has launched! Fuel: {self.fuel}")
        else:
            print(f"❌ {self.name} has no fuel!")
            
    # Another Method
    def status_report(self):
        print(f"[{self.name}] Fuel: {self.fuel} | Flying: {self.is_flying}")

# 2. CREATING OBJECTS (The Real Things)
# We use the class name like a factory function.

ship_1 = Spaceship("Falcon", 100)
ship_2 = Spaceship("X-Wing", 50)

# 3. USING OBJECTS
# We can access their data and make them do things.

ship_1.status_report()
ship_2.status_report()

print("\nLaunching ships...")
ship_1.launch()
ship_2.launch()

# Notice: ship_1's fuel went down, but ship_2's fuel stayed the same.
# They are independent objects!

ship_1.status_report()
ship_2.status_report()

# -----------------------------------------------------------------------------
# 🧠 YOUR CHALLENGE: THE JEDI ORDER
# -----------------------------------------------------------------------------
# 1. Create a class called 'Jedi'.
# 2. In the __init__ method, setup these attributes:
#    - self.name (passed in)
#    - self.force_level (passed in)
#    - self.is_sith (set strictly to False)
# 3. Add a method called 'train()':
#    - It should increase self.force_level by 10.
#    - Print "{name} is training! Force Level: {force_level}"
# 4. Create two Jedi objects: "Obi-Wan" (Level 100) and "Anakin" (Level 80).
# 5. Make Anakin train 3 times.
# 6. Print the dangerous question: Is Anakin's force level now higher than Obi-Wan's?
#
# WRITE YOUR CODE BELOW THIS LINE:
