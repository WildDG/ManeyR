import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Add isAddTargetOpen to MainScreen state
main_screen_state = "    var txToEdit by remember { mutableStateOf<Transaction?>(null) }"
new_main_screen_state = main_screen_state + "\n    var isAddTargetOpen by remember { mutableStateOf(false) }"
content = content.replace(main_screen_state, new_main_screen_state)

# And inject the AddSavingTargetSheet right after TransactionSheet
tx_sheet_search = "        if (isAddSheetOpen || txToEdit != null) {"
target_sheet = """
        if (isAddTargetOpen) {
            AddSavingTargetSheet(
                accounts = accounts,
                onDismiss = { isAddTargetOpen = false },
                onSave = { name, amt, acctId ->
                    viewModel.addSavingTarget(name, amt, acctId)
                    isAddTargetOpen = false
                }
            )
        }
"""
content = content.replace(tx_sheet_search, target_sheet + "\n" + tx_sheet_search)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
