import sys

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'r') as f:
    content = f.read()

# First, remove them from ViewModelFactory
bad_methods = """    fun addInstallment(installment: Installment) {
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

if bad_methods in content:
    content = content.replace(bad_methods, "}")

# Now, add them to TransactionViewModel
# Let's find "    fun deleteTag(tag: Tag) {" and add them before it, or just anywhere inside TransactionViewModel
insertion_point = "    fun deleteTag(tag: Tag) {\n        viewModelScope.launch {\n            repository.deleteTag(tag)\n        }\n    }"
if insertion_point in content:
    new_methods = """
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
"""
    content = content.replace(insertion_point, new_methods + "\n" + insertion_point)
else:
    # Fallback if deleteTag is not found exactly like that
    print("Could not find insertion point!")

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'w') as f:
    f.write(content)
