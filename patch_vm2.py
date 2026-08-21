import sys

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'r') as f:
    content = f.read()

crud_insert = """
    fun addInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.addInstallment(installment)
        }
    }
    
    fun updateInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.updateInstallment(installment)
        }
    }
    
    fun deleteInstallment(installment: Installment) {
        viewModelScope.launch {
            repository.deleteInstallment(installment)
        }
    }
    
    fun payInstallment(installmentId: Int, accountId: String, amount: Long) {
        viewModelScope.launch {
            repository.payInstallment(installmentId, accountId, amount)
        }
    }
}"""

# Try to insert at the end before the last closing brace
last_brace = content.rfind("}")
if last_brace != -1:
    content = content[:last_brace] + crud_insert + "\n"

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'w') as f:
    f.write(content)
