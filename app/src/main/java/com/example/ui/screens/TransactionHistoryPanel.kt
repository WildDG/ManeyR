package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.Transaction
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.ui.components.TransactionCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryPanel(
    transactions: List<Transaction>,
    categories: List<Category>,
    accounts: List<Account>,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") } // All, Income, Expense, Transfer
    
    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, categories, accounts) {
        transactions.filter { tx ->
            val typeMatch = when (selectedTypeFilter) {
                "Income" -> tx.type == "PEMASUKAN"
                "Expense" -> tx.type == "PENGELUARAN"
                "Transfer" -> tx.type == "TRANSFER"
                else -> true
            }
            val catName = categories.find { it.id == tx.categoryId }?.name ?: ""
            val accName = accounts.find { it.id == tx.accountId }?.name ?: ""
            val searchMatch = searchQuery.isBlank() || 
                              tx.notes.contains(searchQuery, ignoreCase = true) ||
                              catName.contains(searchQuery, ignoreCase = true) ||
                              accName.contains(searchQuery, ignoreCase = true)
            typeMatch && searchMatch
        }.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx ->
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            sdf.format(Date(tx.date))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by note, category, account...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Income", "Expense", "Transfer")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedTypeFilter == filter,
                            onClick = { selectedTypeFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }
        }

        if (groupedTransactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("No transactions found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedTransactions.forEach { (dateString, txs) ->
                    item {
                        Text(
                            text = dateString,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )
                    }
                    items(txs, key = { it.id }) { tx ->
                        val cat = categories.find { it.id == tx.categoryId }
                        val srcAcc = accounts.find { it.id == tx.accountId }
                        val destAcc = accounts.find { it.id == tx.destAccountId }
                        TransactionCard(
                            transaction = tx,
                            category = cat,
                            sourceAccount = srcAcc,
                            destAccount = destAcc,
                            onEdit = { onEditTransaction(tx) },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}
