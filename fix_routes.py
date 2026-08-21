import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace('"beranda"', '"home"')

# Add transactions route
transactions_route = """                composable(
                    route = "transactions",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Transactions Screen - Work In Progress", style = MaterialTheme.typography.titleMedium)
                    }
                }
"""

# Add saving route
saving_route = """                composable(
                    route = "saving",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("Saving Goals Screen - Work In Progress", style = MaterialTheme.typography.titleMedium)
                    }
                }
"""

# Insert them after the home route finishes (around line 350)
home_end = content.find('                composable(\n                    route = "statistics",')
if home_end != -1:
    content = content[:home_end] + transactions_route + saving_route + content[home_end:]

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
