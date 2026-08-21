import sys

# 1. Update InstallmentCard
with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'r') as f:
    ic = f.read()

ic_top = """@Composable
fun InstallmentCard(
    installment: Installment,
    isPiutang: Boolean = false,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {"""
ic = ic.replace("""@Composable
fun InstallmentCard(
    installment: Installment,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {""", ic_top)

# Update the progress text part
progress_target = """            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${installment.paidCount} dari ${installment.totalInstallments} dibayar",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )"""

progress_replacement = """            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isPiutang) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = if (isPiutang) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isPiutang) {
                Text(
                    text = "${FormatUtils.formatRupiah(installment.totalAmount - installment.remainingAmount)} / ${FormatUtils.formatRupiah(installment.totalAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Cicilan ${installment.paidCount} dari ${installment.totalInstallments}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Bayaran ${FormatUtils.formatRupiah(installment.totalAmount - installment.remainingAmount)} / ${FormatUtils.formatRupiah(installment.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }"""

ic = ic.replace(progress_target, progress_replacement)

with open('app/src/main/java/com/example/ui/components/InstallmentCard.kt', 'w') as f:
    f.write(ic)


# 2. Update SavingGoalsPanel
with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'r') as f:
    sg = f.read()

sg_top = """import com.example.data.model.Category

@Composable
fun SavingGoalsPanel(
    installments: List<Installment>,
    savingTargets: List<SavingTarget>,
    accounts: List<Account>,
    categories: List<Category>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingTarget) -> Unit,
    onDeleteGoal: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    onAddInstallment: () -> Unit = {},
    onPayInstallment: (Installment) -> Unit = {}
) {"""

sg = sg.replace("""@Composable
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
) {""", sg_top)

sg_item = """                        items(installments) { installment ->
                            val isPiutang = categories.find { it.id == installment.categoryId }?.type == "PEMASUKAN"
                            InstallmentCard(
                                installment = installment,
                                isPiutang = isPiutang,
                                onPay = { onPayInstallment(installment) }
                            )
                        }"""

sg = sg.replace("""                        items(installments) { installment ->
                            InstallmentCard(
                                installment = installment,
                                onPay = { onPayInstallment(installment) }
                            )
                        }""", sg_item)

with open('app/src/main/java/com/example/ui/screens/SavingGoalsPanel.kt', 'w') as f:
    f.write(sg)

