package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.Installment
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInstallmentDialog(
    accounts: List<Account>,
    categories: List<Category>,
    installmentToEdit: Installment? = null,
    onDismiss: () -> Unit,
    onSave: (Installment) -> Unit
) {
    var name by remember { mutableStateOf(installmentToEdit?.name ?: "") }
    var totalAmount by remember { mutableStateOf(installmentToEdit?.totalAmount?.toString() ?: "") }
    var tenor by remember { mutableStateOf(installmentToEdit?.totalInstallments?.toString() ?: "") }
    var selectedCategoryId by remember { mutableStateOf(installmentToEdit?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var selectedAccountId by remember { mutableStateOf(installmentToEdit?.paymentAccountId ?: accounts.firstOrNull()?.id ?: "") }
    
    val initialType = remember {
        if (installmentToEdit != null) {
            val cat = categories.find { it.id == installmentToEdit.categoryId }
            if (cat?.type == "PEMASUKAN") "PIUTANG" else "CICILAN"
        } else "CICILAN"
    }
    var installmentType by remember { mutableStateOf(initialType) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Baru",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { installmentType = "CICILAN" }) {
                        RadioButton(selected = installmentType == "CICILAN", onClick = { installmentType = "CICILAN" })
                        Text("Hutang / Cicilan")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { installmentType = "PIUTANG" }) {
                        RadioButton(selected = installmentType == "PIUTANG", onClick = { installmentType = "PIUTANG" })
                        Text("Piutang (Pinjamkan)")
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama (Contoh: Cicilan Motor, Hutang Budi)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { totalAmount = it },
                    label = { Text("Total Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (installmentType == "CICILAN") {
                    OutlinedTextField(
                        value = tenor,
                        onValueChange = { tenor = it },
                        label = { Text("Tenor (Bulan)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                var showDatePicker by remember { mutableStateOf(false) }
                val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = installmentToEdit?.firstDueDate ?: System.currentTimeMillis())
                val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
                
                if (showDatePicker) {
                    androidx.compose.material3.DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Pilih")
                            }
                        }
                    ) {
                        androidx.compose.material3.DatePicker(state = datePickerState)
                    }
                }
                
                OutlinedTextField(
                    value = dateFormat.format(java.util.Date(selectedDateMillis)),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tanggal Mulai / Jatuh Tempo") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                        }
                    }
                )
                
                // Category Dropdown
                val filteredCategories = categories.filter { if (installmentType == "PIUTANG") it.type == "PEMASUKAN" else it.type != "PEMASUKAN" }
                if (filteredCategories.none { it.id == selectedCategoryId }) {
                    selectedCategoryId = filteredCategories.firstOrNull()?.id ?: ""
                }
                
                var catExpanded by remember { mutableStateOf(false) }
                val selectedCat = filteredCategories.find { it.id == selectedCategoryId }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCat?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        filteredCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Account Dropdown
                var accExpanded by remember { mutableStateOf(false) }
                val selectedAcc = accounts.find { it.id == selectedAccountId }
                ExposedDropdownMenuBox(
                    expanded = accExpanded,
                    onExpandedChange = { accExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAcc?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Akun Pembayaran Default") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = accExpanded,
                        onDismissRequest = { accExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val total = totalAmount.toLongOrNull() ?: 0L
                            val months = if (installmentType == "PIUTANG") 1 else (tenor.toIntOrNull() ?: 1)
                            val installmentAmt = if (installmentType == "PIUTANG") total else (if (months > 0) total / months else total)
                            
                            val installment = installmentToEdit?.copy(
                                name = name,
                                totalAmount = total,
                                totalInstallments = months,
                                paymentAccountId = selectedAccountId,
                                categoryId = selectedCategoryId,
                                firstDueDate = selectedDateMillis
                            ) ?: Installment(
                                name = name,
                                totalAmount = total,
                                installmentAmount = installmentAmt,
                                totalInstallments = months,
                                remainingCount = months,
                                remainingAmount = total,
                                paymentAccountId = selectedAccountId,
                                firstDueDate = selectedDateMillis,
                                nextDueDate = selectedDateMillis,
                                categoryId = selectedCategoryId,
                                status = "ACTIVE"
                            )
                            onSave(installment)
                        },
                        enabled = name.isNotBlank() && totalAmount.isNotBlank() && (installmentType == "PIUTANG" || tenor.isNotBlank())
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
