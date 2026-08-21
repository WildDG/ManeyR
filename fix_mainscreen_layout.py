import sys

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

# Add isBalanceVisible and isUpcomingExpanded
dashboard_top = """fun DashboardPanel(
    installments: List<com.example.data.model.Installment>,
    accounts: List<Account>,
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    categories: List<Category>,
    savingTargets: List<SavingTarget>,
    monthLabel: String,
    totalBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    globalBudgetLimit: Long,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddSavingTarget: (String, Long, String) -> Unit,
    onDeleteSavingTarget: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    monthOffset: Int,
    recurringTransactions: List<RecurringTransaction>,
    onTransfer: (String, String, Long, Long) -> Unit,
    onAdjustAsset: (String, Long, Boolean) -> Unit,
    initialIntentAction: String? = null
) {
    var historyAccountForPopup by remember { mutableStateOf<Account?>(null) }
    var isAddTargetOpen by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }
    var isUpcomingExpanded by remember { mutableStateOf(false) }"""

content = content.replace("""fun DashboardPanel(
    installments: List<com.example.data.model.Installment>,
    accounts: List<Account>,
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    categories: List<Category>,
    savingTargets: List<SavingTarget>,
    monthLabel: String,
    totalBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    globalBudgetLimit: Long,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddSavingTarget: (String, Long, String) -> Unit,
    onDeleteSavingTarget: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    monthOffset: Int,
    recurringTransactions: List<RecurringTransaction>,
    onTransfer: (String, String, Long, Long) -> Unit,
    onAdjustAsset: (String, Long, Boolean) -> Unit,
    initialIntentAction: String? = null
) {
    var historyAccountForPopup by remember { mutableStateOf<Account?>(null) }
    var isAddTargetOpen by remember { mutableStateOf(false) }""", dashboard_top)

# Update Balance blur
balance_target = """                        Text(
                            text = "Total Saldo Gabungan",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = FormatUtils.formatRupiah(totalBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )"""

balance_replacement = """                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Total Saldo Gabungan",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { isBalanceVisible = !isBalanceVisible },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceVisible) androidx.compose.material.icons.filled.Visibility else androidx.compose.material.icons.filled.VisibilityOff,
                                    contentDescription = "Toggle Balance",
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBalanceVisible) FormatUtils.formatRupiah(totalBalance) else "••••••••",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )"""
content = content.replace(balance_target, balance_replacement)

# Update Income blur
income_target = """                                Text(
                                    text = "+ ${FormatUtils.formatRupiah(monthlyIncome)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )"""
income_replacement = """                                Text(
                                    text = if (isBalanceVisible) "+ ${FormatUtils.formatRupiah(monthlyIncome)}" else "••••••••",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )"""
content = content.replace(income_target, income_replacement)

# Update Expense blur
expense_target = """                                Text(
                                    text = "- ${FormatUtils.formatRupiah(monthlyExpense)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )"""
expense_replacement = """                                Text(
                                    text = if (isBalanceVisible) "- ${FormatUtils.formatRupiah(monthlyExpense)}" else "••••••••",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )"""
content = content.replace(expense_target, expense_replacement)

# Update Upcoming Row to be Expandable and blur amount
upcoming_target = """                        Spacer(modifier = Modifier.height(16.dp))
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
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                    color = Color(0xFFFFEBEE)
                                )
                            }
                        }"""

upcoming_replacement = """                        // Expandable toggle button for upcoming
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clickable { isUpcomingExpanded = !isUpcomingExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUpcomingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Upcoming",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(visible = isUpcomingExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
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
                                            text = if (isBalanceVisible) FormatUtils.formatRupiah(upcomingIncome) else "••••••••",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
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
                                            text = if (isBalanceVisible) FormatUtils.formatRupiah(upcomingExpense) else "••••••••",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            color = Color(0xFFFFEBEE)
                                        )
                                    }
                                }
                            }
                        }"""
content = content.replace(upcoming_target, upcoming_replacement)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
