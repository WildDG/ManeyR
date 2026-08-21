import sys

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    print(f"{i+1}: {line}", end="")
