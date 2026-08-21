import re
with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val savingTargets: StateFlow<List<SavingTarget>> = repository.savingTargets\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())",
    "val savingTargets: StateFlow<List<SavingTarget>> = repository.savingTargets\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n\n    val tags: StateFlow<List<Tag>> = repository.tags\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())\n\n    suspend fun getTagIdsForTransactionSync(transactionId: Int): List<String> = repository.getTagIdsForTransactionSync(transactionId)"
)

with open("app/src/main/java/com/example/ui/viewmodel/TransactionViewModel.kt", "w") as f:
    f.write(content)
