with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# Update Enum
content = content.replace("enum class ActiveTab {\n    BERANDA, STATISTIK, PENGATURAN\n}", "enum class ActiveTab {\n    HOME, TRANSACTIONS, STATISTICS, SAVING, SETTINGS\n}")

# Update TopAppBar Title
content = content.replace('title = { Text("Keuanganku", style = MaterialTheme.typography.titleLarge) }', '''title = { 
                Text(
                    text = when(activeTab) {
                        ActiveTab.HOME -> "Home"
                        ActiveTab.TRANSACTIONS -> "Transactions"
                        ActiveTab.STATISTICS -> "Statistics"
                        ActiveTab.SAVING -> "Saving Goals"
                        ActiveTab.SETTINGS -> "Settings"
                    }, 
                    style = MaterialTheme.typography.titleLarge
                ) 
            }''')

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
