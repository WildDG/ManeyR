import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

target = """    if (isAddInstallmentOpen) {
        AddInstallmentDialog("""

replacement = """    if (isAddTargetOpen) {
        AddSavingTargetDialog(
            accounts = accounts,
            onDismiss = { isAddTargetOpen = false },
            onSave = { name, amount, sourceId ->
                viewModel.addSavingTarget(name, amount, sourceId)
                isAddTargetOpen = false
            }
        )
    }

    if (isAddInstallmentOpen) {
        AddInstallmentDialog("""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
