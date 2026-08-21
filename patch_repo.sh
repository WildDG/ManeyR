#!/bin/bash
sed -i 's/suspend fun addTransaction(transaction: Transaction)/suspend fun addTransaction(transaction: Transaction, tagIds: List<String> = emptyList())/g' app/src/main/java/com/example/data/repository/FinanceRepository.kt
sed -i 's/transactionDao.insertTransaction(transaction)/val id = transactionDao.insertTransaction(transaction)\n        if (tagIds.isNotEmpty()) {\n            val txTags = tagIds.map { TransactionTag(id.toInt(), it) }\n            transactionTagDao.insertTransactionTags(txTags)\n        }/g' app/src/main/java/com/example/data/repository/FinanceRepository.kt

sed -i 's/suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction)/suspend fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction, tagIds: List<String> = emptyList())/g' app/src/main/java/com/example/data/repository/FinanceRepository.kt

