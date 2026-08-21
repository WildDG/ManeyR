import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

call_target = """                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,
                        accounts = accounts,"""

call_replacement = """                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,
                        accounts = accounts,
                        categories = categories,"""

content = content.replace(call_target, call_replacement)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
