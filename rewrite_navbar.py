import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace("ActiveTab.BERANDA", "ActiveTab.HOME")
content = content.replace("ActiveTab.STATISTIK", "ActiveTab.STATISTICS")
content = content.replace("ActiveTab.PENGATURAN", "ActiveTab.SETTINGS")

# Now let's replace the whole NavigationBar block
nav_bar_start = content.find("NavigationBar(")
nav_bar_end_search = content.find("        floatingActionButton = {", nav_bar_start)

if nav_bar_start != -1 and nav_bar_end_search != -1:
    old_nav_bar = content[nav_bar_start:nav_bar_end_search]
    new_nav_bar = """NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .height(64.dp),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == ActiveTab.HOME,
                    onClick = { activeTab = ActiveTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    modifier = Modifier.testTag("nav_home"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.TRANSACTIONS,
                    onClick = { activeTab = ActiveTab.TRANSACTIONS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                    modifier = Modifier.testTag("nav_transactions"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.STATISTICS,
                    onClick = { activeTab = ActiveTab.STATISTICS },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Statistics") },
                    modifier = Modifier.testTag("nav_statistics"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.SAVING,
                    onClick = { activeTab = ActiveTab.SAVING },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Saving") },
                    modifier = Modifier.testTag("nav_saving"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.SETTINGS,
                    onClick = { activeTab = ActiveTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    modifier = Modifier.testTag("nav_settings"),
                    alwaysShowLabel = false
                )
            }
        },
"""
    content = content.replace(old_nav_bar, new_nav_bar)

# Fix FloatingActionButton visibility
content = content.replace("activeTab != ActiveTab.STATISTICS && activeTab != ActiveTab.SETTINGS", "activeTab == ActiveTab.HOME || activeTab == ActiveTab.TRANSACTIONS")

# Update imports to make sure Icons.Default.List and Icons.Default.Savings are available
if "import androidx.compose.material.icons.filled.List" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Home", "import androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.List\nimport androidx.compose.material.icons.filled.Savings")


with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
