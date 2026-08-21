import sys

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'r') as f:
    content = f.read()

target = """                OutlinedTextField(
                    value = tenor,
                    onValueChange = { tenor = it },
                    label = { Text("Tenor (Bulan)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = dueDateDay,
                    onValueChange = { dueDateDay = it },
                    label = { Text("Tanggal Jatuh Tempo per Bulan (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )"""

replacement = """                if (installmentType == "CICILAN") {
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
                val datePickerState = androidx.compose.material3.rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
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
                )"""

content = content.replace(target, replacement)

target2 = """import androidx.compose.foundation.clickable"""
replacement2 = """import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton"""
content = content.replace(target2, replacement2)

target3 = """                            val total = totalAmount.toLongOrNull() ?: 0L
                            val months = tenor.toIntOrNull() ?: 1
                            val day = dueDateDay.toIntOrNull()?.coerceIn(1, 31) ?: 1
                            
                            val cal = Calendar.getInstance()
                            // Set to next month if current day is past the due day
                            if (cal.get(Calendar.DAY_OF_MONTH) > day) {
                                cal.add(Calendar.MONTH, 1)
                            }
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            
                            val installmentAmt = if (months > 0) total / months else total
                            
                            val installment = Installment("""

replacement3 = """                            val total = totalAmount.toLongOrNull() ?: 0L
                            val months = if (installmentType == "PIUTANG") 1 else (tenor.toIntOrNull() ?: 1)
                            val installmentAmt = if (installmentType == "PIUTANG") total else (if (months > 0) total / months else total)
                            
                            val installment = Installment("""

content = content.replace(target3, replacement3)

target4 = """                                paymentAccountId = selectedAccountId,
                                firstDueDate = cal.timeInMillis,
                                nextDueDate = cal.timeInMillis,"""

replacement4 = """                                paymentAccountId = selectedAccountId,
                                firstDueDate = selectedDateMillis,
                                nextDueDate = selectedDateMillis,"""

content = content.replace(target4, replacement4)

target5 = """enabled = name.isNotBlank() && totalAmount.isNotBlank() && tenor.isNotBlank()"""
replacement5 = """enabled = name.isNotBlank() && totalAmount.isNotBlank() && (installmentType == "PIUTANG" || tenor.isNotBlank())"""
content = content.replace(target5, replacement5)

with open('app/src/main/java/com/example/ui/screens/AddInstallmentDialog.kt', 'w') as f:
    f.write(content)

