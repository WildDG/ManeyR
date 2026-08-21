import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Replace dummy transactions route with TransactionHistoryPanel
dummy_route = """                composable(
                    route = "transactions",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Transactions Screen - Work In Progress", style = MaterialTheme.typography.titleMedium)
                    }
                }"""

real_route = """                composable(
                    route = "transactions",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    TransactionHistoryPanel(
                        transactions = allTransactions,
                        categories = categories,
                        accounts = accounts,
                        onEditTransaction = { 
                            txToEdit = it
                            isAddSheetOpen = true 
                        },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }"""

content = content.replace(dummy_route, real_route)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
