import sys

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'r') as f:
    lines = f.readlines()

# find class ViewModelFactory
idx = -1
for i, line in enumerate(lines):
    if "class ViewModelFactory" in line:
        idx = i
        break

if idx != -1:
    # search backwards for the closing brace of TransactionViewModel
    brace_idx = -1
    for i in range(idx-1, -1, -1):
        if "}" in lines[i]:
            brace_idx = i
            break
            
    if brace_idx != -1:
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
        lines.insert(brace_idx, new_methods)

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'w') as f:
    f.writelines(lines)
