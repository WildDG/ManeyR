import re

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    content = f.read()

# At line ~1920:
# Button( onClick = ..., modifier = Modifier.weight(1f), shape = ..., colors = ..., contentPadding = PaddingValues(0.dp) ) { Text(startLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }

old_start_button = """                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    if (selectedFilterDateEpoch != null) cal.timeInMillis = selectedFilterDateEpoch!!
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val c = Calendar.getInstance().apply { set(year, month, day) }
                                            selectedFilterDateEpoch = c.timeInMillis
                                            if (selectedFilterEndDateEpoch == null || selectedFilterEndDateEpoch!! < c.timeInMillis) {
                                                selectedFilterEndDateEpoch = c.timeInMillis
                                            }
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(startLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }"""

new_start_button = """                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    if (selectedFilterDateEpoch != null) cal.timeInMillis = selectedFilterDateEpoch!!
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val c = Calendar.getInstance().apply { set(year, month, day) }
                                            selectedFilterDateEpoch = c.timeInMillis
                                            if (selectedFilterEndDateEpoch == null || selectedFilterEndDateEpoch!! < c.timeInMillis) {
                                                selectedFilterEndDateEpoch = c.timeInMillis
                                            }
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mulai", fontSize = 9.sp, fontWeight = FontWeight.Normal)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(startLabel.replace("Mulai...", "Pilih"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, textAlign = TextAlign.Center)
                                }
                            }"""

old_end_button = """                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    if (selectedFilterEndDateEpoch != null) cal.timeInMillis = selectedFilterEndDateEpoch!!
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val c = Calendar.getInstance().apply { set(year, month, day) }
                                            selectedFilterEndDateEpoch = c.timeInMillis
                                            if (selectedFilterDateEpoch == null || selectedFilterDateEpoch!! > c.timeInMillis) {
                                                selectedFilterDateEpoch = c.timeInMillis
                                            }
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(endLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }"""

new_end_button = """                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    if (selectedFilterEndDateEpoch != null) cal.timeInMillis = selectedFilterEndDateEpoch!!
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val c = Calendar.getInstance().apply { set(year, month, day) }
                                            selectedFilterEndDateEpoch = c.timeInMillis
                                            if (selectedFilterDateEpoch == null || selectedFilterDateEpoch!! > c.timeInMillis) {
                                                selectedFilterDateEpoch = c.timeInMillis
                                            }
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Akhir", fontSize = 9.sp, fontWeight = FontWeight.Normal)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(endLabel.replace("Akhir...", "Pilih"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, textAlign = TextAlign.Center)
                                }
                            }"""


content = content.replace(old_start_button, new_start_button)
content = content.replace(old_end_button, new_end_button)

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.write(content)
