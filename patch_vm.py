import sys

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'r') as f:
    content = f.read()

# Import Installment
import_insert = "import com.example.data.model.Installment\nimport com.example.data.model.SavingAllocation\n"
content = content.replace("import com.example.data.model.SavingTarget", import_insert + "import com.example.data.model.SavingTarget")

# Add StateFlow
flow_insert = "    val installments: StateFlow<List<Installment>> = repository.installments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n"
content = content.replace("    val savingTargets: StateFlow<List<SavingTarget>> = repository.savingTargets", flow_insert + "    val savingTargets: StateFlow<List<SavingTarget>> = repository.savingTargets")

# Add CRUD methods
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
"""
content = content.replace("    fun addTag(tag: Tag) {", crud_insert + "\n    fun addTag(tag: Tag) {")

with open('app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt', 'w') as f:
    f.write(content)
