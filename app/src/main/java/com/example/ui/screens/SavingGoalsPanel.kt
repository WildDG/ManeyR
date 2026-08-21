package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import com.example.ui.components.InstallmentCard
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.SavingTarget
import com.example.data.model.Installment
import com.example.data.model.Account
import com.example.ui.components.SavingTargetCard

import com.example.data.model.Category

@Composable
fun SavingGoalsPanel(
    installments: List<Installment>,
    savingTargets: List<SavingTarget>,
    accounts: List<Account>,
    categories: List<Category>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingTarget) -> Unit,
    onDeleteGoal: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    onAddInstallment: () -> Unit = {},
    onPayInstallment: (Installment) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("TABUNGAN") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (activeTab == "TABUNGAN") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == "TABUNGAN",
                onClick = { activeTab = "TABUNGAN" },
                text = { Text("Tabungan") }
            )
            Tab(
                selected = activeTab == "CICILAN",
                onClick = { activeTab = "CICILAN" },
                text = { Text("Cicilan & Piutang") }
            )
        }
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (activeTab == "TABUNGAN") {
                if (savingTargets.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Belum ada target tabungan", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddGoal) {
                            Text("Buat Target")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(savingTargets, key = { it.id }) { target ->
                            val acc = accounts.find { it.id == target.sourceAccountId }
                            SavingTargetCard(
                                target = target,
                                sourceAccount = acc,
                                onSave = { amt -> onSaveToTarget(target.id, target.sourceAccountId, amt) },
                                onDelete = { onDeleteGoal(target) }
                            )
                        }
                    }
                }
            } else {
                if (installments.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Belum ada Cicilan atau Piutang",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onAddInstallment) {
                            Text("Buat Cicilan/Piutang")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(installments) { installment ->
                            val isPiutang = categories.find { it.id == installment.categoryId }?.type == "PEMASUKAN"
                            InstallmentCard(
                                installment = installment,
                                isPiutang = isPiutang,
                                onPay = { onPayInstallment(installment) }
                            )
                        }
                    }
                }
            }
            
            FloatingActionButton(
                onClick = if (activeTab == "TABUNGAN") onAddGoal else onAddInstallment,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = if (activeTab == "TABUNGAN") "Add Goal" else "Add Installment")
            }
        }
    }
}
