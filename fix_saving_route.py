import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

dummy_route = """                composable(
                    route = "saving",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Saving Goals Screen - Work In Progress", style = MaterialTheme.typography.titleMedium)
                    }
                }"""

real_route = """                composable(
                    route = "saving",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    val savingTargets by viewModel.savingTargets.collectAsState()
                    SavingGoalsPanel(
                        savingTargets = savingTargets,
                        accounts = accounts,
                        onAddGoal = { isAddTargetOpen = true },
                        onEditGoal = { /* TODO */ },
                        onDeleteGoal = { viewModel.deleteSavingTarget(it) },
                        onSaveToTarget = { targetId, acctId, amt -> viewModel.saveToTarget(targetId, acctId, amt) }
                    )
                }"""

content = content.replace(dummy_route, real_route)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
