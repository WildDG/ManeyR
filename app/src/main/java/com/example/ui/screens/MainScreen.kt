package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Account
import com.example.data.model.Category
import com.example.data.model.SubCategory
import com.example.data.model.SavingTarget
import com.example.data.model.Transaction
import com.example.data.model.RecurringTransaction
import com.example.ui.components.CustomPieChart
import com.example.ui.components.CustomBarChart
import com.example.ui.util.FormatUtils
import com.example.ui.util.IconsMap
import com.example.ui.viewmodel.TransactionViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import java.util.*

enum class ActiveTab {
    HOME, TRANSACTIONS, STATISTICS, SAVING, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier,
    initialIntentAction: String? = null
) {
    var activeTab by rememberSaveable { mutableStateOf(ActiveTab.HOME) }
    var isAddSheetOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialIntentAction) {
        if (initialIntentAction == "OPEN_ADD_TRANSACTION") {
            isAddSheetOpen = true
        }
    }
    var txToEdit by remember { mutableStateOf<Transaction?>(null) }
    var isAddTargetOpen by remember { mutableStateOf(false) }

    var isAddInstallmentOpen by remember { mutableStateOf(false) }
    var activeInstallmentForPayment by remember { mutableStateOf<com.example.data.model.Installment?>(null) }
    var installmentToEdit by remember { mutableStateOf<com.example.data.model.Installment?>(null) }

    
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("finance_prefs", android.content.Context.MODE_PRIVATE) }
    var isGdriveLoggedIn by remember { mutableStateOf(sharedPrefs.getBoolean("gdrive_logged_in", false)) }
    var isAutoBackupEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("gdrive_auto_backup", false)) }
    var hasPromptedRestore by remember { mutableStateOf(sharedPrefs.getBoolean("gdrive_prompted_restore", false)) }
    var showStartupRestorePrompt by remember { mutableStateOf(false) }

    // Mock check for Google Drive backup when logged in
    LaunchedEffect(isGdriveLoggedIn) {
        if (isGdriveLoggedIn && !hasPromptedRestore) {
            // Mocking network delay to Drive API
            kotlinx.coroutines.delay(1500)
            showStartupRestorePrompt = true
            sharedPrefs.edit().putBoolean("gdrive_prompted_restore", true).apply()
            hasPromptedRestore = true
        }
    }

    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val subCategories by viewModel.subCategories.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val monthLabel by viewModel.selectedMonthLabel.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val currentMonthOffset by viewModel.currentMonthOffset.collectAsState()
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val appColorHex by viewModel.targetAppColorHex.collectAsState()
    val globalBudgetLimit = remember(categories) { categories.filter { it.type == "PENGELUARAN" }.sumOf { it.budgetLimit } }

    val navController = rememberNavController()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Pencatat Keuangan",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    val isSystemDark = isSystemInDarkTheme()
                    val userDarkMode by viewModel.isDarkModeEnabled.collectAsState()
                    val isCurrentlyDark = userDarkMode ?: isSystemDark

                    IconButton(
                        onClick = { viewModel.toggleDarkMode(isSystemDark) },
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isCurrentlyDark) "Ubah ke Mode Terang" else "Ubah ke Mode Gelap",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .height(64.dp),
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == ActiveTab.HOME,
                    onClick = { 
                        if (activeTab != ActiveTab.HOME) {
                            activeTab = ActiveTab.HOME
                            navController.navigate("home") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    modifier = Modifier.testTag("nav_home"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.TRANSACTIONS,
                    onClick = { 
                        if (activeTab != ActiveTab.TRANSACTIONS) {
                            activeTab = ActiveTab.TRANSACTIONS
                            navController.navigate("transactions") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Transactions") },
                    modifier = Modifier.testTag("nav_transactions"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.STATISTICS,
                    onClick = { 
                        if (activeTab != ActiveTab.STATISTICS) {
                            activeTab = ActiveTab.STATISTICS
                            navController.navigate("statistics") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Statistics") },
                    modifier = Modifier.testTag("nav_statistics"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.SAVING,
                    onClick = { 
                        if (activeTab != ActiveTab.SAVING) {
                            activeTab = ActiveTab.SAVING
                            navController.navigate("saving") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Saving") },
                    modifier = Modifier.testTag("nav_saving"),
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    selected = activeTab == ActiveTab.SETTINGS,
                    onClick = { 
                        if (activeTab != ActiveTab.SETTINGS) {
                            activeTab = ActiveTab.SETTINGS
                            navController.navigate("settings") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    modifier = Modifier.testTag("nav_settings"),
                    alwaysShowLabel = false
                )
            }
        },
        floatingActionButton = {
            if (activeTab == ActiveTab.HOME || activeTab == ActiveTab.TRANSACTIONS && !isAddSheetOpen && txToEdit == null) {
                // Regular Full Add FAB
                FloatingActionButton(
                    onClick = {
                        if (accounts.isEmpty()) {
                            Toast.makeText(context, "Silakan buat dompet atau rekening terlebih dahulu di Pengaturan sebelum mencatat transaksi!", Toast.LENGTH_LONG).show()
                        } else {
                            isAddSheetOpen = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .animateContentSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = "home",
                    enterTransition = {
                        slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeIn(tween(400))
                    },
                    exitTransition = {
                        slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeOut(tween(400))
                    },
                    popEnterTransition = {
                        slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeIn(tween(400))
                    },
                    popExitTransition = {
                        slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeOut(tween(400))
                    }
                ) {
                    val savingTargets by viewModel.savingTargets.collectAsState()
                    val installments by viewModel.installments.collectAsState()
                    DashboardPanel(
                        installments = installments,
                        accounts = accounts,
                        transactions = transactions,
                        allTransactions = allTransactions,
                        categories = categories,
                        savingTargets = savingTargets,
                        monthLabel = monthLabel,
                        totalBalance = totalBalance,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        globalBudgetLimit = globalBudgetLimit,
                        onNextMonth = { viewModel.nextMonth() },
                        onPrevMonth = { viewModel.previousMonth() },
                        onDeleteTransaction = { transactionToDelete = it },
                        onEditTransaction = { 
                            txToEdit = it
                            isAddSheetOpen = true 
                        },
                        onAddSavingTarget = { name, amt, acctId -> viewModel.addSavingTarget(name, amt, acctId) },
                        onDeleteSavingTarget = { viewModel.deleteSavingTarget(it) },
                        onSaveToTarget = { targetId, acctId, amt -> viewModel.saveToTarget(targetId, acctId, amt) },
                        monthOffset = currentMonthOffset,
                        recurringTransactions = recurringTransactions,
                        onPayInstallment = { activeInstallmentForPayment = it },
                        onDeleteInstallment = { viewModel.deleteInstallment(it) },
                        onTransfer = { srcId, destId, amt, fee ->
                            viewModel.addTransaction(
                                amount = amt,
                                type = "TRANSFER",
                                categoryId = "transfer",
                                accountId = srcId,
                                destAccountId = destId,
                                notes = "Transfer Cepat"
                            )
                            if (fee > 0L) {
                                val expenseCatId = categories.find { it.id == "tagihan" }?.id 
                                    ?: categories.firstOrNull { it.type == "PENGELUARAN" }?.id 
                                    ?: "tagihan"
                                viewModel.addTransaction(
                                    amount = fee,
                                    type = "PENGELUARAN",
                                    categoryId = expenseCatId,
                                    accountId = srcId,
                                    destAccountId = null,
                                    notes = "Biaya Transfer Cepat"
                                )
                            }
                        },
                        onAdjustAsset = { srcId, amt, isPemasukan ->
                            viewModel.addAdjustAssetTransaction(srcId, amt, isPemasukan)
                        },
                        initialIntentAction = initialIntentAction
                    )
                }
                composable(
                    route = "transactions",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    TransactionHistoryPanel(
                        transactions = allTransactions,
                        categories = categories,
                        accounts = accounts,
                        onEditTransaction = { 
                            txToEdit = it
                            isAddSheetOpen = true 
                        },
                        onDeleteTransaction = { transactionToDelete = it }
                    )
                }
                composable(
                    route = "saving",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    val savingTargets by viewModel.savingTargets.collectAsState()
                    val installments by viewModel.installments.collectAsState()
                    SavingGoalsPanel(
                        installments = installments,
                        savingTargets = savingTargets,
                        accounts = accounts,
                        categories = categories,
                        onAddGoal = { isAddTargetOpen = true },
                        onEditGoal = { /* TODO */ },
                        onDeleteGoal = { viewModel.deleteSavingTarget(it) },
                        onSaveToTarget = { targetId, acctId, amt -> viewModel.saveToTarget(targetId, acctId, amt) },
                        onAddInstallment = { isAddInstallmentOpen = true },
                        onPayInstallment = { activeInstallmentForPayment = it }
                    )
                }
                composable(
                    route = "statistics",
                    enterTransition = {
                        if (initialState.destination.route == "home") {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeIn(tween(400))
                        } else {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeIn(tween(400))
                        }
                    },
                    exitTransition = {
                        if (targetState.destination.route == "home") {
                            slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeOut(tween(400))
                        } else {
                            slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeOut(tween(400))
                        }
                    },
                    popEnterTransition = {
                        if (initialState.destination.route == "home") {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeIn(tween(400))
                        } else {
                            slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeIn(tween(400))
                        }
                    },
                    popExitTransition = {
                        if (targetState.destination.route == "home") {
                            slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeOut(tween(400))
                        } else {
                            slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeOut(tween(400))
                        }
                    }
                ) {
                    StatisticsPanel(
                        allTransactions = allTransactions,
                        categories = categories,
                        subCategories = subCategories,
                        accounts = accounts,
                        monthLabel = monthLabel,
                        monthOffset = currentMonthOffset,
                        onNextMonth = { viewModel.nextMonth() },
                        onPrevMonth = { viewModel.previousMonth() }
                    )
                }
                composable(
                    route = "settings",
                    enterTransition = {
                        slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeIn(tween(400))
                    },
                    exitTransition = {
                        slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeOut(tween(400))
                    },
                    popEnterTransition = {
                        slideIntoContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeIn(tween(400))
                    },
                    popExitTransition = {
                        slideOutOfContainer(androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeOut(tween(400))
                    }
                ) {
                    SettingsPanel(
                        accounts = accounts,
                        categories = categories,
                        subCategories = subCategories,
                        recurringTransactions = recurringTransactions,
                        themeMode = themeMode,
                        appColorHex = appColorHex,
                        globalBudgetLimit = globalBudgetLimit,
                        onGlobalBudgetLimitChange = { viewModel.setGlobalBudgetLimit(it) },
                        onThemeChange = { viewModel.setThemeMode(it) },
                        onAppColorChange = { viewModel.setAppColorHex(it) },
                        onAddAccount = { name, initial -> viewModel.addCustomAccount(name, initial) },
                        onAddCategory = { name, type, icon, limit, parentId -> viewModel.addCustomCategory(name, type, icon, limit, parentId) },
                        onDeleteAccount = { viewModel.deleteCustomAccount(it) },
                        onDeleteCategory = { viewModel.deleteCustomCategory(it) },
                        onEditAccount = { viewModel.updateCustomAccount(it) },
                        onEditCategory = { viewModel.updateCustomCategory(it) },
                        onAddSubCat = { name, catId -> viewModel.addCustomSubCategory(name, catId) },
                        onDeleteSubCat = { viewModel.deleteCustomSubCategory(it) },
                        onUpdateSubCat = { viewModel.updateCustomSubCategory(it) },
                        onAddRecurringTransaction = { name, amount, type, catId, acctId, day, notes ->
                            viewModel.addRecurringTransaction(name, amount, type, catId, acctId, day, notes)
                        },
                        onDeleteRecurringTransaction = { viewModel.deleteRecurringTransaction(it) },
                        onUpdateRecurringTransaction = { viewModel.updateRecurringTransaction(it) },
                        onSeedMockData = { seedMockTransactions(viewModel) },
                        onClearData = { clearAllData(viewModel) },
                        onImportCsv = { csv, cb -> viewModel.importCsvTransactions(csv, cb) },
                        onExportCsv = { cb -> viewModel.exportCsvTransactions(cb) },
                        isGdriveLoggedIn = isGdriveLoggedIn,
                        onGdriveLoginChange = { 
                            isGdriveLoggedIn = it
                            sharedPrefs.edit().putBoolean("gdrive_logged_in", it).apply()
                        },
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        onAutoBackupChange = { 
                            isAutoBackupEnabled = it
                            sharedPrefs.edit().putBoolean("gdrive_auto_backup", it).apply()
                        }
                    )
                }
            }

            if (isAddSheetOpen) {
                AddTransactionDialog(
                    accounts = accounts,
                    categories = categories,
                    subCategories = subCategories,
                    recurringTransactions = recurringTransactions,
                    tags = tags,
                    allTransactions = allTransactions,
                    txToEdit = txToEdit,
                    getTagsForTx = { txId -> viewModel.getTagIdsForTransactionSync(txId) },
                    onDismiss = { 
                        isAddSheetOpen = false 
                        txToEdit = null
                    },
                    onSave = { amount, type, catId, subCatId, acctId, destAcctId, date, notes, transferFee, selectedTagIds ->
                        if (txToEdit != null) {
                            viewModel.updateTransaction(txToEdit!!, amount, type, catId, subCatId, acctId, destAcctId, date, notes, selectedTagIds)
                            txToEdit = null
                        } else {
                            viewModel.addTransaction(amount, type, catId, subCatId, acctId, destAcctId, date, notes, selectedTagIds)
                            if (transferFee > 0L) {
                                val expenseCatId = categories.find { it.id == "tagihan" }?.id 
                                    ?: categories.firstOrNull { it.type == "PENGELUARAN" }?.id 
                                    ?: "tagihan"
                                viewModel.addTransaction(
                                    amount = transferFee,
                                    type = "PENGELUARAN",
                                    categoryId = expenseCatId,
                                    accountId = acctId,
                                    destAccountId = null,
                                    date = date,
                                    notes = if (notes.isNotBlank()) "Biaya Transfer: $notes" else "Biaya Transfer"
                                )
                            }
                        }
                        isAddSheetOpen = false
                    },
                    onAddCategoryDirectly = { newCat ->
                        viewModel.updateCustomCategory(newCat)
                    },
                    onAddSubCatDirectly = { name, catId ->
                        viewModel.addCustomSubCategory(name, catId)
                    }
                )
            }

            if (showStartupRestorePrompt) {
                AlertDialog(
                    onDismissRequest = { showStartupRestorePrompt = false },
                    title = {
                        Text(
                            text = "Backup Ditemukan",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = "Aplikasi menemukan file backup transaksi terbaru di Google Drive yang terhubung. Apakah Anda ingin melakukan pemulihan (restore) data dari Drive sekarang?\n\nPeringatan: Data yang ada sekarang akan ditimpa dengan data backup dari Drive.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showStartupRestorePrompt = false
                                Toast.makeText(context, "Memulai proses pemulihan (restore) dari Google Drive...", Toast.LENGTH_LONG).show()
                                // TODO: Actual Drive Download & Restore logic
                            }
                        ) {
                            Text("Restore Sekarang")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStartupRestorePrompt = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            if (transactionToDelete != null) {
                val txToDelete = transactionToDelete!!
                AlertDialog(
                    onDismissRequest = { transactionToDelete = null },
                    title = {
                        Text(
                            text = "Hapus Catatan Transaksi",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = "Apakah Anda yakin ingin menghapus catatan transaksi '${txToDelete.notes.ifBlank { "Tanpa keterangan" }}' sebesar ${FormatUtils.formatRupiah(txToDelete.amount)}? Tindakan ini tidak dapat dibatalkan.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteTransaction(txToDelete)
                                transactionToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Hapus/Delete")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { transactionToDelete = null }
                        ) {
                            Text("Batal")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            
    if (isAddTargetOpen) {
        AddSavingTargetDialog(
            accounts = accounts,
            onDismiss = { isAddTargetOpen = false },
            onSave = { name, amount, sourceId ->
                viewModel.addSavingTarget(name, amount, sourceId)
                isAddTargetOpen = false
            }
        )
    }

    if (isAddInstallmentOpen) {
        AddInstallmentDialog(
            accounts = accounts,
            categories = categories,
            installmentToEdit = installmentToEdit,
            onDismiss = { 
                isAddInstallmentOpen = false 
                installmentToEdit = null
            },
            onSave = { 
                if (installmentToEdit != null) {
                    viewModel.updateInstallment(it)
                } else {
                    viewModel.addInstallment(it)
                }
                isAddInstallmentOpen = false
                installmentToEdit = null
            }
        )
    }
    
    activeInstallmentForPayment?.let { installment ->
        val cat = categories.find { it.id == installment.categoryId }
        val isPiutang = cat?.type == "PEMASUKAN"
        var showPaymentDialog by remember { mutableStateOf(true) }
        var selectedPaymentAccountId by remember { mutableStateOf(installment.paymentAccountId) }
        val selectedAccount = accounts.find { it.id == selectedPaymentAccountId }
        var paymentAmountText by remember { mutableStateOf(installment.installmentAmount.toString()) }
        val parsedAmount = paymentAmountText.toLongOrNull() ?: 0L
        val isBalanceSufficient = isPiutang || (selectedAccount != null && selectedAccount.balance >= parsedAmount)
        
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPaymentDialog = false
                    activeInstallmentForPayment = null
                },
                title = { Text(if (isPiutang) "Terima Pembayaran Piutang" else "Bayar Cicilan") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (isPiutang) "Masukkan nominal penerimaan uang dari '${installment.name}':" else "Konfirmasi pembayaran cicilan untuk '${installment.name}':")
                        
                        OutlinedTextField(
                            value = paymentAmountText,
                            onValueChange = { paymentAmountText = it.filter { char -> char.isDigit() } },
                            label = { Text("Nominal (Rp)") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        var accExpanded by remember { mutableStateOf(false) }
                        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                        androidx.compose.material3.ExposedDropdownMenuBox(
                            expanded = accExpanded,
                            onExpandedChange = { accExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedAccount?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isPiutang) "Masuk ke Akun/Dompet" else "Bayar dari Akun/Dompet") },
                                trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = accExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = accExpanded,
                                onDismissRequest = { accExpanded = false }
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name + " (Rp" + com.example.ui.util.FormatUtils.formatRupiah(acc.balance) + ")") },
                                        onClick = {
                                            selectedPaymentAccountId = acc.id
                                            accExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (!isBalanceSufficient && !isPiutang) {
                            Text(
                                "Saldo tidak cukup!",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.payInstallment(installment.id, selectedPaymentAccountId, parsedAmount)
                            showPaymentDialog = false
                            activeInstallmentForPayment = null
                        },
                        enabled = isBalanceSufficient
                    ) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showPaymentDialog = false
                        activeInstallmentForPayment = null
                    }) {
                        Text("Batal")
                    }
                }
            )
        }
    }

        }
    }
}

@Composable
fun DashboardPanel(
    installments: List<com.example.data.model.Installment>,
    accounts: List<Account>,
    transactions: List<Transaction>,
    allTransactions: List<Transaction>,
    categories: List<Category>,
    savingTargets: List<SavingTarget>,
    monthLabel: String,
    totalBalance: Long,
    monthlyIncome: Long,
    monthlyExpense: Long,
    globalBudgetLimit: Long,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit,
    onAddSavingTarget: (String, Long, String) -> Unit,
    onDeleteSavingTarget: (SavingTarget) -> Unit,
    onSaveToTarget: (Int, String, Long) -> Unit,
    monthOffset: Int,
    recurringTransactions: List<RecurringTransaction>,
    onTransfer: (String, String, Long, Long) -> Unit,
    onAdjustAsset: (String, Long, Boolean) -> Unit,
    onPayInstallment: (com.example.data.model.Installment) -> Unit = {},
    onDeleteInstallment: (com.example.data.model.Installment) -> Unit = {},
    initialIntentAction: String? = null
) {
    var historyAccountForPopup by remember { mutableStateOf<Account?>(null) }
    var isAddTargetOpen by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }
    var isUpcomingExpanded by remember { mutableStateOf(false) }
    var isInstallmentsExpanded by rememberSaveable { mutableStateOf(true) }

    var isAddInstallmentOpen by remember { mutableStateOf(false) }
    var activeInstallmentForPayment by remember { mutableStateOf<com.example.data.model.Installment?>(null) }
    var installmentToEdit by remember { mutableStateOf<com.example.data.model.Installment?>(null) }


    LaunchedEffect(initialIntentAction) {
        if (initialIntentAction == "OPEN_ADD_TARGET") {
            isAddTargetOpen = true
        }
    }
    var activeTargetForDeposit by remember { mutableStateOf<SavingTarget?>(null) }
    var activeAccountForQuickTransfer by remember { mutableStateOf<Account?>(null) }
    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var minAmountQuery by remember { mutableStateOf("") }
    var maxAmountQuery by remember { mutableStateOf("") }
    var selectedFilterDateEpoch by remember { mutableStateOf<Long?>(null) }
    var selectedFilterEndDateEpoch by remember { mutableStateOf<Long?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var activeDashboardFilter by remember { mutableStateOf("BULAN") } // "HARI", "BULAN", "TAHUN"
    var activeAccountFilter by remember { mutableStateOf<String?>(null) } // accountId

    val upcomingIncome = remember(installments, categories) {
        installments.filter { it.status == "ACTIVE" && (categories.find { cat -> cat.id == it.categoryId }?.type == "PEMASUKAN") }
            .sumOf { it.installmentAmount }
    }
    
    val upcomingExpense = remember(installments, categories) {
        installments.filter { it.status == "ACTIVE" && (categories.find { cat -> cat.id == it.categoryId }?.type != "PEMASUKAN") }
            .sumOf { it.installmentAmount }
    }

    var isCalendarExpanded by remember { mutableStateOf(false) }

    val dashboardFilteredTransactions = remember(transactions, allTransactions, activeDashboardFilter, monthOffset, selectedFilterDateEpoch, selectedFilterEndDateEpoch, activeAccountFilter) {
        val baseFiltered = when (activeDashboardFilter) {
            "HARI" -> {
                val today = Calendar.getInstance()
                allTransactions.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    txCal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    txCal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                }
            }
            "BULAN" -> {
                transactions
            }
            "TAHUN" -> {
                val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
                val offsetYear = calendar.get(Calendar.YEAR)
                allTransactions.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.YEAR) == offsetYear
                }
            }
            "SPESIFIK" -> {
                if (selectedFilterDateEpoch != null && selectedFilterEndDateEpoch != null) {
                    val startCal = Calendar.getInstance().apply { 
                        timeInMillis = selectedFilterDateEpoch!!
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val endCal = Calendar.getInstance().apply { 
                        timeInMillis = selectedFilterEndDateEpoch!!
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    allTransactions.filter { tx ->
                        tx.date in startCal.timeInMillis..endCal.timeInMillis
                    }
                } else {
                    val filterEpoch = selectedFilterDateEpoch ?: System.currentTimeMillis()
                    val calFilter = Calendar.getInstance().apply { timeInMillis = filterEpoch }
                    allTransactions.filter { tx ->
                        val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                        calFilter.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                        calFilter.get(Calendar.MONTH) == txCal.get(Calendar.MONTH) &&
                        calFilter.get(Calendar.DAY_OF_MONTH) == txCal.get(Calendar.DAY_OF_MONTH)
                    }
                }
            }
            else -> transactions
        }
        
        if (activeAccountFilter == null) {
            baseFiltered
        } else {
            baseFiltered.filter { it.accountId == activeAccountFilter || it.destAccountId == activeAccountFilter }
        }
    }

    val searchFilteredTransactions = remember(
        dashboardFilteredTransactions, searchQuery, minAmountQuery, maxAmountQuery, selectedFilterDateEpoch, selectedFilterEndDateEpoch, categories
    ) {
        dashboardFilteredTransactions.filter { tx ->
            val matchKeyword = if (searchQuery.trim().isEmpty()) {
                true
            } else {
                val cat = categories.find { it.id == tx.categoryId }
                tx.notes.contains(searchQuery, ignoreCase = true) ||
                        (cat?.name ?: "").contains(searchQuery, ignoreCase = true)
            }
            
            val minA = minAmountQuery.toLongOrNull()
            val maxA = maxAmountQuery.toLongOrNull()
            val matchMin = if (minA == null) true else tx.amount >= minA
            val matchMax = if (maxA == null) true else tx.amount <= maxA
            
            val matchDate = if (selectedFilterDateEpoch == null) {
                true
            } else if (selectedFilterDateEpoch != null && selectedFilterEndDateEpoch != null) {
                val startCal = Calendar.getInstance().apply { 
                    timeInMillis = selectedFilterDateEpoch!!
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply { 
                    timeInMillis = selectedFilterEndDateEpoch!!
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                tx.date in startCal.timeInMillis..endCal.timeInMillis
            } else {
                val calFilter = Calendar.getInstance().apply { timeInMillis = selectedFilterDateEpoch!! }
                val calTx = Calendar.getInstance().apply { timeInMillis = tx.date }
                calFilter.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calFilter.get(Calendar.MONTH) == calTx.get(Calendar.MONTH) &&
                calFilter.get(Calendar.DAY_OF_MONTH) == calTx.get(Calendar.DAY_OF_MONTH)
            }
            
            matchKeyword && matchMin && matchMax && matchDate
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val dynamicPadding = when {
        screenWidth < 360 -> 6.dp
        screenWidth < 600 -> 10.dp
        else -> 18.dp
    }
    val dynamicSpacing = when {
        screenWidth < 360 -> 6.dp
        screenWidth < 600 -> 10.dp
        else -> 14.dp
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(dynamicPadding),
        verticalArrangement = Arrangement.spacedBy(dynamicSpacing)
    ) {
        // Combined Unified Financial Overview Card
        item {
            val gradientBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.tertiary
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.elevatedCardElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier.background(gradientBrush)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Total Saldo Gabungan",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { isBalanceVisible = !isBalanceVisible },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Balance",
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBalanceVisible) FormatUtils.formatRupiah(totalBalance) else "••••••••",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Income Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Pemasukan",
                                            tint = Color(0xFFC8E6C9),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pemasukan",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBalanceVisible) "+ ${FormatUtils.formatRupiah(monthlyIncome)}" else "••••••••",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                            )
                            
                            // Expense Column
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Pengeluaran",
                                            tint = Color(0xFFFFCDD2),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pengeluaran",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isBalanceVisible) "- ${FormatUtils.formatRupiah(monthlyExpense)}" else "••••••••",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        // Expandable toggle button for upcoming
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .clickable { isUpcomingExpanded = !isUpcomingExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUpcomingExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Upcoming",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(visible = isUpcomingExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                androidx.compose.material3.HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = "Upcoming In",
                                                tint = Color(0xFFC8E6C9),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Akan Masuk (Piutang)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isBalanceVisible) FormatUtils.formatRupiah(upcomingIncome) else "••••••••",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            color = Color(0xFFE8F5E9)
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                                    )
                                    
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = "Upcoming Out",
                                                tint = Color(0xFFFFCDD2),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Akan Keluar (Cicilan)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isBalanceVisible) FormatUtils.formatRupiah(upcomingExpense) else "••••••••",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                            color = Color(0xFFFFEBEE)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bar Chart (with Limit)
        item {
            var isExpenseChartExpanded by rememberSaveable { mutableStateOf(false) }
            val expenseTransactions = dashboardFilteredTransactions.filter { it.type == "PENGELUARAN" }
            val totalExpensePie = expenseTransactions.sumOf { it.amount }
            if (totalExpensePie > 0) {
                val grouped = expenseTransactions.groupBy { it.categoryId }.map { (catId, txs) ->
                    val cat = categories.find { it.id == catId } ?: Category(catId, catId, "QuestionMark", "PENGELUARAN", "#9E9E9E")
                    val amount = txs.sumOf { it.amount }
                    com.example.ui.viewmodel.CategoryShare(
                        category = cat,
                        amount = amount,
                        percentage = (amount.toDouble() / totalExpensePie.toDouble()) * 100.0
                    )
                }.sortedByDescending { it.amount }
                
                val pipDisplayShares = if (grouped.size <= 5) grouped else {
                    val top = grouped.take(4)
                    val otherShares = grouped.drop(4)
                    val otherCategory = Category("lainnya", "Lainnya", "Category", "PENGELUARAN", "#B0BEC5")
                    top + com.example.ui.viewmodel.CategoryShare(otherCategory, otherShares.sumOf { it.amount }, otherShares.sumOf { it.percentage })
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize().clickable { isExpenseChartExpanded = !isExpenseChartExpanded },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Pengeluaran Kategori (Bulan Ini)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Icon(
                                imageVector = if (isExpenseChartExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                        
                        if (!isExpenseChartExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val totalColor = if (globalBudgetLimit > 0) {
                                if (totalExpensePie >= globalBudgetLimit) Color(0xFFD32F2F)
                                else if (totalExpensePie >= 0.8 * globalBudgetLimit) Color(0xFFFFB300)
                                else Color(0xFF2E7D32)
                            } else MaterialTheme.colorScheme.primary

                            Text(
                                text = "Total: ${FormatUtils.formatRupiah(totalExpensePie)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = totalColor
                            )
                            if (globalBudgetLimit > 0) {
                                Text(
                                    text = "Batas: ${FormatUtils.formatRupiah(globalBudgetLimit)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (totalExpensePie >= globalBudgetLimit) {
                                    Text(
                                        text = "Peringatan ! Anda telah melewati batas anggaran.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                } else if (totalExpensePie >= 0.8 * globalBudgetLimit) {
                                    Text(
                                        text = "Peringatan ! Mendekati batas anggaran bulanan.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF4E342E),
                                        modifier = Modifier.background(Color(0xFFFFB300), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            if (globalBudgetLimit > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                if (totalExpensePie >= globalBudgetLimit) {
                                    Text(
                                        text = "Peringatan ! Anda telah melewati batas anggaran.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.background(Color(0xFFD32F2F), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                } else if (totalExpensePie >= 0.8 * globalBudgetLimit) {
                                    Text(
                                        text = "Peringatan ! Mendekati batas anggaran bulanan.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF4E342E),
                                        modifier = Modifier.background(Color(0xFFFFB300), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            com.example.ui.components.CustomBarChart(
                                shares = pipDisplayShares,
                                statType = "PENGELUARAN",
                                modifier = Modifier.fillMaxWidth(),
                                globalBudgetLimit = globalBudgetLimit
                            )
                        }
                    }
                }
            }
        }

        // Month Selector Sliding Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(36.dp).testTag("prev_month_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya", modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(36.dp).testTag("next_month_button")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Calendar-based visualization Card
        item {
            var selectedCalendarDay by remember(monthOffset) {
                mutableStateOf(if (monthOffset == 0) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else 1)
            }

            val calendar = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
            }
            
            val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayCal = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                set(Calendar.DAY_OF_MONTH, 1)
            }
            // Sunday is 1, Monday is 2, etc.
            val startDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_calendar_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Kalender",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kalender Transaksi & Rutinitas",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { isCalendarExpanded = !isCalendarExpanded },
                            modifier = Modifier.size(32.dp).testTag("toggle_calendar_expand")
                        ) {
                            Icon(
                                imageVector = if (isCalendarExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isCalendarExpanded) "Sembunyikan" else "Tampilkan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = isCalendarExpanded,
                        enter = androidx.compose.animation.expandVertically(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                        ) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                        ) + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                            Text(
                                text = "Ketuk tanggal untuk melihat rencana rutin dan rincian catatan aktual harian.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Days of week header
                            val daysHeader = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                daysHeader.forEach { d ->
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        color = if (d == "Min") Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val totalCells = startDayOfWeek + maxDays
                            val rowsCount = (totalCells + 6) / 7

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (r in 0 until rowsCount) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        for (c in 0..6) {
                                            val cellIndex = r * 7 + c
                                            val dayNumber = cellIndex - startDayOfWeek + 1

                                            if (cellIndex < startDayOfWeek || dayNumber > maxDays) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            } else {
                                                val isSelected = selectedCalendarDay == dayNumber
                                                val dayRecurring = recurringTransactions.filter { !it.isPaused && it.dayOfMonth == dayNumber }
                                                val hasRecurring = dayRecurring.isNotEmpty()
                                                
                                                val indicatorColor = if (dayRecurring.any { it.type == "PENGELUARAN" }) Color(0xFFC62828) else Color(0xFF2E7D32)

                                                // Dynamic Animated Selection Color & Scale
                                                val selectionBgColor by androidx.compose.animation.animateColorAsState(
                                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                                                    label = "day_bg"
                                                )
                                                
                                                val selectionTextColor by androidx.compose.animation.animateColorAsState(
                                                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                                                    label = "day_text"
                                                )

                                                val scale by androidx.compose.animation.core.animateFloatAsState(
                                                    targetValue = if (isSelected) 1.05f else 1f,
                                                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy),
                                                    label = "day_scale"
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1.3f)
                                                        .graphicsLayer(scaleX = scale, scaleY = scale)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(selectionBgColor)
                                                        .clickable { selectedCalendarDay = dayNumber }
                                                        
                                .padding(vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = dayNumber.toString(),
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            color = selectionTextColor,
                                                            fontSize = 11.sp
                                                        )
                                                        if (hasRecurring) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(top = 2.dp)
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else indicatorColor)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Rincian Tanggal $selectedCalendarDay:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Crossfade animation for details when selectedCalendarDay changes
                            androidx.compose.animation.AnimatedContent(
                                targetState = selectedCalendarDay,
                                transitionSpec = {
                                    androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(220))
                                },
                                label = "calendar_details_anim"
                            ) { targetDay ->
                                val dayScheduled = recurringTransactions.filter { it.dayOfMonth == targetDay }
                                val dayActual = transactions.filter { tx ->
                                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                                    txCal.get(Calendar.DAY_OF_MONTH) == targetDay
                                }

                                if (dayScheduled.isEmpty() && dayActual.isEmpty()) {
                                    Text(
                                        text = "Tidak ada rencana rutin atau catatan harian di tanggal ini.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    val detailScrollState = rememberScrollState()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .verticalScroll(detailScrollState)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (dayScheduled.isNotEmpty()) {
                                                Text(
                                                    text = "Rencana Rutin Terjadwal",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                dayScheduled.forEach { rec ->
                                                    val cat = categories.find { it.id == rec.categoryId }
                                                    val isExp = rec.type == "PENGELUARAN"
                                                    val amtColor = if (isExp) Color(0xFFC62828) else Color(0xFF2E7D32)
                                                    val catColor = runCatching { Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E")) }.getOrDefault(Color.Gray)

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                            .padding(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(CircleShape)
                                                                .background(catColor.copy(alpha = 0.12f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = IconsMap.getIcon(cat?.iconName ?: "QuestionMark"),
                                                                contentDescription = rec.name,
                                                                tint = catColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(rec.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = FormatUtils.formatRupiah(rec.amount),
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = amtColor
                                                                )
                                                                if (rec.isPaused) {
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .background(Color.Red.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                                            .padding(horizontal = 4.dp)
                                                                    ) {
                                                                    Text("PAUSED", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Rutin", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                    }
                                                }
                                            }
                                        }

                                        if (dayActual.isNotEmpty()) {
                                            Text(
                                                text = "Catatan Keuangan Aktual",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            dayActual.forEach { tx ->
                                                val cat = categories.find { it.id == tx.categoryId } ?: categories.find { it.id == "belanja" }
                                                val isExp = tx.type == "PENGELUARAN"
                                                val amtColor = if (isExp) Color(0xFFC62828) else if (tx.type == "PEMASUKAN") Color(0xFF2E7D32) else Color(0xFF1565C0)
                                                val catColor = runCatching { Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E")) }.getOrDefault(Color.Gray)

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(catColor.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = IconsMap.getIcon(cat?.iconName ?: "QuestionMark"),
                                                            contentDescription = tx.notes,
                                                            tint = catColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        val rawNotes = tx.notes
                                                         var subCatLabel = ""
                                                         var cleanNotes = if (rawNotes.isEmpty()) (cat?.name ?: "Transaksi") else rawNotes
                                                         if (rawNotes.startsWith("[") && rawNotes.contains("]")) {
                                                             val endIdx = rawNotes.indexOf("]")
                                                             if (endIdx > 1) {
                                                                 subCatLabel = rawNotes.substring(1, endIdx)
                                                                 cleanNotes = rawNotes.substring(endIdx + 1).trim()
                                                                 if (cleanNotes.isEmpty()) {
                                                                     cleanNotes = cat?.name ?: "Transaksi"
                                                                 }
                                                             }
                                                         }
                                                         
                                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                                             if (subCatLabel.isNotEmpty()) {
                                                                 Box(
                                                                     modifier = Modifier
                                                                         .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                                         .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                 ) {
                                                                     Text(
                                                                         text = subCatLabel,
                                                                         style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                         color = MaterialTheme.colorScheme.onSecondaryContainer
                                                                     )
                                                                 }
                                                                 Spacer(modifier = Modifier.width(4.dp))
                                                             }
                                                             Text(
                                                                 text = cleanNotes,
                                                                 style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                                 maxLines = 1,
                                                                 overflow = TextOverflow.Ellipsis
                                                             )
                                                         }
                                                        Text(
                                                            text = if (tx.type == "TRANSFER") "Trf: ${FormatUtils.formatRupiah(tx.amount)}" else FormatUtils.formatRupiah(tx.amount),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = amtColor
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Aktual", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Wallets/Accounts Carousel Summary (Hero Element)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_accounts_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dompet & Akun Saya",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${accounts.size} Akun",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                    ) {
                        if (accounts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Belum ada dompet", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            accounts.forEach { account ->
                                val accountColor = runCatching {
                                    Color(android.graphics.Color.parseColor(account.colorHex))
                                }.getOrDefault(MaterialTheme.colorScheme.primary)

                                 // Find last transaction for this account
                                val accountTransactions = transactions.filter { it.accountId == account.id || it.destAccountId == account.id }
                                val lastTx = accountTransactions.maxByOrNull { it.date }
                                val lastTxText = if (lastTx == null) {
                                    "Belum ada"
                                } else {
                                    val isPemasukan = lastTx.type == "PEMASUKAN" || (lastTx.type == "TRANSFER" && lastTx.destAccountId == account.id)
                                    val prefix = if (lastTx.type == "TRANSFER") "➔ " else if (isPemasukan) "+ " else "- "
                                    "$prefix${FormatUtils.formatRupiah(lastTx.amount)}"
                                }
                                
                                val isSelected = activeAccountFilter == account.id
                                Card(
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable {
                                             historyAccountForPopup = account
                                        }
                                        .testTag("account_hero_card_${account.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) accountColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accountColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = IconsMap.getIcon(account.iconName),
                                                    contentDescription = account.name,
                                                    tint = accountColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = account.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { activeAccountForQuickTransfer = account },
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)
                                                    .clip(CircleShape)
                                                    .testTag("quick_transfer_btn_${account.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = "Transfer Cepat",
                                                    modifier = Modifier.size(12.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            text = FormatUtils.formatRupiah(account.balance),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (lastTx == null) "Belum ada tx" else "Last: $lastTxText",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, fontSize = 9.sp),
                                            color = if (lastTx == null) Color.Gray else if (lastTx.type == "PEMASUKAN" || (lastTx.type == "TRANSFER" && lastTx.destAccountId == account.id)) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Saving Targets Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Tabungan (Impian) 🎯",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { isAddTargetOpen = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Target", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Target Baru", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        if (savingTargets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada target impian. Yuk menabung untuk beli sesuatu!",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savingTargets, key = { "target_${it.id}" }) { target ->
                val sourceAccount = accounts.find { it.id == target.sourceAccountId }
                val currentAmount = sourceAccount?.balance ?: 0L
                val progress = if (target.targetAmount > 0) {
                    (currentAmount / target.targetAmount).toFloat().coerceIn(0f, 1f)
                } else 0f
                val percentage = (progress * 100).toInt()
                val targetColor = runCatching {
                    Color(android.graphics.Color.parseColor(target.colorHex))
                }.getOrDefault(MaterialTheme.colorScheme.primary)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saving_target_${target.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular Progress Indicator visually filling up
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(52.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxSize(),
                                color = targetColor,
                                strokeWidth = 5.dp,
                                trackColor = targetColor.copy(alpha = 0.15f)
                            )
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = targetColor
                            )
                        }

                        // Details in the middle
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = target.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sumber: ${sourceAccount?.name ?: "Pilih Akun"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${FormatUtils.formatRupiah(currentAmount)} / ${FormatUtils.formatRupiah(target.targetAmount)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        // Actions on the right
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onDeleteSavingTarget(target) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Target",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Piutang & Cicilan Section
        val activeInstallments = installments.filter { it.status == "ACTIVE" }
        if (activeInstallments.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp)
                        .clickable { isInstallmentsExpanded = !isInstallmentsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Piutang & Cicilan Aktif 💳",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (isInstallmentsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (isInstallmentsExpanded) {
                items(activeInstallments, key = { "inst_${it.id}" }) { installment ->
                    val cat = categories.find { it.id == installment.categoryId }
                    val isPiutang = cat?.type == "PEMASUKAN"
                    
                    com.example.ui.components.InstallmentCard(
                        installment = installment,
                        isPiutang = isPiutang,
                        onPay = { onPayInstallment(installment) },
                        onDelete = { onDeleteInstallment(installment) },
                        onEdit = { 
                            installmentToEdit = installment
                            isAddInstallmentOpen = true 
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Transactions History Header
        item {
            Text(
                text = "Riwayat Transaksi",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    
                                .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf(
                    "HARI" to "Hari Ini",
                    "BULAN" to "Bulan Ini",
                    "TAHUN" to "Tahun Ini",
                    "SPESIFIK" to "Rentang Tanggal"
                )
                items(filters) { (filterType, label) ->
                    val isSelected = activeDashboardFilter == filterType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activeDashboardFilter = filterType
                                if (filterType == "SPESIFIK" && selectedFilterDateEpoch == null) {
                                    selectedFilterDateEpoch = System.currentTimeMillis()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("dashboard_filter_$filterType")
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        item {
            var expanded by remember { mutableStateOf(false) }
            val selectedName = if (activeAccountFilter == null) "Semua Dompet" else accounts.find { it.id == activeAccountFilter }?.name ?: "Semua Dompet"
            val selectedColor = if (activeAccountFilter == null) MaterialTheme.colorScheme.primary else runCatching { Color(android.graphics.Color.parseColor(accounts.find { it.id == activeAccountFilter }?.colorHex ?: "#808080")) }.getOrDefault(Color.Gray)
            
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    Card(
                        modifier = Modifier.clip(RoundedCornerShape(24.dp)).clickable { expanded = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = selectedColor.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, selectedColor.copy(alpha = 0.3f)),
                        elevation = CardDefaults.elevatedCardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = selectedColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = selectedColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = selectedColor, modifier = Modifier.size(18.dp))
                        }
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Dompet", style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                activeAccountFilter = null
                                expanded = false
                            }
                        )
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name, style = MaterialTheme.typography.bodySmall) },
                                onClick = {
                                    activeAccountFilter = account.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (activeDashboardFilter == "SPESIFIK") {
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val dateSdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
                val startLabel = if (selectedFilterDateEpoch != null) dateSdf.format(java.util.Date(selectedFilterDateEpoch!!)) else "Mulai..."
                val endLabel = if (selectedFilterEndDateEpoch != null) dateSdf.format(java.util.Date(selectedFilterEndDateEpoch!!)) else "Akhir..."

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        
                                .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pilih Rentang Tanggal",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
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
                            }

                            Button(
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
                            }
                        }
                    }
                }
            }
        }

        // Global Search Bar & Filters Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    
                                .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row for text input & filter toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari catatan atau kategori...", style = MaterialTheme.typography.bodySmall) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("search_query_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        // Toggle for Advanced Filters
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSearchExpanded || minAmountQuery.isNotEmpty() || maxAmountQuery.isNotEmpty() || selectedFilterDateEpoch != null) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .testTag("search_filter_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter Lanjutan",
                                tint = if (isSearchExpanded || minAmountQuery.isNotEmpty() || maxAmountQuery.isNotEmpty() || selectedFilterDateEpoch != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Advanced Collapsible Filter options
                    if (isSearchExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Filter Tambahan",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // Amount range fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = minAmountQuery,
                                    onValueChange = { minAmountQuery = it },
                                    placeholder = { Text("Harga Min (Rp)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("search_min_amount"),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = maxAmountQuery,
                                    onValueChange = { maxAmountQuery = it },
                                    placeholder = { Text("Harga Max (Rp)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("search_max_amount"),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    )
                                )
                            }
                            
                            // Reset all filters button
                            if (searchQuery.isNotEmpty() || minAmountQuery.isNotEmpty() || maxAmountQuery.isNotEmpty() || selectedFilterDateEpoch != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            searchQuery = ""
                                            minAmountQuery = ""
                                            maxAmountQuery = ""
                                            selectedFilterDateEpoch = null
                                            selectedFilterEndDateEpoch = null
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Reset Semua", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Results Count and Master Collapse Control
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val resultsCount = searchFilteredTransactions.size
                Text(
                    text = "Ditemukan: $resultsCount transaksi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                if (searchFilteredTransactions.isNotEmpty()) {
                    val groupedKeys = searchFilteredTransactions.groupBy { FormatUtils.formatDate(it.date) }.keys
                    val allExpanded = groupedKeys.isNotEmpty() && groupedKeys.all { expandedDates.contains(it) }
                    
                    TextButton(
                        onClick = {
                            if (allExpanded) {
                                expandedDates = emptySet()
                            } else {
                                expandedDates = groupedKeys
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = if (allExpanded) "Tutup Semua" else "Buka Semua",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        // Transactions list items
        if (searchFilteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val emptyStr = if (searchQuery.isNotEmpty() || minAmountQuery.isNotEmpty() || maxAmountQuery.isNotEmpty() || selectedFilterDateEpoch != null) {
                            "Tidak ada transaksi yang cocok dengan pencarian Anda."
                        } else {
                            when (activeDashboardFilter) {
                                "HARI" -> "Tidak ada transaksi hari ini."
                                "BULAN" -> "Tidak ada transaksi di bulan ini."
                                "TAHUN" -> "Tidak ada transaksi di tahun ini."
                                else -> "Tidak ada transaksi."
                            }
                        }
                        Text(
                            text = "$emptyStr\nTekan '+' atau perbaiki filter pencarian.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val groupedTransactions = searchFilteredTransactions.groupBy { FormatUtils.formatDate(it.date) }
            groupedTransactions.forEach { (dateStr, dayTx) ->
                // Date Header with Daily Total
                item(key = "header_$dateStr") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("date_header_$dateStr")
                            .animateContentSize(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedDates = if (expandedDates.contains(dateStr)) {
                                        expandedDates - dateStr
                                    } else {
                                        expandedDates + dateStr
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandedDates.contains(dateStr)) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                    contentDescription = "Toggle Collapse",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val dailyPemasukan = dayTx.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
                            val dailyPengeluaran = dayTx.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
                            val dailyNet = dailyPemasukan - dailyPengeluaran
                            val netColor = if (dailyNet > 0) Color(0xFF2E7D32) else if (dailyNet < 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                            val netPrefix = if (dailyNet > 0) "+" else ""

                            Text(
                                text = "Total: $netPrefix${FormatUtils.formatRupiah(dailyNet)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = netColor
                            )
                        }
                    }
                }

                // Show items if expanded
                if (expandedDates.contains(dateStr)) {
                    items(dayTx, key = { "tx_${it.id}" }) { tx ->
                        val category = categories.find { it.id == tx.categoryId }
                        val account = accounts.find { it.id == tx.accountId }
                        val destAccount = tx.destAccountId?.let { id -> accounts.find { id == it.id } }

                        val textHexColor = category?.colorHex ?: "#9E9E9E"
                        val catColor = runCatching { Color(android.graphics.Color.parseColor(textHexColor)) }.getOrDefault(Color.Gray)

                        val amountColor = when (tx.type) {
                            "PEMASUKAN" -> Color(0xFF4CAF50)
                            "PENGELUARAN" -> Color(0xFFF44336)
                            else -> Color(0xFF2196F3) // TRANSFER
                        }

                        val amountPrefix = when (tx.type) {
                            "PEMASUKAN" -> "+"
                            "PENGELUARAN" -> "-"
                            "TRANSFER" -> ""
                            else -> ""
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                                .testTag("transaction_item_${tx.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Circle Icon left (high density - 32.dp)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(category?.iconName ?: "Category"),
                                        contentDescription = category?.name ?: "Kategori",
                                        tint = catColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Middle details (high density)
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (tx.type == "TRANSFER") {
                                            "Transfer: ${account?.name ?: "Asal"} ➔ ${destAccount?.name ?: "Tujuan"}"
                                        } else {
                                            category?.name ?: "Kategori"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (tx.notes.isNotEmpty()) {
                                         val rawNotes = tx.notes
                                         var subCatLabel2 = ""
                                         var cleanNotes2 = rawNotes
                                         if (rawNotes.startsWith("[") && rawNotes.contains("]")) {
                                             val endIdx = rawNotes.indexOf("]")
                                             if (endIdx > 1) {
                                                 subCatLabel2 = rawNotes.substring(1, endIdx)
                                                 cleanNotes2 = rawNotes.substring(endIdx + 1).trim()
                                             }
                                         }
                                         
                                         Row(verticalAlignment = Alignment.CenterVertically) {
                                             if (subCatLabel2.isNotEmpty()) {
                                                 Box(
                                                     modifier = Modifier
                                                         .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                         .padding(horizontal = 4.dp, vertical = 1.dp)
                                                 ) {
                                                     Text(
                                                         text = subCatLabel2,
                                                         style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                         color = MaterialTheme.colorScheme.onSecondaryContainer
                                                     )
                                                 }
                                                 Spacer(modifier = Modifier.width(4.dp))
                                             }
                                             if (cleanNotes2.isNotEmpty()) {
                                                 Text(
                                                     text = cleanNotes2,
                                                     style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                     maxLines = 1,
                                                     overflow = TextOverflow.Ellipsis
                                                 )
                                             }
                                         }
                                     } else if (false && tx.notes.isNotEmpty()) {
                                        Text(
                                            text = tx.notes,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "Akun: ${account?.name ?: "Dompet"} | ${FormatUtils.formatDateShort(tx.date)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                // Right Amount & Delete
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "$amountPrefix${FormatUtils.formatRupiah(tx.amount)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                        color = amountColor,
                                        modifier = Modifier.testTag("tx_amount_${tx.id}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { onEditTransaction(tx) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("edit_tx_btn_${tx.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteTransaction(tx) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("delete_tx_btn_${tx.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (isAddTargetOpen) {
        AddSavingTargetDialog(
            accounts = accounts,
            onDismiss = { isAddTargetOpen = false },
            onSave = { name, amount, sourceId ->
                onAddSavingTarget(name, amount, sourceId)
                isAddTargetOpen = false
            }
        )
    }

    if (activeTargetForDeposit != null) {
        val target = activeTargetForDeposit!!
        SaveToTargetDialog(
            accounts = accounts,
            targetName = target.name,
            onDismiss = { activeTargetForDeposit = null },
            onSave = { sourceId, amount ->
                onSaveToTarget(target.id, sourceId, amount)
                activeTargetForDeposit = null
            }
        )
    }

    if (activeAccountForQuickTransfer != null) {
        val srcAccount = activeAccountForQuickTransfer!!
        QuickTransferDialog(
            sourceAccount = srcAccount,
            accounts = accounts,
            savingTargets = savingTargets,
            onDismiss = { activeAccountForQuickTransfer = null },
            onSaveToTarget = { targetId, amount ->
                onSaveToTarget(targetId, srcAccount.id, amount)
                activeAccountForQuickTransfer = null
            },
            onTransfer = { destAccountId, amount, transferFee ->
                onTransfer(srcAccount.id, destAccountId, amount, transferFee)
                activeAccountForQuickTransfer = null
            },
            onAdjustAsset = { amount, isPemasukan ->
                onAdjustAsset(srcAccount.id, amount, isPemasukan)
                activeAccountForQuickTransfer = null
            }
        )
    }

    if (historyAccountForPopup != null) {
        val account = historyAccountForPopup!!
        val accountTransactions = allTransactions.filter { it.accountId == account.id || it.destAccountId == account.id }.sortedByDescending { it.date }
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { historyAccountForPopup = null }) {
            Card(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("account_history_popup"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Riwayat ${account.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    if (accountTransactions.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat transaksi", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(accountTransactions) { tx ->
                                val cat = categories.find { it.id == tx.categoryId }
                                val isPemasukan = tx.type == "PEMASUKAN" || (tx.type == "TRANSFER" && tx.destAccountId == account.id)
                                val amountColor = if (isPemasukan) Color(0xFF2E7D32) else Color(0xFFC62828)
                                val prefix = if (tx.type == "TRANSFER") "➔ " else if (isPemasukan) "+ " else "- "
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { historyAccountForPopup = null; onEditTransaction(tx) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (tx.type == "TRANSFER") "Transfer" else cat?.name ?: "Unknown",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            if (tx.notes.isNotBlank()) {
                                                Text(text = tx.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            Text(
                                                text = com.example.ui.util.FormatUtils.formatDate(tx.date),
                                                style = MaterialTheme.typography.labelSmall, color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = "$prefix${com.example.ui.util.FormatUtils.formatRupiah(tx.amount)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = amountColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { historyAccountForPopup = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickTransferDialog(
    sourceAccount: Account,
    accounts: List<Account>,
    savingTargets: List<SavingTarget>,
    onDismiss: () -> Unit,
    onSaveToTarget: (Int, Long) -> Unit,
    onTransfer: (String, Long, Long) -> Unit,
    onAdjustAsset: (Long, Boolean) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Ke Target Tabungan, 1: Ke Akun Lain
    
    // For Saving Targets option
    var selectedTargetId by remember { mutableStateOf(savingTargets.firstOrNull()?.id ?: -1) }
    
    // For Other Accounts option
    val otherAccounts = remember(accounts, sourceAccount) { accounts.filter { it.id != sourceAccount.id } }
    var selectedDestAccountId by remember { mutableStateOf(otherAccounts.firstOrNull()?.id ?: "") }

    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
    
    var isPemasukan by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != 1) {
            hasTransferFee = false
            transferFeeText = ""
        }
    }

    val themeAccentColor = Color(0xFF1565C0) // Sapphire Blue for Transfer

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("quick_transfer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Details
                Text(
                    text = "Kirim / Transfer Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = themeAccentColor
                )

                // Source Card Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = themeAccentColor.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeAccentColor.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Transfer dari dompet:",
                                fontSize = 10.sp,
                                color = themeAccentColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = sourceAccount.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = FormatUtils.formatRupiah(sourceAccount.balance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = themeAccentColor
                        )
                    }
                }

                // Tab Switcher for types
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        0 to "🎯 Tabungan",
                        1 to "💳 Transfer",
                        2 to "⚖️ Koreksi"
                    )
                    tabs.forEach { (tabIdx, label) ->
                        val isSel = selectedTab == tabIdx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) themeAccentColor else Color.Transparent)
                                .clickable { selectedTab = tabIdx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 1: To Saving Targets
                if (selectedTab == 0) {
                    Column {
                        Text(
                            text = "Pilih Target Tabungan Utama:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (savingTargets.isEmpty()) {
                            Text(
                                "Belum ada target tabungan impian aktif.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        } else {
                            var targetExpanded by remember { mutableStateOf(false) }
                            var targetTriggerWidth by remember { mutableStateOf(0) }
                            val selectedTargetObj = savingTargets.find { it.id == selectedTargetId } ?: savingTargets.firstOrNull()

                            // Keep selectedTargetId in sync
                            if (selectedTargetObj != null && selectedTargetId != selectedTargetObj.id) {
                                selectedTargetId = selectedTargetObj.id
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        targetTriggerWidth = coordinates.size.width
                                    }
                            ) {
                                OutlinedCard(
                                    onClick = { targetExpanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("quick_transfer_target_trigger"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedTargetObj != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(themeAccentColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "🎯",
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = selectedTargetObj.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Sisa: ${FormatUtils.formatRupiah(selectedTargetObj.targetAmount - (accounts.find { it.id == selectedTargetObj.sourceAccountId }?.balance ?: 0L))}",
                                                        fontSize = 11.sp,
                                                        color = themeAccentColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Pilih Target Tabungan...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (targetExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                androidx.compose.material3.DropdownMenu(
                                    expanded = targetExpanded,
                                    onDismissRequest = { targetExpanded = false },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { targetTriggerWidth.toDp() })
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    savingTargets.forEach { target ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(themeAccentColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "🎯",
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = target.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "Sisa: ${FormatUtils.formatRupiah(target.targetAmount - (accounts.find { it.id == target.sourceAccountId }?.balance ?: 0L))}",
                                                            fontSize = 11.sp,
                                                            color = themeAccentColor
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedTargetId = target.id
                                                targetExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // Option 2: To Other Accounts
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pilih Rekening / Dompet Penerima:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (otherAccounts.isEmpty()) {
                            Text(
                                "Tidak ada rekening penerima lain.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        } else {
                            var destAccountExpanded by remember { mutableStateOf(false) }
                            var destTriggerWidth by remember { mutableStateOf(0) }
                            val selectedDestAccountObj = otherAccounts.find { it.id == selectedDestAccountId } ?: otherAccounts.firstOrNull()

                            // Keep selectedDestAccountId in sync
                            if (selectedDestAccountObj != null && selectedDestAccountId != selectedDestAccountObj.id) {
                                selectedDestAccountId = selectedDestAccountObj.id
                            }

                            val destAcctColor = selectedDestAccountObj?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrDefault(Color.Gray) } ?: Color.Gray

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        destTriggerWidth = coordinates.size.width
                                    }
                            ) {
                                OutlinedCard(
                                    onClick = { destAccountExpanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("quick_transfer_dest_account_trigger"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedDestAccountObj != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(destAcctColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = IconsMap.getIcon(selectedDestAccountObj.iconName),
                                                        contentDescription = selectedDestAccountObj.name,
                                                        tint = destAcctColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = selectedDestAccountObj.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = FormatUtils.formatRupiah(selectedDestAccountObj.balance),
                                                        fontSize = 11.sp,
                                                        color = destAcctColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Pilih Tujuan Rekening...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (destAccountExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                androidx.compose.material3.DropdownMenu(
                                    expanded = destAccountExpanded,
                                    onDismissRequest = { destAccountExpanded = false },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { destTriggerWidth.toDp() })
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    otherAccounts.forEach { acct ->
                                        val acColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(acColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = IconsMap.getIcon(acct.iconName),
                                                            contentDescription = acct.name,
                                                            tint = acColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = acct.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = FormatUtils.formatRupiah(acct.balance),
                                                            fontSize = 11.sp,
                                                            color = acColor
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedDestAccountId = acct.id
                                                destAccountExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Checkbox Admin Fee inside Option 2!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = hasTransferFee,
                                        onCheckedChange = { hasTransferFee = it },
                                        colors = CheckboxDefaults.colors(checkedColor = themeAccentColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { hasTransferFee = !hasTransferFee }
                                    ) {
                                        Text(
                                            text = "Tambah Biaya Transfer (Admin Fee)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Centang jika transfer dikenakan biaya admin",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (hasTransferFee) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = transferFeeText,
                                        onValueChange = { input ->
                                            if (input.all { it.isDigit() }) {
                                                if (input.length <= 10) transferFeeText = input
                                            }
                                        },
                                        label = { Text("Biaya Admin (Rupiah)", fontSize = 11.sp) },
                                        placeholder = { Text("2500") },
                                        prefix = { Text("Rp ", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = themeAccentColor,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedLabelColor = themeAccentColor
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // Option 3: Asset Adjustment
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Koreksi Saldo Aktual:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val transferFee = if (selectedTab == 1 && hasTransferFee) (transferFeeText.toLongOrNull() ?: 0L) else 0L
                val amountVal = amountText.toLongOrNull() ?: 0L
                val totalRequiredAmount = amountVal + transferFee
                val isAmountOverBalance = if (selectedTab == 2) false else totalRequiredAmount > sourceAccount.balance

                // Nominal Amount Input field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanFinancialInput(it) },
                    label = { Text(if (selectedTab == 2) "Saldo Aktual (Rp)" else "Nominal Transfer (Rp)") },
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ") },
                    isError = isAmountOverBalance,
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    supportingText = {
                        if (isAmountOverBalance) {
                            Text("Saldo dompet tidak mencukupi! Tersedia: ${FormatUtils.formatRupiah(sourceAccount.balance)}", color = MaterialTheme.colorScheme.error)
                        } else {
                            val hintText = if (selectedTab == 2) {
                                "Masukkan nominal saldo yang sebenarnya di dompet ini."
                            } else {
                                "Masukkan nominal saldo yang ingin ditransfer" + if (transferFee > 0) " + Biaya Admin " + FormatUtils.formatRupiah(transferFee) else ""
                            }
                            Text(
                                hintText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = themeAccentColor,
                        focusedLabelColor = themeAccentColor,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                // Actions controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (!isAmountOverBalance) {
                                if (selectedTab == 0 && selectedTargetId != -1 && amountVal > 0L) {
                                    onSaveToTarget(selectedTargetId, amountVal)
                                } else if (selectedTab == 1 && selectedDestAccountId.isNotEmpty() && amountVal > 0L) {
                                    val feeVal = if (hasTransferFee) (transferFeeText.toLongOrNull() ?: 0L) else 0L
                                    onTransfer(selectedDestAccountId, amountVal, feeVal)
                                } else if (selectedTab == 2) {
                                    val selisih = amountVal - sourceAccount.balance
                                    if (selisih != 0L) {
                                        onAdjustAsset(Math.abs(selisih), selisih > 0)
                                    } else {
                                        onDismiss() // No change needed if same
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = (!isAmountOverBalance && (
                            (selectedTab == 0 && selectedTargetId != -1 && amountText.isNotEmpty()) || 
                            (selectedTab == 1 && selectedDestAccountId.isNotEmpty() && amountText.isNotEmpty()) ||
                            (selectedTab == 2 && amountText.isNotEmpty())
                        ))
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddSavingTargetDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }

    val themeAccentColor = Color(0xFF2E7D32) // Soft Emerald Green for savings goal

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("add_saving_target_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Buat Target Tabungan Baru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = themeAccentColor
                )

                val isNameInvalid = name.isNotEmpty() && name.trim().isEmpty()
                val isAmountInvalid = targetAmountText.isNotEmpty() && (targetAmountText.toLongOrNull() ?: 0L) <= 0L

                // Goal Name Form Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Impian (misal: Beli Laptop Asus)") },
                    placeholder = { Text("misal: Liburan ke Bali") },
                    isError = isNameInvalid,
                    supportingText = {
                        if (isNameInvalid) {
                            Text("Nama impian tidak boleh berupa spasi saja!", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Contoh: Beli Playstation 5, Liburan ke Bali", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = themeAccentColor,
                        focusedLabelColor = themeAccentColor,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                // Goal Target Amount Form Field
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = cleanFinancialInput(it) },
                    label = { Text("Target Nominal Tabungan (Rp)") },
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ") },
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    isError = isAmountInvalid,
                    supportingText = {
                        if (isAmountInvalid) {
                            Text("Nominal target tabungan harus lebih dari Rp 0", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Tentukan target nominal dana yang ingin dikumpulkan", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = themeAccentColor,
                        focusedLabelColor = themeAccentColor,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                // Source Wallet Selector (Highly Elegant Cards Row)
                Column {
                    Text(
                        text = "Pilih Rekening yang Diikuti:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acct ->
                            val isSel = selectedAccountId == acct.id
                            val acctColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAccountId = acct.id },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) acctColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) acctColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(acct.iconName),
                                        contentDescription = acct.name,
                                        tint = if (isSel) acctColor else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        acct.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val amount = targetAmountText.toLongOrNull() ?: 0L
                            if (name.isNotBlank() && amount > 0L && selectedAccountId.isNotBlank()) {
                                onSave(name, amount, selectedAccountId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                        enabled = name.isNotBlank() && (targetAmountText.toLongOrNull() ?: 0L) > 0L && selectedAccountId.isNotBlank(),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Buat Target", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SaveToTargetDialog(
    accounts: List<Account>,
    targetName: String,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }

    val themeAccentColor = Color(0xFF1565C0) // Custom Sapphire Blue theme

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("save_to_target_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details
                Text(
                    text = "Menabung Impian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = themeAccentColor
                )
                Text(
                    text = "Mempersiapkan dana untuk:\n\"$targetName\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                val selectedAccount = accounts.find { it.id == selectedAccountId }
                val isAmountOverBalance = (amountText.toLongOrNull() ?: 0L) > (selectedAccount?.balance ?: 0L)

                // Input target nominal field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanFinancialInput(it) },
                    label = { Text("Nominal Menabung (Rp)") },
                    placeholder = { Text("0") },
                    prefix = { Text("Rp ") },
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    isError = isAmountOverBalance,
                    supportingText = {
                        if (isAmountOverBalance) {
                            Text("Saldo dompet tidak mencukupi! Tersedia: ${FormatUtils.formatRupiah(selectedAccount?.balance ?: 0L)}", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Masukkan jumlah dana yang ingin disisihkan ke target impian", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeAccentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = themeAccentColor,
                        focusedLabelColor = themeAccentColor,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                // Selection grid module
                Column {
                    Text(
                        text = "Gunakan Saldo Dari Dompet:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acct ->
                            val isSel = selectedAccountId == acct.id
                            val acctColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                            
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedAccountId = acct.id },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) acctColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) acctColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(acct.iconName),
                                        contentDescription = acct.name,
                                        tint = if (isSel) acctColor else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        acct.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        FormatUtils.formatRupiah(acct.balance),
                                        fontSize = 9.sp,
                                        color = if (isSel) acctColor else Color.Gray.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            if (amount > 0L && selectedAccountId.isNotBlank() && !isAmountOverBalance) {
                                onSave(selectedAccountId, amount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = themeAccentColor),
                        enabled = (amountText.toLongOrNull() ?: 0L) > 0L && selectedAccountId.isNotBlank() && !isAmountOverBalance,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Mulai Tabung", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsPanel(
    allTransactions: List<Transaction>,
    categories: List<Category>,
    subCategories: List<SubCategory>,
    accounts: List<Account>,
    monthLabel: String,
    monthOffset: Int,
    onNextMonth: () -> Unit,
    onPrevMonth: () -> Unit
) {
    var selectedStatType by remember { mutableStateOf("PENGELUARAN") } // PENGELUARAN, PEMASUKAN, or GABUNGAN
    var selectedPeriod by remember { mutableStateOf("BULAN") } // "BULAN", "TAHUN", or "RENTANG"
    var selectedAccountFilter by remember { mutableStateOf<String?>(null) } // accountId

    val context = androidx.compose.ui.platform.LocalContext.current
    var customStartDate by remember { mutableStateOf(System.currentTimeMillis() - 30 * 24 * 3600 * 1000L) } // Default to 30 days ago
    var customEndDate by remember { mutableStateOf(System.currentTimeMillis()) } // Default to today

    val indonLocale = java.util.Locale("id", "ID")
    val monthFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", indonLocale) }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Independent monthly/yearly calendar state for Statistics panel
    var statisticsMonthCal by remember {
        mutableStateOf(Calendar.getInstance().apply {
            // Apply monthOffset on top of current date as requested (defaulting to the current dashboard month context)
            add(Calendar.MONTH, monthOffset)
        })
    }

    val currentMonthName = remember(statisticsMonthCal) {
        monthFormat.format(statisticsMonthCal.time)
    }

    // Calculate dynamic transaction list depending on period
    val filteredTransactionsByPeriod = remember(allTransactions, selectedPeriod, customStartDate, customEndDate, statisticsMonthCal, selectedAccountFilter) {
        val baseFiltered = when (selectedPeriod) {
            "BULAN" -> {
                val targetYear = statisticsMonthCal.get(Calendar.YEAR)
                val targetMonth = statisticsMonthCal.get(Calendar.MONTH)
                allTransactions.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.YEAR) == targetYear && txCal.get(Calendar.MONTH) == targetMonth
                }
            }
            "TAHUN" -> {
                val targetYear = statisticsMonthCal.get(Calendar.YEAR)
                allTransactions.filter { tx ->
                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                    txCal.get(Calendar.YEAR) == targetYear
                }
            }
            "RENTANG" -> {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = customStartDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = customEndDate
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                allTransactions.filter { it.date in startCal..endCal }
            }
            else -> allTransactions
        }
        
        if (selectedAccountFilter == null) {
            baseFiltered
        } else {
            baseFiltered.filter { it.accountId == selectedAccountFilter || it.destAccountId == selectedAccountFilter }
        }
    }

    val filteredByTypeAndPeriod = remember(filteredTransactionsByPeriod, selectedStatType) {
        if (selectedStatType == "GABUNGAN") {
            filteredTransactionsByPeriod.filter { it.type == "PEMASUKAN" || it.type == "PENGELUARAN" }
        } else {
            filteredTransactionsByPeriod.filter { it.type == selectedStatType }
        }
    }

    val totalAmount = remember(filteredByTypeAndPeriod) {
        filteredByTypeAndPeriod.sumOf { it.amount }
    }

    val categoryShares = remember(filteredByTypeAndPeriod, categories) {
        if (totalAmount == 0L) emptyList() else {
            val grouped = filteredByTypeAndPeriod.groupBy { it.categoryId }
            grouped.map { (catId, txs) ->
                val fallbackType = txs.firstOrNull()?.type ?: "PENGELUARAN"
                val cat = categories.find { it.id == catId } ?: Category(catId, catId, "QuestionMark", fallbackType, "#9E9E9E")
                val amount = txs.sumOf { it.amount }
                
                val subCatGrouped = txs.groupBy { it.subCategoryId }
                val subCatShares = subCatGrouped.map { (subCatId, subTxs) ->
                    val subCat = subCategories.find { it.id == subCatId }
                    com.example.ui.viewmodel.SubCategoryShare(
                        subCategory = subCat,
                        amount = subTxs.sumOf { it.amount }
                    )
                }.sortedByDescending { it.amount }
                
                com.example.ui.viewmodel.CategoryShare(
                    category = cat,
                    amount = amount,
                    percentage = (amount.toDouble() / totalAmount.toDouble()) * 100.0,
                    subCategoryShares = subCatShares
                )
            }.sortedByDescending { it.amount }
        }
    }

    val displayShares = remember(categoryShares, selectedStatType) {
        if (categoryShares.size <= 5) {
            categoryShares
        } else {
            val topShares = categoryShares.take(4)
            val otherShares = categoryShares.drop(4)
            val otherAmount = otherShares.sumOf { it.amount }
            val otherPercentage = otherShares.sumOf { it.percentage }
            
            val otherCategory = Category(
                id = "lainnya",
                name = "Lainnya",
                iconName = "Category",
                type = selectedStatType,
                colorHex = "#757575"
            )
            
            val otherShare = com.example.ui.viewmodel.CategoryShare(
                category = otherCategory,
                amount = otherAmount,
                percentage = otherPercentage
            )
            
            topShares + otherShare
        }
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Toggle Pemasukan vs Pengeluaran vs Gabungan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            listOf("PENGELUARAN", "PEMASUKAN", "GABUNGAN").forEach { t ->
                val isSel = selectedStatType == t
                val activeBgColor = when (t) {
                    "PENGELUARAN" -> Color(0xFFC62828)
                    "PEMASUKAN" -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) activeBgColor else Color.Transparent)
                        .clickable { selectedStatType = t }
                        .padding(vertical = 10.dp)
                        .testTag("stat_type_$t"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (t) {
                            "PENGELUARAN" -> "Pengeluaran"
                            "PEMASUKAN" -> "Pemasukan"
                            else -> "Gabungan"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Segmented Period Option Tabs Row (BULAN vs TAHUN vs RENTANG)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
            listOf(
                "BULAN" to "Bulan Pilihan",
                "TAHUN" to "Tahun Pilihan",
                "RENTANG" to "Rentang Bebas"
            ).forEach { (key, label) ->
                val isSel = selectedPeriod == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { selectedPeriod = key }
                        .padding(vertical = 10.dp)
                        .testTag("stat_period_$key"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when(key) {
                                "BULAN" -> Icons.Default.CalendarToday
                                "TAHUN" -> Icons.Default.Today
                                else -> Icons.Default.DateRange
                            },
                            contentDescription = null,
                            tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Account Filter Row
        var expandedAccountFilter by remember { mutableStateOf(false) }
        val selectedAccountName = if (selectedAccountFilter == null) "Semua Dompet" else accounts.find { it.id == selectedAccountFilter }?.name ?: "Semua Dompet"
        val selectedColor = if (selectedAccountFilter == null) MaterialTheme.colorScheme.primary else runCatching { Color(android.graphics.Color.parseColor(accounts.find { it.id == selectedAccountFilter }?.colorHex ?: "#808080")) }.getOrDefault(Color.Gray)
        
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box {
                Card(
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).clickable { expandedAccountFilter = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = selectedColor.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, selectedColor.copy(alpha = 0.3f)),
                    elevation = CardDefaults.elevatedCardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = selectedColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedAccountName, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = selectedColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = selectedColor, modifier = Modifier.size(18.dp))
                    }
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = expandedAccountFilter,
                    onDismissRequest = { expandedAccountFilter = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Semua Dompet", style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            selectedAccountFilter = null
                            expandedAccountFilter = false
                        }
                    )
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                selectedAccountFilter = account.id
                                expandedAccountFilter = false
                            }
                        )
                    }
                }
            }
        }

        // Custom Date Range Picker panel (only display if RENTANG selected)
        if (selectedPeriod == "RENTANG") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pilih Periode Kustom:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = customStartDate }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val c = Calendar.getInstance().apply { set(y, m, d) }
                                        customStartDate = c.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mulai", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(FormatUtils.formatDate(customStartDate), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
                            }
                        }

                        Button(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = customEndDate }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val c = Calendar.getInstance().apply { set(y, m, d) }
                                        customEndDate = c.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Akhir", fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(FormatUtils.formatDate(customEndDate), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }

        // Month selector (only display if BULAN selected)
        if (selectedPeriod == "BULAN") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = statisticsMonthCal.timeInMillis
                            add(Calendar.MONTH, -1)
                        }
                        statisticsMonthCal = newCal
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = statisticsMonthCal.timeInMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val c = Calendar.getInstance().apply { set(y, m, d) }
                                        statisticsMonthCal = c
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = currentMonthName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ketuk untuk memilih bulan bebas",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = statisticsMonthCal.timeInMillis
                            add(Calendar.MONTH, 1)
                        }
                        statisticsMonthCal = newCal
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Selanjutnya")
                    }
                }
            }
        }

        // Year selector (only display if TAHUN selected)
        if (selectedPeriod == "TAHUN") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = statisticsMonthCal.timeInMillis
                            add(Calendar.YEAR, -1)
                        }
                        statisticsMonthCal = newCal
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Tahun Sebelumnya")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = statisticsMonthCal.timeInMillis }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val c = Calendar.getInstance().apply { set(y, m, d) }
                                        statisticsMonthCal = c
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Tahun ${statisticsMonthCal.get(Calendar.YEAR)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Ketuk untuk memilih tahun bebas",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    IconButton(onClick = {
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = statisticsMonthCal.timeInMillis
                            add(Calendar.YEAR, 1)
                        }
                        statisticsMonthCal = newCal
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Tahun Selanjutnya")
                    }
                }
            }
        }

        // Custom Donut Pie Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val chartTitle = when (selectedStatType) {
                    "PEMASUKAN" -> "Distribusi Pemasukan"
                    "GABUNGAN" -> "Distribusi Keuangan (Gabungan)"
                    else -> "Distribusi Pengeluaran"
                }
                Text(
                    text = chartTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (displayShares.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    CustomPieChart(
                        shares = displayShares,
                        statType = selectedStatType,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        // Categories List Card Details
        val detailTitle = when (selectedStatType) {
            "PEMASUKAN" -> "Rincian Kategori Pemasukan"
            "GABUNGAN" -> "Rincian Kategori (Gabungan)"
            else -> "Rincian Kategori Pengeluaran"
        }
        Text(
            text = detailTitle,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (displayShares.isEmpty()) {
                val emptyDetailMsg = when (selectedStatType) {
                    "PEMASUKAN" -> "Belum ada rincian pemasukan pada periode ini."
                    "GABUNGAN" -> "Belum ada rincian transaksi pada periode ini."
                    else -> "Belum ada rincian pengeluaran pada periode ini."
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyDetailMsg,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayShares.forEach { share ->
                        var expanded by remember(share.category.id) { mutableStateOf(false) }
                        val hasSubCategories = share.subCategoryShares.isNotEmpty()
                        val catHex = share.category.colorHex
                        val catColor = runCatching { Color(android.graphics.Color.parseColor(catHex)) }.getOrDefault(Color.Gray)
                        
                        val currentType = if (selectedStatType == "GABUNGAN") share.category.type else selectedStatType
                        val numColor = if (currentType == "PEMASUKAN") Color(0xFF2E7D32) else Color(0xFFC62828)
                        val prefix = if (currentType == "PEMASUKAN") "+" else "-"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (hasSubCategories) Modifier.clickable { expanded = !expanded } else Modifier)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = share.category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (selectedStatType == "GABUNGAN") {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (currentType == "PEMASUKAN") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (currentType == "PEMASUKAN") "Pemasukan" else "Pengeluaran",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (currentType == "PEMASUKAN") Color(0xFF2E7D32) else Color(0xFFC62828)
                                            )
                                        )
                                    }
                                }
                                if (currentType != "PEMASUKAN" && share.category.budgetLimit > 0) {
                                    val limit = share.category.budgetLimit
                                    val pct = (share.amount / limit) * 100
                                    val warnText = if (pct >= 100) "Melebihi " else "Mendekati "
                                    
                                    if (pct >= 80) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Warning",
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${warnText}Batas Anggaran!",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        androidx.compose.material3.LinearProgressIndicator(
                                            progress = { (share.amount.toFloat() / limit.toFloat()).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .padding(top = 4.dp)
                                                .height(4.dp),
                                            color = Color(0xFFE65100),
                                            trackColor = Color(0xFFFFF3E0),
                                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        )
                                    }
                                }
                                 Text(
                                text = "${String.format("%.1f", share.percentage)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "$prefix${FormatUtils.formatRupiah(share.amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = numColor
                            )
                            if (hasSubCategories) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle",
                                    modifier = Modifier.padding(start = 4.dp).size(20.dp),
                                    tint = Color.Gray
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        }

                        if (expanded && hasSubCategories) {
                            Column(modifier = Modifier.fillMaxWidth().padding(start = 22.dp, bottom = 4.dp)) {
                                share.subCategoryShares.forEach { subShare ->
                                    val subName = subShare.subCategory?.name ?: "Tanpa Sub"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "↳ $subName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        Text(text = FormatUtils.formatRupiah(subShare.amount), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
            }
        }
    }
}
}
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        if (c == '"') {
            if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                current.append('"')
                i++
            } else {
                inQuotes = !inQuotes
            }
        } else if (c == ',' && !inQuotes) {
            result.add(current.toString())
            current.setLength(0)
        } else {
            current.append(c)
        }
        i++
    }
    result.add(current.toString())
    return result
}

private fun convertToXlsxXml(csvContent: String): String {
    val builder = StringBuilder()
    builder.append("<?xml version=\"1.0\"?>\n")
    builder.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
    builder.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
    builder.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
    builder.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
    builder.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
    builder.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")
    builder.append(" <Worksheet ss:Name=\"Transaksi Keuangan\">\n")
    builder.append("  <Table>\n")
    
    val lines = csvContent.split("\n")
    for (line in lines) {
        if (line.trim().isBlank()) continue
        builder.append("   <Row>\n")
        val cells = parseCsvLine(line)
        for (cell in cells) {
            val isNumeric = cell.toLongOrNull() != null || cell.toLongOrNull() != null
            val typeAttr = if (isNumeric) "ss:Type=\"Number\"" else "ss:Type=\"String\""
            val escapedCell = cell.replace("&", "&amp;")
                                  .replace("<", "&lt;")
                                  .replace(">", "&gt;")
                                  .replace("\"", "&quot;")
                                  .replace("'", "&apos;")
            builder.append("    <Cell><Data $typeAttr>$escapedCell</Data></Cell>\n")
        }
        builder.append("   </Row>\n")
    }
    
    builder.append("  </Table>\n")
    builder.append(" </Worksheet>\n")
    builder.append("</Workbook>")
    return builder.toString()
}

@Composable
private fun SettingsSubHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        ),
        modifier = modifier.padding(top = 8.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun CollapsibleSettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Sembunyikan" else "Tampilkan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean,
    supportingText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    prefix: @Composable (() -> Unit)? = null,
    testTag: String = "",
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isError,
        supportingText = supportingText,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon ?: {
            if (isError) {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
            }
        },
        prefix = prefix,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            errorBorderColor = MaterialTheme.colorScheme.error
        )
    )
}

@Composable
fun SettingsPanel(
    accounts: List<Account>,
    categories: List<Category>,
    subCategories: List<SubCategory>,
    recurringTransactions: List<RecurringTransaction>,
    themeMode: com.example.data.repository.ThemeMode,
    appColorHex: String,
    globalBudgetLimit: Long,
    onGlobalBudgetLimitChange: (Long) -> Unit,
    onThemeChange: (com.example.data.repository.ThemeMode) -> Unit,
    onAppColorChange: (String) -> Unit,
    onAddAccount: (String, Long) -> Unit,
    onAddCategory: (String, String, String, Long, String?) -> Unit,
    onDeleteAccount: (Account) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onEditAccount: (Account) -> Unit,
    onEditCategory: (Category) -> Unit,
    onAddSubCat: (String, String) -> Unit,
    onDeleteSubCat: (SubCategory) -> Unit,
    onUpdateSubCat: (SubCategory) -> Unit,
    onAddRecurringTransaction: (String, Long, String, String, String, Int, String) -> Unit,
    onDeleteRecurringTransaction: (RecurringTransaction) -> Unit,
    onUpdateRecurringTransaction: (RecurringTransaction) -> Unit,
    onSeedMockData: () -> Unit,
    onClearData: () -> Unit,
    onImportCsv: (String, (Boolean, String) -> Unit) -> Unit,
    onExportCsv: ((String) -> Unit) -> Unit,
    isGdriveLoggedIn: Boolean = false,
    onGdriveLoginChange: (Boolean) -> Unit = {},
    isAutoBackupEnabled: Boolean = false,
    onAutoBackupChange: (Boolean) -> Unit = {}
) {
    var acctName by remember { mutableStateOf("") }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var acctBalance by remember { mutableStateOf("") }

    var catName by remember { mutableStateOf("") }
    var catType by remember { mutableStateOf("PEMASUKAN") }
    var catIcon by remember { mutableStateOf("ShoppingCart") }
    var catBudgetLimit by remember { mutableStateOf("") }
    var catParentId by remember { mutableStateOf<String?>(null) }

    // Recurring Transaction state fields
    var recName by remember { mutableStateOf("") }
    var recAmount by remember { mutableStateOf("") }
    var recType by remember { mutableStateOf("PENGELUARAN") } // PEMASUKAN or PENGELUARAN
    var recDayOfMonth by remember { mutableStateOf("") }
    var recNotes by remember { mutableStateOf("") }
    var editingRecurring by remember { mutableStateOf<RecurringTransaction?>(null) }

    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    
    // Ensure selected account is valid
    if (selectedAccountId.isEmpty() && accounts.isNotEmpty()) {
        selectedAccountId = accounts.first().id
    }

    val filteredCategories = categories.filter { it.type == recType }
    var selectedCategoryId by remember(recType) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: "")
    }

    // Expandable states for collapsible categories
    var isDemoExpanded by rememberSaveable { mutableStateOf(false) }
    var isWalletExpanded by rememberSaveable { mutableStateOf(false) } // Closed by default
    var isCategoryExpanded by rememberSaveable { mutableStateOf(false) }
    var isRecurringExpanded by rememberSaveable { mutableStateOf(false) }
    var isBackupRestoreExpanded by rememberSaveable { mutableStateOf(false) }
    var isBudgetExpanded by rememberSaveable { mutableStateOf(false) }
    var isThemeExpanded by rememberSaveable { mutableStateOf(false) }

    var pastedCsvText by remember { mutableStateOf("") }
    var showManualPasteRow by rememberSaveable { mutableStateOf(false) }
    var exportFormat by rememberSaveable { mutableStateOf("CSV") }
    var loadingMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                loadingMessage = "Membaca file data..."
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val text = stream.bufferedReader().use { r -> r.readText() }
                    if (text.isNotBlank()) {
                        loadingMessage = "Memulihkan data transaksi..."
                        onImportCsv(text) { success, msg ->
                            loadingMessage = null
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        loadingMessage = null
                        Toast.makeText(context, "File kosong atau tidak dapat dibaca.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                loadingMessage = null
                Toast.makeText(context, "Gagal membuka file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tema / Tampilan
        item {
            CollapsibleSettingsCard(
                title = "Tampilan Tema & Warna",
                icon = Icons.Default.Palette,
                expanded = isThemeExpanded,
                onExpandedChange = { isThemeExpanded = it },
                testTag = "settings_col_theme"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    com.example.data.repository.ThemeMode.values().forEach { mode ->
                        val label = when(mode) {
                            com.example.data.repository.ThemeMode.LIGHT -> "Terang"
                            com.example.data.repository.ThemeMode.DARK -> "Gelap"
                            com.example.data.repository.ThemeMode.SYSTEM -> "Ikuti Sistem"
                        }
                        val isSelected = themeMode == mode
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { onThemeChange(mode) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Warna Aksen Aplikasi (Opsional)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExtendedColorPicker(
                    selectedColorHex = appColorHex,
                    onColorSelected = { onAppColorChange(it) }
                )
                if (appColorHex.isNotEmpty()) {
                    TextButton(
                        onClick = { onAppColorChange("") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Reset Warna Default", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Group 1: Statistik & Demo
        item {
            CollapsibleSettingsCard(
                title = "Statistik & Demo",
                icon = Icons.Default.Analytics,
                expanded = isDemoExpanded,
                onExpandedChange = { isDemoExpanded = it },
                testTag = "settings_col_demo"
            ) {
                Text(
                    text = "Isi aplikasi dengan data simulasi keuangan bulanan untuk melihat Pie Chart secara langsung.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSeedMockData,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("seed_demo_btn")
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = "Seed")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulasi Data")
                    }

                    OutlinedButton(
                        onClick = onClearData,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_db_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Data")
                    }
                }
            }
        }

        // Removed Global Budget Settings here

        // Group 2: Kelola Dompet Keuangan
        item {
            CollapsibleSettingsCard(
                title = "Kelola Dompet & Akun",
                icon = Icons.Default.AccountBalanceWallet,
                expanded = isWalletExpanded,
                onExpandedChange = { isWalletExpanded = it },
                testTag = "settings_col_wallet"
            ) {
                SettingsSubHeader("Daftar Dompet Aktif")
                
                if (accounts.isEmpty()) {
                    Text("Tidak ada dompet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    accounts.forEach { acct ->
                        val acctColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = IconsMap.getIcon(acct.iconName),
                                    contentDescription = acct.name,
                                    tint = acctColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = acct.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = FormatUtils.formatRupiah(acct.balance),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { editingAccount = acct },
                                    modifier = Modifier.size(36.dp).testTag("edit_account_btn_${acct.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Dompet",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (accounts.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteAccount(acct) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Dompet",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSubHeader("Tambah Akun Dompet Baru")
                
                val isAcctNameInvalid = acctName.isNotEmpty() && acctName.trim().isEmpty()
                val isBalanceInvalid = acctBalance.isNotEmpty() && acctBalance.toLongOrNull() == null

                SettingsTextField(
                    value = acctName,
                    onValueChange = { acctName = it },
                    label = "Nama Dompet (misal: Bank Mandiri)",
                    placeholder = "misal: Wallet Utama",
                    isError = isAcctNameInvalid,
                    supportingText = {
                        if (isAcctNameInvalid) {
                            Text("Nama dompet tidak boleh kosong / spasi saja", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Masukkan nama dompet atau nama kartu bank Anda", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    testTag = "acct_name_input"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsTextField(
                    value = acctBalance,
                    onValueChange = { acctBalance = cleanFinancialInput(it) },
                    label = "Saldo Awal (Rp)",
                    placeholder = "0",
                    isError = isBalanceInvalid,
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    supportingText = {
                        if (isBalanceInvalid) {
                            Text("Format nominal saldo awal tidak valid!", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Isi saldo cadangan awal pada dompet baru Anda", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "acct_balance_input"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val bal = acctBalance.toLongOrNull() ?: 0L
                        if (acctName.isNotBlank() && !isAcctNameInvalid && !isBalanceInvalid) {
                            onAddAccount(acctName, bal)
                            acctName = ""
                            acctBalance = ""
                        }
                    },
                    enabled = acctName.isNotBlank() && acctBalance.isNotBlank() && !isAcctNameInvalid && !isBalanceInvalid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_account")
                ) {
                    Text("Simpan Akun", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Group 3: Kelola Kategori Transaksi
        item {
            CollapsibleSettingsCard(
                title = "Kelola Kategori Transaksi",
                icon = Icons.Default.Category,
                expanded = isCategoryExpanded,
                onExpandedChange = { isCategoryExpanded = it },
                testTag = "settings_col_category"
            ) {
                SettingsSubHeader("Daftar Kategori Aktif")
                
                if (categories.isEmpty()) {
                    Text("Tidak ada kategori.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    val parentCats = categories.filter { it.parentId == null }.sortedBy { it.name }
                    val groupedCategories = mutableListOf<Category>()
                    parentCats.forEach { p ->
                        groupedCategories.add(p)
                        groupedCategories.addAll(categories.filter { it.parentId == p.id }.sortedBy { it.name })
                    }
                    val strayCats = categories.filter { it.parentId != null && parentCats.none { p -> p.id == it.parentId } }
                    groupedCategories.addAll(strayCats)

                    groupedCategories.forEach { cat ->
                        val catColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color.Gray)
                        val isChild = cat.parentId != null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isChild) {
                                    Spacer(modifier = Modifier.width(24.dp))
                                    Text("↳ ", color = Color.Gray, fontSize = 16.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = catColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (cat.type == "PEMASUKAN") "Pemasukan" else "Pengeluaran",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { editingCategory = cat },
                                    modifier = Modifier.size(36.dp).testTag("edit_category_btn_${cat.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Kategori",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteCategory(cat) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Kategori",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSubHeader("Tambah Kategori Baru")
                
                val isCatNameInvalid = catName.isNotEmpty() && catName.trim().isEmpty()

                SettingsTextField(
                    value = catName,
                    onValueChange = { catName = it },
                    label = "Nama Kategori (misal: Pulsa / Kopi)",
                    placeholder = "misal: Transportasi",
                    isError = isCatNameInvalid,
                    supportingText = {
                        if (isCatNameInvalid) {
                            Text("Nama kategori tidak boleh berupa spasi saja!", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Masukkan nama kategori pengeluaran atau pemasukan baru", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    },
                    testTag = "category_name_input"
                )
                if (catType == "PENGELUARAN") {
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsTextField(
                        value = catBudgetLimit,
                        onValueChange = { catBudgetLimit = cleanFinancialInput(it) },
                        label = "Batas Anggaran Bulanan (Opsional)",
                        placeholder = "misal: 1000000",
                        isError = false,
                        visualTransformation = IndonesianCurrencyVisualTransformation(),
                        supportingText = { Text("Mendapat notifikasi warna jika melebihi 80% dari batas") }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Tipe Kategori", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedFilterChip(
                        selected = catType == "PEMASUKAN",
                        onClick = { catType = "PEMASUKAN"; catParentId = null },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.weight(1f)
                    )
                    ElevatedFilterChip(
                        selected = catType == "PENGELUARAN",
                        onClick = { catType = "PENGELUARAN"; catParentId = null },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Sub-Kategori Dari (Opsional)", style = MaterialTheme.typography.labelMedium)
                val availableParents = categories.filter { it.type == catType && it.parentId == null }
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ElevatedFilterChip(
                            selected = catParentId == null,
                            onClick = { catParentId = null },
                            label = { Text("Tidak Ada") }
                        )
                    }
                    items(availableParents) { parent ->
                        ElevatedFilterChip(
                            selected = catParentId == parent.id,
                            onClick = { catParentId = parent.id },
                            label = { Text(parent.name) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Pilih Ikon Asosiasi", style = MaterialTheme.typography.labelMedium)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 4.dp)
                ) {
                    items(IconsMap.availableIcons) { icOpt ->
                        val isSelected = catIcon == icOpt.name
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable { catIcon = icOpt.name }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = IconsMap.getIcon(icOpt.name),
                                    contentDescription = icOpt.description,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(icOpt.description, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (catName.isNotBlank() && !isCatNameInvalid) {
                            val limit = catBudgetLimit.toLongOrNull() ?: 0L
                            onAddCategory(catName, catType, catIcon, limit, catParentId)
                            catName = ""
                            catBudgetLimit = ""
                            catParentId = null
                        }
                    },
                    enabled = catName.isNotBlank() && !isCatNameInvalid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_category")
                ) {
                    Text("Simpan Kategori", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Group 4: Transaksi Rutin Bulanan
        item {
            CollapsibleSettingsCard(
                title = "Transaksi Rutin Bulanan",
                icon = Icons.Default.Event,
                expanded = isRecurringExpanded,
                onExpandedChange = { isRecurringExpanded = it },
                testTag = "settings_col_recurring"
            ) {
                SettingsSubHeader("Daftar Transaksi Rutin Berjalan")
                
                if (recurringTransactions.isEmpty()) {
                    Text(
                        text = "Tidak ada transaksi rutin aktif berjalan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                } else {
                    recurringTransactions.forEach { r ->
                        val isExp = r.type == "PENGELUARAN"
                        val amtColor = if (isExp) Color(0xFFC62828) else Color(0xFF2E7D32)
                        val cat = categories.find { it.id == r.categoryId }
                        val catColor = runCatching { Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#9E9E9E")) }.getOrDefault(Color.Gray)
                        val acct = accounts.find { it.id == r.accountId }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(cat?.iconName ?: "QuestionMark"),
                                        contentDescription = r.name,
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = r.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = FormatUtils.formatRupiah(r.amount),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = amtColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Setiap tgl ${r.dayOfMonth}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (r.isPaused) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.Red.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("TERJEDA", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Dompet: ${acct?.name ?: "Unknown"} | Kat: ${cat?.name ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                    if (r.notes.isNotEmpty()) {
                                        Text(
                                            text = "Ket: ${r.notes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onUpdateRecurringTransaction(r.copy(isPaused = !r.isPaused)) },
                                    modifier = Modifier.size(36.dp).testTag("pause_recurring_${r.id}")
                                ) {
                                    Icon(
                                        imageVector = if (r.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = if (r.isPaused) "Aktifkan" else "Jeda",
                                        tint = if (r.isPaused) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { editingRecurring = r },
                                    modifier = Modifier.size(36.dp).testTag("edit_recurring_${r.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Ubah",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteRecurringTransaction(r) },
                                    modifier = Modifier.size(36.dp).testTag("delete_recurring_${r.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSubHeader("Tambah Transaksi Rutin Bulanan Baru")
                
                Text(text = "Tipe Transaksi", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedFilterChip(
                        selected = recType == "PEMASUKAN",
                        onClick = { recType = "PEMASUKAN" },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.weight(1f)
                    )
                    ElevatedFilterChip(
                        selected = recType == "PENGELUARAN",
                        onClick = { recType = "PENGELUARAN" },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                val isRecNameInvalid = recName.isNotEmpty() && recName.trim().isEmpty()
                val isRecAmountInvalid = recAmount.isNotEmpty() && recAmount.toLongOrNull() == null
                val recDayVal = recDayOfMonth.toIntOrNull()
                val isRecDayInvalid = recDayOfMonth.isNotEmpty() && (recDayVal == null || recDayVal !in 1..31)

                SettingsTextField(
                    value = recName,
                    onValueChange = { recName = it },
                    label = "Nama Transaksi Rutin",
                    placeholder = "misal: Biaya Kos / Langganan VPS",
                    isError = isRecNameInvalid,
                    supportingText = {
                        if (isRecNameInvalid) {
                            Text("Nama transaksi tidak boleh kosong", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    testTag = "recurring_name_input"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsTextField(
                    value = recAmount,
                    onValueChange = { recAmount = cleanFinancialInput(it) },
                    label = "Jumlah Nominal (Rp)",
                    placeholder = "0",
                    isError = isRecAmountInvalid,
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    supportingText = {
                        if (isRecAmountInvalid) {
                            Text("Nominal nominal tidak valid!", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "recurring_amount_input"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection
                Text(text = "Dompet Rujukan", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (accounts.isEmpty()) {
                    Text("Tidak ada dompet tersedia. Tambah akun dompet terlebih dahulu.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(accounts) { acct ->
                            val isSel = selectedAccountId == acct.id
                            val acctColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                            Card(
                                modifier = Modifier.clickable { selectedAccountId = acct.id },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) acctColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) acctColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = IconsMap.getIcon(acct.iconName),
                                        contentDescription = acct.name,
                                        tint = if (isSel) acctColor else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(acct.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Category Selection
                Text(text = "Pilih Kategori", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                if (filteredCategories.isEmpty()) {
                    Text("Tidak ada kategori rujukan.", color = Color.Gray, fontSize = 12.sp)
                } else {
                    if (selectedCategoryId.isEmpty() || !filteredCategories.any { it.id == selectedCategoryId }) {
                        selectedCategoryId = filteredCategories.first().id
                    }
                    val parentCats = filteredCategories.filter { it.parentId == null }.sortedBy { it.name }
                    val groupedCategories = mutableListOf<Category>()
                    parentCats.forEach { p ->
                        groupedCategories.add(p)
                        groupedCategories.addAll(filteredCategories.filter { it.parentId == p.id }.sortedBy { it.name })
                    }
                    val strayCats = filteredCategories.filter { it.parentId != null && parentCats.none { p -> p.id == it.parentId } }
                    groupedCategories.addAll(strayCats)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(groupedCategories) { cat ->
                            val isSel = selectedCategoryId == cat.id
                            val isChild = cat.parentId != null
                            val catColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color.Gray)
                            Card(
                                modifier = Modifier.clickable { selectedCategoryId = cat.id },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) catColor else MaterialTheme.colorScheme.outlineVariant
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) catColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isChild) {
                                        Text("↳ ", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(
                                        imageVector = IconsMap.getIcon(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = if (isSel) catColor else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SettingsTextField(
                    value = recDayOfMonth,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) recDayOfMonth = input
                    },
                    label = "Jatuh Tempo Bulanan (Siklus Tanggal 1 s.d. 31)",
                    placeholder = "misal: 10",
                    isError = isRecDayInvalid,
                    supportingText = {
                        if (isRecDayInvalid) {
                            Text("Masukkan hari rujukan antara tanggal 1 s.d. 31", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("Urutan hari bulanan (misal: 1 untuk awal bulan, 31 untuk akhir bulan).")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "recurring_day_input"
                )
                Spacer(modifier = Modifier.height(8.dp))

                SettingsTextField(
                    value = recNotes,
                    onValueChange = { recNotes = it },
                    label = "Keterangan Tambahan (Opsional)",
                    placeholder = "misal: Tagihan bulanan internet WiFi",
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
                    isError = false,
                    testTag = "recurring_notes_input"
                )
                Spacer(modifier = Modifier.height(14.dp))

                val isFormValid = recName.isNotBlank() && recAmount.isNotBlank() && recDayOfMonth.isNotBlank() &&
                                  !isRecNameInvalid && !isRecAmountInvalid && !isRecDayInvalid &&
                                  selectedAccountId.isNotEmpty() && selectedCategoryId.isNotEmpty()

                Button(
                    onClick = {
                        val amt = recAmount.toLongOrNull() ?: 0L
                        val day = recDayOfMonth.toIntOrNull() ?: 1
                        if (isFormValid) {
                            onAddRecurringTransaction(recName, amt, recType, selectedCategoryId, selectedAccountId, day, recNotes)
                            recName = ""
                            recAmount = ""
                            recDayOfMonth = ""
                            recNotes = ""
                        }
                    },
                    enabled = isFormValid,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_recurring")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Simpan")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Transaksi Rutin", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Group 5: Backup & Restore (CSV)
        item {
            CollapsibleSettingsCard(
                title = "Cadangkan & Pulihkan (CSV)",
                icon = Icons.Default.CloudSync,
                expanded = isBackupRestoreExpanded,
                onExpandedChange = { isBackupRestoreExpanded = it },
                testTag = "settings_col_backup_restore"
            ) {
                Text(
                    text = "Kelola data transaksi Anda secara bebas. Ekspor kapanpun sebagai cadangan, atau impor dari file CSV eksternal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                // PULIHKAN (RESTORE) CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Pulihkan / Impor Data CSV",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kolom wajib: Tanggal, Aset, Kategori, Sub-kategori, Catatan, Jumlah, Tipe. Transaksi akan digabungkan secara otomatis.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    importLauncher.launch("*/*")
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pilih File CSV", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            
                            OutlinedButton(
                                onClick = { showManualPasteRow = !showManualPasteRow },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (showManualPasteRow) Icons.Default.ExpandLess else Icons.Default.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showManualPasteRow) "Sembunyikan" else "Tempel Teks", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        
                        if (showManualPasteRow) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = pastedCsvText,
                                onValueChange = { pastedCsvText = it },
                                label = { Text("Tempel Konten CSV di Sini", style = MaterialTheme.typography.bodySmall) },
                                placeholder = { Text("Tanggal,Aset,Kategori,Sub-kategori,Catatan,Jumlah,Tipe\n04/06/2026 10:00:00,BCA,Makanan,,Nasi rames,15000,Pengeluaran", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)) },
                                modifier = Modifier.fillMaxWidth().height(140.dp).testTag("dialog_manual_csv_input"),
                                textStyle = MaterialTheme.typography.bodySmall,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (pastedCsvText.isNotBlank()) {
                                        loadingMessage = "Memproses pemulihan data..."
                                        onImportCsv(pastedCsvText) { success, msg ->
                                            loadingMessage = null
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            if (success) {
                                                pastedCsvText = ""
                                                showManualPasteRow = false
                                            }
                                        }
                                    }
                                },
                                enabled = pastedCsvText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Selesaikan Impor Manual", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // CADANGKAN (BACKUP) CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Cadangkan / Ekspor Data Keuangan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ekspor seluruh rangkuman transaksi keuangan Anda ke dalam format CSV universal atau format XLSX spreadsheet (Excel).",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = "Pilih Format Ekspor:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = exportFormat == "CSV",
                                onClick = { exportFormat = "CSV" },
                                label = { Text("CSV (.csv)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f)
                            )
                            ElevatedFilterChip(
                                selected = exportFormat == "XLSX",
                                onClick = { exportFormat = "XLSX" },
                                label = { Text("Excel (.xlsx)", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = {
                                loadingMessage = "Memproses pembuatan cadangan data..."
                                onExportCsv { csv ->
                                    if (csv.isNotBlank()) {
                                        val outputText = if (exportFormat == "XLSX") {
                                            convertToXlsxXml(csv)
                                        } else {
                                            csv
                                        }
                                        val displayLabel = if (exportFormat == "XLSX") "Excel (.xlsx)" else "CSV (.csv)"
                                        
                                        // Copy to Clipboard
                                        clipboardManager.setText(AnnotatedString(outputText))
                                        Toast.makeText(context, "Berhasil! Teks $displayLabel disalin ke Clipboard.", Toast.LENGTH_SHORT).show()
                                        
                                        // Trigger Share Sheet
                                        try {
                                            val sendIntent = android.content.Intent().apply {
                                                action = android.content.Intent.ACTION_SEND
                                                putExtra(android.content.Intent.EXTRA_TEXT, outputText)
                                                type = "text/plain"
                                            }
                                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Simpan / Bagikan $displayLabel")
                                            context.startActivity(shareIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal membagikan file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Data transaksi kosong.", Toast.LENGTH_SHORT).show()
                                    }
                                    loadingMessage = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Ekspor ${if (exportFormat == "XLSX") "Excel" else "CSV"}", 
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Group 6: Google Drive Backup
        item {
            var isGdriveExpanded by rememberSaveable { mutableStateOf(false) }
            var showRestoreDialog by remember { mutableStateOf(false) }
            
            CollapsibleSettingsCard(
                title = "Sinkronisasi Google Drive (BETA)",
                icon = Icons.Default.CloudSync,
                expanded = isGdriveExpanded,
                onExpandedChange = { isGdriveExpanded = it },
                testTag = "settings_col_gdrive"
            ) {
                Text(
                    text = "Otomatis upload backup database (JSON Terenkripsi) ke Google Drive agar data Anda aman.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Status Login GDrive", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Button(
                                onClick = { 
                                    if (isGdriveLoggedIn) {
                                        onGdriveLoginChange(false)
                                        onAutoBackupChange(false)
                                    } else {
                                        // TODO: Actual implementation using GoogleSignInClient. For now, mocking flow.
                                        onGdriveLoginChange(true)
                                        Toast.makeText(context, "Google Account Terhubung (Mock)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(if (isGdriveLoggedIn) "Logout" else "Login via Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (isGdriveLoggedIn) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto Upload DB (JSON)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Otomatis upload file DB di background ketika aplikasi dibuka/tutup", style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 14.sp)
                                }
                                androidx.compose.material3.Switch(
                                    checked = isAutoBackupEnabled,
                                    onCheckedChange = { onAutoBackupChange(it) }
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { showRestoreDialog = true }, modifier = Modifier.weight(1f).height(44.dp), shape=RoundedCornerShape(8.dp)) {
                                    Text("Restore dari Drive", fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                                Button(
                                    onClick = { Toast.makeText(context, "Memulai backup DB Json ke Google Drive...", Toast.LENGTH_SHORT).show() }, 
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Backup Sekarang", fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Silakan Login terlebih dahulu agar dapat melakukan backup file DB .json", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            
            if (showRestoreDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showRestoreDialog = false },
                    title = { Text("Konfirmasi Restore dari GDrive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    text = { Text("Pemberitahuan: Apakah Anda yakin ingin memulihkan (restore) data? Tindakan ini akan menimpa database aplikasi saat ini secara permanen dari file backup terakhir (.json) Anda di Google Drive.") },
                    confirmButton = {
                        Button(onClick = { 
                            showRestoreDialog = false; 
                            Toast.makeText(context, "Mulai restore database dari Google Drive...", Toast.LENGTH_LONG).show() 
                        }) {
                            Text("Ya, Restore")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }

    if (loadingMessage != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {}
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = loadingMessage ?: "Memproses...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mohon tunggu beberapa saat",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (editingRecurring != null) {
        EditRecurringDialog(
            recurring = editingRecurring!!,
            accounts = accounts,
            categories = categories,
            onDismiss = { editingRecurring = null },
            onSave = { updated ->
                onUpdateRecurringTransaction(updated)
                editingRecurring = null
            }
        )
    }

    if (editingCategory != null) {
        val catId = editingCategory!!.id
        EditCategoryDialog(
            category = editingCategory!!,
            categories = categories,
            subCategories = subCategories,
            onDismiss = { editingCategory = null },
            onSave = { updated ->
                onEditCategory(updated)
                editingCategory = null
            },
            onAddSubCat = { name -> onAddSubCat(name, catId) },
            onDeleteSubCat = onDeleteSubCat,
            onUpdateSubCat = onUpdateSubCat
        )
    }

    if (editingAccount != null) {
        EditAccountDialog(
            account = editingAccount!!,
            onDismiss = { editingAccount = null },
            onSave = { updated ->
                onEditAccount(updated)
                editingAccount = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditRecurringDialog(
    recurring: RecurringTransaction,
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (RecurringTransaction) -> Unit
) {
    var name by remember { mutableStateOf(recurring.name) }
    var amountText by remember { mutableStateOf(recurring.amount.toString()) }
    var type by remember { mutableStateOf(recurring.type) } // PEMASUKAN, PENGELUARAN
    var dayOfMonthText by remember { mutableStateOf(recurring.dayOfMonth.toString()) }
    var notes by remember { mutableStateOf(recurring.notes) }
    
    var selectedAccountId by remember { mutableStateOf(recurring.accountId) }
    val filteredCats = categories.filter { it.type == type }
    var selectedCategoryId by remember(type) {
        mutableStateOf(if (recurring.type == type) recurring.categoryId else (filteredCats.firstOrNull()?.id ?: ""))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ubah Rencana Transaksi Rutin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pengingat") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_rec_name_field")
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            amountText = input
                        }
                    },
                    label = { Text("Jumlah (Rp)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_rec_amount_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PENGELUARAN", "PEMASUKAN").forEach { t ->
                        val isSel = type == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { type = t }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (t == "PENGELUARAN") "Pengeluaran" else "Pemasukan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dayOfMonthText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            dayOfMonthText = input
                        }
                    },
                    label = { Text("Tanggal Bulanan (1 - 28)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_rec_day_field")
                )

                Text("Pilih Dompet / Akun", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSel = acc.id == selectedAccountId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(acc.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text("Pilih Kategori", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredCats.forEach { cat ->
                        val isSel = cat.id == selectedCategoryId
                        val catColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color.Gray)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) catColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSel) catColor else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = IconsMap.getIcon(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = catColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Keterangan") },
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_rec_notes_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toLongOrNull() ?: recurring.amount
                    val day = dayOfMonthText.toIntOrNull()?.coerceIn(1, 28) ?: recurring.dayOfMonth
                    val updated = recurring.copy(
                        name = name,
                        amount = amt,
                        type = type,
                        dayOfMonth = day,
                        notes = notes,
                        accountId = selectedAccountId,
                        categoryId = selectedCategoryId
                    )
                    onSave(updated)
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ExtendedColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val extendedColors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5",
        "#2196F3", "#00BCD4", "#4CAF50", "#FF9800", "#FF5722",
        "#795548", "#607D8B"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(extendedColors) { colorStr ->
                val color = runCatching { Color(android.graphics.Color.parseColor(colorStr)) }.getOrDefault(Color.Gray)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(colorStr) }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColorHex.uppercase() == colorStr.uppercase()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        var hexInput by remember(selectedColorHex) { mutableStateOf(selectedColorHex) }
        OutlinedTextField(
            value = hexInput,
            onValueChange = { newValue ->
                hexInput = newValue
                if (newValue.matches(Regex("^#[0-9a-fA-F]{6}$"))) {
                    onColorSelected(newValue.uppercase())
                }
            },
            label = { Text("Warna Kustom (HEX)") },
            placeholder = { Text("#FFFFFF") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var balanceText by remember { mutableStateOf(account.balance.toString()) }
    var selectedColorHex by remember { mutableStateOf(account.colorHex) }
    var selectedIconName by remember { mutableStateOf(account.iconName) }
    
    val isNameInvalid = name.trim().isEmpty()
    val isBalanceInvalid = balanceText.toLongOrNull() == null
    
    val walletIcons = listOf(
        Pair("AccountBalanceWallet", "Dompet"),
        Pair("AccountBalance", "Bank"),
        Pair("PhoneAndroid", "E-Wallet"),
        Pair("Paid", "Tunai"),
        Pair("MonetizationOn", "Keuangan"),
        Pair("TrendingUp", "Investasi"),
        Pair("Work", "Kantor"),
        Pair("Home", "Lainnya")
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val scrollState = androidx.compose.foundation.rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Dompet / Akun",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Dompet") },
                    isError = isNameInvalid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_acct_name_field"),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = cleanFinancialInput(it) },
                    label = { Text("Saldo (Rp)") },
                    isError = isBalanceInvalid,
                    singleLine = true,
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("edit_acct_balance_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                // Select Color
                Text(
                    text = "Warna Aksen",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExtendedColorPicker(
                    selectedColorHex = selectedColorHex,
                    onColorSelected = { selectedColorHex = it }
                )

                // Select Icon
                Text(
                    text = "Pilih Ikon Dompet",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    items(walletIcons) { (icName, icDesc) ->
                        val isSelected = selectedIconName == icName
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable { selectedIconName = icName }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = IconsMap.getIcon(icName),
                                    contentDescription = icDesc,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(icDesc, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val bal = balanceText.toLongOrNull() ?: account.balance
                            onSave(account.copy(name = name, balance = bal, colorHex = selectedColorHex, iconName = selectedIconName))
                        },
                        enabled = !isNameInvalid && !isBalanceInvalid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditCategoryDialog(
    category: Category,
    categories: List<Category>,
    subCategories: List<SubCategory>,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
    onAddSubCat: (String) -> Unit = {},
    onDeleteSubCat: (SubCategory) -> Unit = {},
    onUpdateSubCat: (SubCategory) -> Unit = {}
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedColorHex by remember { mutableStateOf(category.colorHex) }
    var selectedIconName by remember { mutableStateOf(category.iconName) }
    var budgetLimitText by remember { mutableStateOf(if (category.budgetLimit > 0) category.budgetLimit.toLong().toString() else "") }
    var catParentId by remember { mutableStateOf(category.parentId) }
    
    val isNameInvalid = name.trim().isEmpty()
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val scrollState = androidx.compose.foundation.rememberScrollState()
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kategori") },
                    isError = isNameInvalid,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_cat_name_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (category.type == "PENGELUARAN") {
                    OutlinedTextField(
                        value = budgetLimitText,
                        onValueChange = { budgetLimitText = cleanFinancialInput(it) },
                        label = { Text("Batas Anggaran (Opsional)") },
                        visualTransformation = IndonesianCurrencyVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(text = "Sub-Kategori Dari (Opsional)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                val availableParents = categories.filter { it.type == category.type && it.parentId == null && it.id != category.id }
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ElevatedFilterChip(
                            selected = catParentId == null,
                            onClick = { catParentId = null },
                            label = { Text("Tidak Ada") }
                        )
                    }
                    items(availableParents) { parent ->
                        ElevatedFilterChip(
                            selected = catParentId == parent.id,
                            onClick = { catParentId = parent.id },
                            label = { Text(parent.name) }
                        )
                    }
                }

                // Select Color
                Text(
                    text = "Warna Aksen",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExtendedColorPicker(
                    selectedColorHex = selectedColorHex,
                    onColorSelected = { selectedColorHex = it }
                )

                // Select Icon
                Text(
                    text = "Pilih Ikon Kategori",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(IconsMap.availableIcons) { icOpt ->
                        val isSelected = selectedIconName == icOpt.name
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable { selectedIconName = icOpt.name }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = IconsMap.getIcon(icOpt.name),
                                    contentDescription = icOpt.description,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(icOpt.description, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }

                // Sub Categories
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sub Kategori",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val mySubCats = subCategories.filter { it.categoryId == category.id }.sortedBy { it.orderIndex }
                var newSubCatName by remember { mutableStateOf("") }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newSubCatName,
                        onValueChange = { newSubCatName = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nama sub kategori baru") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSubCatName.isNotBlank()) {
                                onAddSubCat(newSubCatName)
                                newSubCatName = ""
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                if (mySubCats.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        mySubCats.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                var isEditing by remember(sub.id) { mutableStateOf(false) }
                                var editName by remember(sub.id) { mutableStateOf(sub.name) }
                                
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    IconButton(onClick = { 
                                        if (editName.isNotBlank() && editName != sub.name) {
                                            onUpdateSubCat(sub.copy(name = editName))
                                        }
                                        isEditing = false 
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Text(sub.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Row {
                                        IconButton(onClick = { isEditing = true }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = { onDeleteSubCat(sub) }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val limit = budgetLimitText.toLongOrNull() ?: 0L
                            onSave(category.copy(name = name, colorHex = selectedColorHex, iconName = selectedIconName, budgetLimit = limit, parentId = catParentId))
                        },
                        enabled = !isNameInvalid,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    categories: List<Category>,
    subCategories: List<SubCategory>,
    tags: List<com.example.data.model.Tag>,
    recurringTransactions: List<RecurringTransaction>,
    allTransactions: List<Transaction>,
    txToEdit: Transaction? = null,
    getTagsForTx: suspend (Int) -> List<String> = { emptyList() },
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String?, String, String?, Long, String, Long, List<String>) -> Unit,
    onAddCategoryDirectly: (Category) -> Unit,
    onAddSubCatDirectly: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf(txToEdit?.type ?: "PENGELUARAN") } // PEMASUKAN, PENGELUARAN, TRANSFER
    var amountText by remember { mutableStateOf(txToEdit?.amount?.toLong()?.toString() ?: "") }
    var selectedDate by remember { mutableStateOf(txToEdit?.date ?: System.currentTimeMillis()) }
    
    val initialNotes = txToEdit?.notes ?: ""
    val (initialSubCat, rawNotes) = if (initialNotes.startsWith("[")) {
        val endIdx = initialNotes.indexOf("]")
        if (endIdx != -1) {
            Pair(initialNotes.substring(1, endIdx), initialNotes.substring(endIdx + 1).trim())
        } else {
            Pair("", initialNotes)
        }
    } else {
        Pair("", initialNotes)
    }
    
    var notesText by remember { mutableStateOf(if (rawNotes.startsWith("Biaya Admin:")) "" else rawNotes) }
    var subCatText by remember { mutableStateOf(initialSubCat) }

    val calForOverlap = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val selectedDayOfMonth = calForOverlap.get(Calendar.DAY_OF_MONTH)

    var selectedAccountId by remember { mutableStateOf(txToEdit?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var selectedDestAccountId by remember { mutableStateOf(txToEdit?.destAccountId ?: accounts.firstOrNull { it.id != selectedAccountId }?.id ?: accounts.getOrNull(1)?.id ?: "") }

    // Categorized list options
    val filteredCategories = categories.filter { it.type == selectedType }
    
    var selectedCategoryId by remember(selectedType) {
        mutableStateOf(
            if (txToEdit != null && txToEdit.type == selectedType) {
                txToEdit.categoryId
            } else {
                filteredCategories.firstOrNull()?.id ?: ""
            }
        )
    }

    var selectedSubCategoryId by remember(selectedCategoryId, txToEdit) {
        mutableStateOf(
            if (txToEdit != null && txToEdit.categoryId == selectedCategoryId) {
                txToEdit.subCategoryId ?: ""
            } else {
                "" // Default to "None"
            }
        )
    }

    // Dynamic core color theme depending on transaction type
    val themeAccentColor = when (selectedType) {
        "PEMASUKAN" -> Color(0xFF2E7D32) // Deep Emerald Green
        "PENGELUARAN" -> Color(0xFFC62828) // Deep Crimson Red
        else -> Color(0xFF1565C0) // Royal Sapphire Blue
    }

    val selectedAccount = accounts.find { it.id == selectedAccountId }

    var hasTransferFee by remember { mutableStateOf(false) }
    var transferFeeText by remember { mutableStateOf("") }
    var selectedTagIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(txToEdit) {
        if (txToEdit != null) {
            val txTags = getTagsForTx(txToEdit.id)
            selectedTagIds = txTags.toSet()
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedType != "TRANSFER") {
            hasTransferFee = false
            transferFeeText = ""
        }
    }

    val transferFee = if (selectedType == "TRANSFER" && hasTransferFee) (transferFeeText.toLongOrNull() ?: 0L) else 0L
    val totalRequiredAmount = (amountText.toLongOrNull() ?: 0L) + transferFee
    
    val txToEditRevertedBalance = if (txToEdit != null && selectedAccount?.id == txToEdit.accountId) {
        when (txToEdit.type) {
            "PENGELUARAN" -> txToEdit.amount
            "TRANSFER" -> txToEdit.amount
            "PEMASUKAN" -> -txToEdit.amount
            else -> 0L
        }
    } else 0L

    val isAmountOverLimit = (selectedType == "PENGELUARAN" || selectedType == "TRANSFER") &&
            totalRequiredAmount > ((selectedAccount?.balance ?: 0L) + txToEditRevertedBalance)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("add_transaction_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, themeAccentColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Dialog Block
                Text(
                    text = "Tambah Catatan Keuangan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Capsule Sliding Switch for Type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf(
                        "PENGELUARAN" to "📤 Pengeluaran",
                        "PEMASUKAN" to "📥 Pemasukan",
                        "TRANSFER" to "🔄 Transfer"
                    )
                    types.forEach { (typeVal, label) ->
                        val isSel = selectedType == typeVal
                        val activeColor = when (typeVal) {
                            "PEMASUKAN" -> Color(0xFF2E7D32)
                            "PENGELUARAN" -> Color(0xFFC62828)
                            else -> Color(0xFF1565C0)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) activeColor else Color.Transparent)
                                .clickable { selectedType = typeVal }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Container
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nominal Input Card (Glow Custom Layout)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) {
                                    themeAccentColor.copy(alpha = 0.15f)
                                } else {
                                    themeAccentColor.copy(alpha = 0.06f)
                                }
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, themeAccentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = when (selectedType) {
                                            "PEMASUKAN" -> Icons.Default.ArrowDownward
                                            "PENGELUARAN" -> Icons.Default.ArrowUpward
                                            else -> Icons.Default.CompareArrows
                                        },
                                        contentDescription = "Tipe",
                                        tint = themeAccentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Jumlah Transaksi (Rupiah)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = themeAccentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                // Large Input Field with native currency look
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Rp",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = themeAccentColor.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    OutlinedTextField(
                                        value = amountText,
                                        onValueChange = { amountText = cleanFinancialInput(it) },
                                        placeholder = { Text("0", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = themeAccentColor.copy(alpha = 0.39f)) },
                                        visualTransformation = IndonesianCurrencyVisualTransformation(),
                                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = themeAccentColor,
                                            fontSize = 28.sp
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("transaction_amount_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            disabledBorderColor = Color.Transparent,
                                            errorBorderColor = Color.Transparent
                                        )
                                    )
                                }

                                if (isAmountOverLimit) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Saldo dompet tidak mencukupi! Tersedia: ${FormatUtils.formatRupiah((selectedAccount?.balance ?: 0L) + txToEditRevertedBalance)}",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Elegant Date Picker Bar
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Tanggal",
                                        tint = themeAccentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tanggal Catatan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        cal.timeInMillis = selectedDate
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val c = Calendar.getInstance()
                                                c.set(y, m, d)
                                                selectedDate = c.timeInMillis
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.testTag("date_picker_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeAccentColor.copy(alpha = 0.12f),
                                        contentColor = themeAccentColor
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = FormatUtils.formatDate(selectedDate),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Account Source Selector (Dropdown)
                    item {
                        var accountExpanded by remember { mutableStateOf(false) }
                        var accountTriggerWidth by remember { mutableStateOf(0) }
                        val selectedAccountObj = accounts.find { it.id == selectedAccountId }
                        val acctColor = selectedAccountObj?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrDefault(Color.Gray) } ?: Color.Gray

                        Column {
                            Text(
                                text = if (selectedType == "TRANSFER") "Sumber Rekening / Dompet" else "Simpan ke Dompet",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        accountTriggerWidth = coordinates.size.width
                                    }
                            ) {
                                OutlinedCard(
                                    onClick = { accountExpanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("transaction_account_dropdown_trigger"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (selectedAccountObj != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(acctColor.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = IconsMap.getIcon(selectedAccountObj.iconName),
                                                        contentDescription = selectedAccountObj.name,
                                                        tint = acctColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = selectedAccountObj.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = FormatUtils.formatRupiah(selectedAccountObj.balance),
                                                        fontSize = 11.sp,
                                                        color = acctColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Pilih Rekening...",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (accountExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                androidx.compose.material3.DropdownMenu(
                                    expanded = accountExpanded,
                                    onDismissRequest = { accountExpanded = false },
                                    modifier = Modifier
                                        .width(with(LocalDensity.current) { accountTriggerWidth.toDp() })
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    accounts.forEach { acct ->
                                        val acColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(acColor.copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = IconsMap.getIcon(acct.iconName),
                                                            contentDescription = acct.name,
                                                            tint = acColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(
                                                            text = acct.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = FormatUtils.formatRupiah(acct.balance),
                                                            fontSize = 11.sp,
                                                            color = acColor
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedAccountId = acct.id
                                                accountExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Destination Account (for TRANSFER only)
                    if (selectedType == "TRANSFER") {
                        item {
                            var destAccountExpanded by remember { mutableStateOf(false) }
                            var destTriggerWidth by remember { mutableStateOf(0) }
                            val destAccounts = accounts.filter { it.id != selectedAccountId }
                            val selectedDestAccountObj = destAccounts.find { it.id == selectedDestAccountId } ?: destAccounts.firstOrNull()
                            
                            // Keep selectedDestAccountId in sync
                            if (selectedDestAccountObj != null && selectedDestAccountId != selectedDestAccountObj.id) {
                                selectedDestAccountId = selectedDestAccountObj.id
                            }
                            
                            val destAcctColor = selectedDestAccountObj?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrDefault(Color.Gray) } ?: Color.Gray

                            Column {
                                Text(
                                    text = "Tujuan Rekening / Dompet Penerima",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = themeAccentColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (destAccounts.isEmpty()) {
                                    Text("Tidak ada akun lain.", color = Color.Gray, fontSize = 11.sp)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                destTriggerWidth = coordinates.size.width
                                            }
                                    ) {
                                        OutlinedCard(
                                            onClick = { destAccountExpanded = true },
                                            modifier = Modifier.fillMaxWidth().testTag("transaction_dest_account_dropdown_trigger"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (selectedDestAccountObj != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(CircleShape)
                                                                .background(destAcctColor.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = IconsMap.getIcon(selectedDestAccountObj.iconName),
                                                                contentDescription = selectedDestAccountObj.name,
                                                                tint = destAcctColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = selectedDestAccountObj.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = FormatUtils.formatRupiah(selectedDestAccountObj.balance),
                                                                fontSize = 11.sp,
                                                                color = destAcctColor,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "Pilih Tujuan Rekening...",
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = if (destAccountExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                                    contentDescription = "Dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        androidx.compose.material3.DropdownMenu(
                                            expanded = destAccountExpanded,
                                            onDismissRequest = { destAccountExpanded = false },
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { destTriggerWidth.toDp() })
                                                .background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            destAccounts.forEach { acct ->
                                                val acColor = runCatching { Color(android.graphics.Color.parseColor(acct.colorHex)) }.getOrDefault(Color.Gray)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(acColor.copy(alpha = 0.15f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = IconsMap.getIcon(acct.iconName),
                                                                    contentDescription = acct.name,
                                                                    tint = acColor,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Column {
                                                                Text(
                                                                    text = acct.name,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Text(
                                                                    text = FormatUtils.formatRupiah(acct.balance),
                                                                    fontSize = 11.sp,
                                                                    color = acColor
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedDestAccountId = acct.id
                                                        destAccountExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Transfer Fee Checkbox (for TRANSFER only)
                    if (selectedType == "TRANSFER") {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = hasTransferFee,
                                            onCheckedChange = { hasTransferFee = it },
                                            colors = CheckboxDefaults.colors(checkedColor = themeAccentColor),
                                            modifier = Modifier.testTag("transfer_fee_checkbox")
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { hasTransferFee = !hasTransferFee }
                                        ) {
                                            Text(
                                                text = "Tambah Biaya Transfer (Admin Fee)",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Centang jika ada biaya admin tambahan",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (hasTransferFee) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedTextField(
                                            value = transferFeeText,
                                            onValueChange = { transferFeeText = cleanFinancialInput(it) },
                                            label = { Text("Biaya Admin / Transfer (Rupiah)") },
                                            placeholder = { Text("6500") },
                                            prefix = { Text("Rp ") },
                                            singleLine = true,
                                            visualTransformation = IndonesianCurrencyVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = themeAccentColor,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                focusedLabelColor = themeAccentColor
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("transfer_fee_amount_field")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Category Selection (Dropdown)
                    if (selectedType != "TRANSFER") {
                        item {
                            var categoryExpanded by remember { mutableStateOf(false) }
                            var categoryTriggerWidth by remember { mutableStateOf(0) }
                            val selectedCategoryObj = filteredCategories.find { it.id == selectedCategoryId } ?: filteredCategories.firstOrNull()
                            
                            // Keep selectedCategoryId in sync
                            if (selectedCategoryObj != null && selectedCategoryId != selectedCategoryObj.id) {
                                selectedCategoryId = selectedCategoryObj.id
                            }
                            
                            val catColor = selectedCategoryObj?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrDefault(Color.Gray) } ?: Color.Gray

                            Column {
                                Text(
                                    text = "Pilih Kategori",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (filteredCategories.isEmpty()) {
                                    Text(
                                        text = "Belum ada kategori. Buat di Pengaturan.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                categoryTriggerWidth = coordinates.size.width
                                            }
                                    ) {
                                        OutlinedCard(
                                            onClick = { categoryExpanded = true },
                                            modifier = Modifier.fillMaxWidth().testTag("transaction_category_dropdown_trigger"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (selectedCategoryObj != null) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .clip(CircleShape)
                                                                .background(catColor.copy(alpha = 0.15f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = IconsMap.getIcon(selectedCategoryObj.iconName),
                                                                contentDescription = selectedCategoryObj.name,
                                                                tint = catColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = selectedCategoryObj.name,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Pilih Kategori...",
                                                            fontSize = 13.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = if (categoryExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                                    contentDescription = "Dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        androidx.compose.material3.DropdownMenu(
                                            expanded = categoryExpanded,
                                            onDismissRequest = { categoryExpanded = false },
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { categoryTriggerWidth.toDp() })
                                                .background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            filteredCategories.forEach { cat ->
                                                val cColor = runCatching { Color(android.graphics.Color.parseColor(cat.colorHex)) }.getOrDefault(Color.Gray)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(cColor.copy(alpha = 0.15f)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = IconsMap.getIcon(cat.iconName),
                                                                    contentDescription = cat.name,
                                                                    tint = cColor,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = cat.name,
                                                                fontSize = 13.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedCategoryId = cat.id
                                                        selectedSubCategoryId = "" // Reset subcategory when category changes
                                                        categoryExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // SubCategory Selection
                                val availableSubCategories = subCategories.filter { !it.isArchived && it.categoryId == selectedCategoryId }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                    var subCategoryExpanded by remember { mutableStateOf(false) }
                                    var subCategoryTriggerWidth by remember { mutableStateOf(0) }
                                    var newSubCatName by remember { mutableStateOf("") }
                                    val selectedSubCategoryObj = availableSubCategories.find { it.id == selectedSubCategoryId }

                                    Text(
                                        text = "Sub Kategori (Opsional)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                subCategoryTriggerWidth = coordinates.size.width
                                            }
                                    ) {
                                        OutlinedCard(
                                            onClick = { subCategoryExpanded = true },
                                            modifier = Modifier.fillMaxWidth().testTag("transaction_subcategory_dropdown_trigger"),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = selectedSubCategoryObj?.name ?: "Pilih Sub Kategori...",
                                                    fontSize = 13.sp,
                                                    color = if (selectedSubCategoryObj != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Icon(
                                                    imageVector = if (subCategoryExpanded) androidx.compose.material.icons.Icons.Default.ArrowDropUp else androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                                                    contentDescription = "Dropdown",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        androidx.compose.material3.DropdownMenu(
                                            expanded = subCategoryExpanded,
                                            onDismissRequest = { subCategoryExpanded = false },
                                            modifier = Modifier
                                                .width(with(LocalDensity.current) { subCategoryTriggerWidth.toDp() })
                                                .background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Tidak ada (Pilih jika tidak perlu)") },
                                                onClick = {
                                                    selectedSubCategoryId = ""
                                                    subCategoryExpanded = false
                                                }
                                            )
                                            availableSubCategories.forEach { subCat ->
                                                DropdownMenuItem(
                                                    text = { Text(subCat.name) },
                                                    onClick = {
                                                        selectedSubCategoryId = subCat.id
                                                        subCategoryExpanded = false
                                                    }
                                                )
                                            }
                                            DropdownMenuItem(
                                                text = { 
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                                        OutlinedTextField(
                                                            value = newSubCatName,
                                                            onValueChange = { newSubCatName = it },
                                                            modifier = Modifier.weight(1f).height(48.dp),
                                                            placeholder = { Text("Tambah Baru", fontSize = 12.sp) },
                                                            singleLine = true,
                                                            textStyle = MaterialTheme.typography.bodySmall
                                                        )
                                                        IconButton(onClick = { 
                                                            if (newSubCatName.isNotBlank()) {
                                                                onAddSubCatDirectly(newSubCatName, selectedCategoryId)
                                                                newSubCatName = ""
                                                            }
                                                        }, modifier = Modifier.size(32.dp)) {
                                                            Icon(Icons.Default.Add, contentDescription = "Tambah", tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                },
                                                onClick = {}
                                            )
                                        }
                                    }
                            }
                        }
                    }

                    if (tags.isNotEmpty()) {
                        item {
                            Text("Label Transaksi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                            androidx.compose.foundation.layout.FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    val isSelected = selectedTagIds.contains(tag.id)
                                    val tagColor = runCatching { Color(android.graphics.Color.parseColor(tag.colorHex)) }.getOrDefault(Color.Gray)
                                    androidx.compose.material3.FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedTagIds = if (isSelected) selectedTagIds - tag.id else selectedTagIds + tag.id
                                        },
                                        label = { Text(tag.name) },
                                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = tagColor.copy(alpha = 0.2f),
                                            selectedLabelColor = tagColor,
                                            iconColor = tagColor
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Description Note Input text block
                    item {
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Keterangan (Opsional)") },
                            placeholder = { Text("Ketik keterangan jika ada") },
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transaction_notes_field"),
                            maxLines = 2,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = themeAccentColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = themeAccentColor,
                                focusedLabelColor = themeAccentColor
                            )
                        )
                    }

                    // Overlap warning detection item
                    item {
                        val resolvedCategoryIdForOverlap = if (selectedType == "TRANSFER") "transfer" else selectedCategoryId
                        val overlapTx = recurringTransactions.find { rec ->
                            !rec.isPaused && rec.dayOfMonth == selectedDayOfMonth && rec.categoryId == resolvedCategoryIdForOverlap && rec.type == selectedType
                        }
                        if (overlapTx != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("overlap_warning_box"),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Overlap Warning Icon",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "⚠️ PERINGATAN: Ada transaksi rutin '${overlapTx.name}' terjadwal di tanggal $selectedDayOfMonth untuk kategori yang sama. Catatan baru ini berpotensi duplikat.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
 
                Spacer(modifier = Modifier.height(16.dp))
 
                // Action Controls Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("Batal")
                    }
 
                    Button(
                        onClick = {
                            val amount = amountText.toLongOrNull() ?: 0L
                            if (amount > 0 && selectedAccountId.isNotEmpty()) {
                                val transferFeeVal = if (selectedType == "TRANSFER" && hasTransferFee) (transferFeeText.toLongOrNull() ?: 0L) else 0L
                                val finalNotes = notesText
                                
                                val resolvedCategoryId = if (selectedType == "TRANSFER") {
                                    "transfer"
                                } else {
                                    selectedCategoryId
                                }

                                onSave(
                                    amount,
                                    selectedType,
                                    resolvedCategoryId,
                                    if (selectedType == "TRANSFER") null else selectedSubCategoryId.ifEmpty { null },
                                    selectedAccountId,
                                    if (selectedType == "TRANSFER") selectedDestAccountId else null,
                                    selectedDate,
                                    finalNotes,
                                    transferFeeVal,
                                    selectedTagIds.toList()
                                )
                            }
                        },
                        enabled = (amountText.toLongOrNull() ?: 0L) > 0L && 
                                  selectedAccountId.isNotEmpty() && 
                                  (selectedType != "TRANSFER" || selectedDestAccountId.isNotEmpty()) && 
                                  !isAmountOverLimit && 
                                  (selectedType == "TRANSFER" || selectedCategoryId.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeAccentColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_transaction_button")
                    ) {
                        Text(
                            text = "Simpan Catatan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Seeds the DB with realistic custom monthly transaction data to live demonstration
fun seedMockTransactions(viewModel: TransactionViewModel) {
    viewModel.restoreDefaultAccounts()
    val calendar = Calendar.getInstance()
    val todayMs = calendar.timeInMillis

    // Pemasukan
    viewModel.addTransaction(
        amount = 6500000L,
        type = "PEMASUKAN",
        categoryId = "gaji",
        accountId = "bca",
        notes = "Gaji Bulanan Pekerjaan Tetap"
    )

    viewModel.addTransaction(
        amount = 500000L,
        type = "PEMASUKAN",
        categoryId = "bonus",
        accountId = "ovo",
        notes = "Hadiah Cashback Reward"
    )

    // Pengeluaran
    // Makanan
    viewModel.addTransaction(
        amount = 120000L,
        type = "PENGELUARAN",
        categoryId = "makanan",
        accountId = "tunai",
        date = todayMs - 24 * 3600 * 1000,
        notes = "Makan Siang Sate Ayam"
    )
    viewModel.addTransaction(
        amount = 85000L,
        type = "PENGELUARAN",
        categoryId = "makanan",
        accountId = "ovo",
        date = todayMs,
        notes = "Kopi & Roti Bakar Sore"
    )

    // Transportasi
    viewModel.addTransaction(
        amount = 150000L,
        type = "PENGELUARAN",
        categoryId = "transportasi",
        accountId = "bca",
        date = todayMs - 2 * 24 * 3600 * 1000,
        notes = "Isi bensin Pertamax Mobil"
    )

    // Belanja
    viewModel.addTransaction(
        amount = 450000L,
        type = "PENGELUARAN",
        categoryId = "belanja",
        accountId = "bca",
        date = todayMs - 3 * 24 * 3600 * 1000,
        notes = "Belanja Bulanan Supermarket"
    )

    // Hiburan
    viewModel.addTransaction(
        amount = 180000L,
        type = "PENGELUARAN",
        categoryId = "hiburan",
        accountId = "dana",
        date = todayMs - 12 * 3600 * 1000,
        notes = "Tiket Bioskop Cinema XXI"
    )

    // Tagihan
    viewModel.addTransaction(
        amount = 350000L,
        type = "PENGELUARAN",
        categoryId = "tagihan",
        accountId = "bca",
        date = todayMs - 5 * 24 * 3600 * 1000,
        notes = "Tagihan Listrik PLN & Wifi"
    )

    // Optional Transfer
    viewModel.addTransaction(
        amount = 200000L,
        type = "TRANSFER",
        categoryId = "transfer",
        accountId = "bca",
        destAccountId = "ovo",
        date = todayMs - 3600 * 1000,
        notes = "Topup saldo OVO via BCA Mobile"
    )
}

// Clear all databases transactions to fresh default state
fun clearAllData(viewModel: TransactionViewModel) {
    viewModel.clearAllData()
}

@Composable
fun QuickAddTransactionDialog(
    accounts: List<Account>,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PENGELUARAN") } // "PEMASUKAN", "PENGELUARAN"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Instant Cepat", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    
                                .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type Selector Tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("PENGELUARAN" to "Pengeluaran", "PEMASUKAN" to "Pemasukan").forEach { (typeVal, label) ->
                        val isSelected = type == typeVal
                        val containerColor = if (isSelected) {
                            if (typeVal == "PEMASUKAN") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        } else {
                            Color.Transparent
                        }
                        val contentColor = if (isSelected) {
                            if (typeVal == "PEMASUKAN") Color(0xFF2E7D32) else Color(0xFFC62828)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(containerColor)
                                .clickable { type = typeVal }
                                .padding(vertical = 8.dp)
                                .testTag("quick_add_type_$typeVal"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            )
                        }
                    }
                }
                
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanFinancialInput(it) },
                    label = { Text("Jumlah (Rupiah)", style = MaterialTheme.typography.bodySmall) },
                    placeholder = { Text("0", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    visualTransformation = IndonesianCurrencyVisualTransformation(),
                    prefix = { Text("Rp ", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                
                Text(
                    text = "*Transaksi ini akan otomatis menggunakan akun pertama dan kategori default hari ini.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        onSave(amount, type)
                    }
                },
                enabled = amountText.isNotEmpty() && (amountText.toLongOrNull() ?: 0L) > 0L,
                modifier = Modifier.testTag("quick_add_submit_button")
            ) {
                Text("Simpan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("quick_add_cancel_button")
            ) {
                Text("Batal", style = MaterialTheme.typography.labelSmall)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

fun cleanFinancialInput(input: String): String {
    var cleaned = input.replace(',', '.')
    val parts = cleaned.split('.')
    if (parts.size > 2) {
        cleaned = parts[0] + "." + parts[1]
    }
    val p = cleaned.split('.')
    if (p.size == 2 && p[1].length > 2) {
        cleaned = p[0] + "." + p[1].substring(0, 2)
    }
    if (p[0].length > 12) {
        cleaned = p[0].substring(0, 12) + (if (p.size > 1) "." + p[1] else "")
    }
    return cleaned.filterIndexed { index, c -> c.isDigit() || (c == '.' && index == cleaned.indexOf('.')) }
}

class IndonesianCurrencyVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)

        val parts = originalText.split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) "," + parts[1] else "" // Use comma for decimal separator visually

        val formattedIntPart = if (intPart.isNotEmpty()) {
             intPart.reversed().chunked(3).joinToString(".").reversed()
        } else ""
        
        val formattedText = formattedIntPart + decPart

        val mapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val intLen = intPart.length
                if (offset <= intLen) {
                    val dotsToRight = Math.max(0, (intLen - offset - 1) / 3)
                    val totalDots = Math.max(0, (intLen - 1) / 3)
                    return offset + (totalDots - dotsToRight)
                }
                val totalDots = Math.max(0, (intLen - 1) / 3)
                return offset + totalDots
            }

            override fun transformedToOriginal(offset: Int): Int {
                val intLen = intPart.length
                val totalDots = Math.max(0, (intLen - 1) / 3)
                val formattedIntLen = intLen + totalDots
                if (offset <= formattedIntLen) {
                    var dots = 0
                    for (i in 0 until offset) {
                        if (formattedIntPart.length > i && formattedIntPart[i] == '.') dots++
                    }
                    return offset - dots
                }
                return offset - totalDots
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formattedText), mapping)
    }
}
