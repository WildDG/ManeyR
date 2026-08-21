import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Replace onClick handlers in NavigationBar
replacements = {
    "onClick = { activeTab = ActiveTab.HOME }": """onClick = { 
                        if (activeTab != ActiveTab.HOME) {
                            activeTab = ActiveTab.HOME
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }""",
    "onClick = { activeTab = ActiveTab.TRANSACTIONS }": """onClick = { 
                        if (activeTab != ActiveTab.TRANSACTIONS) {
                            activeTab = ActiveTab.TRANSACTIONS
                            navController.navigate("transactions") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }""",
    "onClick = { activeTab = ActiveTab.STATISTICS }": """onClick = { 
                        if (activeTab != ActiveTab.STATISTICS) {
                            activeTab = ActiveTab.STATISTICS
                            navController.navigate("statistics") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }""",
    "onClick = { activeTab = ActiveTab.SAVING }": """onClick = { 
                        if (activeTab != ActiveTab.SAVING) {
                            activeTab = ActiveTab.SAVING
                            navController.navigate("saving") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }""",
    "onClick = { activeTab = ActiveTab.SETTINGS }": """onClick = { 
                        if (activeTab != ActiveTab.SETTINGS) {
                            activeTab = ActiveTab.SETTINGS
                            navController.navigate("settings") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }"""
}

for old, new in replacements.items():
    content = content.replace(old, new)

# Change startDestination to home
content = content.replace('startDestination = "beranda"', 'startDestination = "home"')
content = content.replace('route = "beranda"', 'route = "home"')
content = content.replace('route = "statistik"', 'route = "statistics"')
content = content.replace('route = "pengaturan"', 'route = "settings"')

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
