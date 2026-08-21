import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# Calculate upcoming
calc_insert = """
    val upcomingIncome = remember(installments, categories) {
        installments.filter { it.status == "ACTIVE" && (categories.find { cat -> cat.id == it.categoryId }?.type == "PEMASUKAN") }
            .sumOf { it.installmentAmount }
    }
    
    val upcomingExpense = remember(installments, categories) {
        installments.filter { it.status == "ACTIVE" && (categories.find { cat -> cat.id == it.categoryId }?.type != "PEMASUKAN") }
            .sumOf { it.installmentAmount }
    }
"""

content = content.replace("    var isCalendarExpanded by remember { mutableStateOf(false) }", calc_insert + "\n    var isCalendarExpanded by remember { mutableStateOf(false) }")

ui_insert = """
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Upcoming In",
                                        tint = Color(0xFFC8E6C9),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Akan Masuk (Piutang)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatRupiah(upcomingIncome),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE8F5E9)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = "Upcoming Out",
                                        tint = Color(0xFFFFCDD2),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Akan Keluar (Cicilan)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = FormatUtils.formatRupiah(upcomingExpense),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFEBEE)
                                )
                            }
                        }
"""

content = content.replace("                            // End of Income/Expense Row\n                        }", "                            // End of Income/Expense Row\n                        }" + ui_insert)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
