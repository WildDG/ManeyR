with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    "recurringTransactions = recurringTransactions,\n                    allTransactions = allTransactions,\n                    txToEdit = txToEdit,",
    "recurringTransactions = recurringTransactions,\n                    tags = tags,\n                    allTransactions = allTransactions,\n                    txToEdit = txToEdit,"
)

content = content.replace(
    "onSave = { amount, type, catId, subCatId, acctId, destAcctId, date, notes, transferFee ->\n                        if (txToEdit != null) {\n                            viewModel.updateTransaction(txToEdit!!, amount, type, catId, subCatId, acctId, destAcctId, date, notes)\n                            txToEdit = null\n                        } else {\n                            viewModel.addTransaction(amount, type, catId, subCatId, acctId, destAcctId, date, notes)",
    "onSave = { amount, type, catId, subCatId, acctId, destAcctId, date, notes, transferFee, selectedTagIds ->\n                        if (txToEdit != null) {\n                            viewModel.updateTransaction(txToEdit!!, amount, type, catId, subCatId, acctId, destAcctId, date, notes, selectedTagIds)\n                            txToEdit = null\n                        } else {\n                            viewModel.addTransaction(amount, type, catId, subCatId, acctId, destAcctId, date, notes, selectedTagIds)"
)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
