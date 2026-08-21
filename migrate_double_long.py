import os
import re

def replace_in_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Special handling for PreferencesManager
    if 'PreferencesManager.kt' in filepath:
        content = content.replace("StateFlow<Double>", "StateFlow<Long>")
        content = content.replace("fun setGlobalBudgetLimit(limit: Double)", "fun setGlobalBudgetLimit(limit: Long)")
        content = content.replace("private fun getGlobalBudgetLimitFromPrefs(): Double", "private fun getGlobalBudgetLimitFromPrefs(): Long")
        content = content.replace("getFloat(\"GLOBAL_BUDGET_LIMIT\", 0f).toDouble()", "getLong(\"GLOBAL_BUDGET_LIMIT\", 0L)")
        content = content.replace("putFloat(\"GLOBAL_BUDGET_LIMIT\", limit.toFloat())", "putLong(\"GLOBAL_BUDGET_LIMIT\", limit)")

    # Replacements for Models
    content = content.replace("val amount: Double", "val amount: Long")
    content = content.replace("val balance: Double", "val balance: Long")
    content = content.replace("val budgetLimit: Double = 0.0", "val budgetLimit: Long = 0L")
    content = content.replace("val targetAmount: Double", "val targetAmount: Long")
    content = content.replace("val currentAmount: Double = 0.0", "val currentAmount: Long = 0L")

    # FormatUtils
    content = content.replace("fun formatRupiah(amount: Double)", "fun formatRupiah(amount: Long)")

    # ViewModels & Repository
    content = content.replace("amount: Double", "amount: Long")
    content = content.replace("budgetLimit: Double", "budgetLimit: Long")
    content = content.replace("initialBalance: Double", "initialBalance: Long")
    content = content.replace("targetAmount: Double", "targetAmount: Long")
    content = content.replace("limit: Double", "limit: Long")
    content = content.replace("amt: Double", "amt: Long")
    content = content.replace("StateFlow<Double>", "StateFlow<Long>")
    
    # UI parsing
    content = content.replace("toDoubleOrNull() ?: 0.0", "toLongOrNull() ?: 0L")
    content = content.replace("toDoubleOrNull() == null", "toLongOrNull() == null")
    content = content.replace("toDoubleOrNull()", "toLongOrNull()")
    
    with open(filepath, 'w') as f:
        f.write(content)

for root, dirs, files in os.walk("app/src/main/java/com/example"):
    for file in files:
        if file.endswith(".kt"):
            replace_in_file(os.path.join(root, file))
