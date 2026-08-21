with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "fun AddTransactionDialog(\n    accounts: List<Account>,\n    categories: List<Category>,\n    subCategories: List<SubCategory>,\n    recurringTransactions: List<RecurringTransaction>,\n    allTransactions: List<Transaction>,\n    txToEdit: Transaction? = null,\n    onDismiss: () -> Unit,\n    onSave: (Double, String, String, String?, String, String?, Long, String, Double) -> Unit,\n    onAddCategoryDirectly: (Category) -> Unit\n) {",
    "fun AddTransactionDialog(\n    accounts: List<Account>,\n    categories: List<Category>,\n    subCategories: List<SubCategory>,\n    tags: List<com.example.data.model.Tag>,\n    recurringTransactions: List<RecurringTransaction>,\n    allTransactions: List<Transaction>,\n    txToEdit: Transaction? = null,\n    onDismiss: () -> Unit,\n    onSave: (Double, String, String, String?, String, String?, Long, String, Double, List<String>) -> Unit,\n    onAddCategoryDirectly: (Category) -> Unit\n) {"
)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
