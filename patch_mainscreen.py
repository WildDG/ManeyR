import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# Pass installments to DashboardPanel
db_call = """                    val savingTargets by viewModel.savingTargets.collectAsState()
                    DashboardPanel("""
db_call_new = """                    val savingTargets by viewModel.savingTargets.collectAsState()
                    val installments by viewModel.installments.collectAsState()
                    DashboardPanel(
                        installments = installments,"""
content = content.replace(db_call, db_call_new)

# Update DashboardPanel signature
dp_sig = """fun DashboardPanel(
    accounts: List<Account>,
    transactions: List<Transaction>,"""
dp_sig_new = """fun DashboardPanel(
    installments: List<com.example.data.model.Installment>,
    accounts: List<Account>,
    transactions: List<Transaction>,"""
content = content.replace(dp_sig, dp_sig_new)

# Pass installments to SavingGoalsPanel
sp_call = """                    val savingTargets by viewModel.savingTargets.collectAsState()
                    SavingGoalsPanel(
                        savingTargets = savingTargets,"""
sp_call_new = """                    val savingTargets by viewModel.savingTargets.collectAsState()
                    val installments by viewModel.installments.collectAsState()
                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,"""
content = content.replace(sp_call, sp_call_new)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
