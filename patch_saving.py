import sys

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'r') as f:
    content = f.read()

# Add Installment import
content = content.replace("import com.example.data.model.SavingTarget", "import com.example.data.model.SavingTarget\nimport com.example.data.model.Installment")

# Update signature
old_sig = """@Composable
fun SavingGoalsPanel(
    savingTargets: List<SavingTarget>,
    accounts: List<Account>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingTarget) -> Unit,
    onDeleteGoal: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit
) {"""

new_sig = """@Composable
fun SavingGoalsPanel(
    installments: List<Installment>,
    savingTargets: List<SavingTarget>,
    accounts: List<Account>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingTarget) -> Unit,
    onDeleteGoal: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    onAddInstallment: () -> Unit = {},
    onPayInstallment: (Installment) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("TABUNGAN") }
"""

content = content.replace(old_sig, new_sig)

# Add tabs and switch content
old_body = """    Box(modifier = Modifier.fillMaxSize()) {
        if (savingTargets.isEmpty()) {"""

new_body = """    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (activeTab == "TABUNGAN") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == "TABUNGAN",
                onClick = { activeTab = "TABUNGAN" },
                text = { Text("Tabungan") }
            )
            Tab(
                selected = activeTab == "CICILAN",
                onClick = { activeTab = "CICILAN" },
                text = { Text("Cicilan & Piutang") }
            )
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (activeTab == "TABUNGAN") {
                if (savingTargets.isEmpty()) {"""

content = content.replace(old_body, new_body)

old_fab = """        FloatingActionButton(
            onClick = onAddGoal,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
        }
    }"""

new_fab = """        FloatingActionButton(
            onClick = if (activeTab == "TABUNGAN") onAddGoal else onAddInstallment,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = if (activeTab == "TABUNGAN") "Add Goal" else "Add Installment")
        }
    }
    }"""

content = content.replace(old_fab, new_fab)

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'w') as f:
    f.write(content)
