import sys

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'r') as f:
    content = f.read()

else_block = """            } else {
                if (installments.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Belum ada Cicilan atau Piutang",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(installments) { installment ->
                            InstallmentCard(
                                installment = installment,
                                onPay = { onPayInstallment(installment) }
                            )
                        }
                    }
                }
            }
"""

# Find where the `TABUNGAN` if ends. Let's see: 
# It ends right before `FloatingActionButton`
content = content.replace("        }\n        \n        FloatingActionButton", else_block + "        }\n        \n        FloatingActionButton")

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'w') as f:
    f.write(content)
