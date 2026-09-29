package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.CurrencySelectorDialog
import com.example.ui.dialogs.LinkBankDialog
import com.example.ui.dialogs.QuickTransferDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.SyncSettingsScreen
import com.example.ui.screens.TradingViewAnalysisScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.VaultSyncTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaultSyncTheme {
                VaultSyncApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultSyncApp(viewModel: FinanceViewModel) {
    // Security & App Lock State
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isPinEnabled.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isPrivacyMode by viewModel.isPrivacyMode.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val netWorthSummary by viewModel.netWorthSummary.collectAsStateWithLifecycle()
    val categorySpendings by viewModel.categorySpendings.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val syncLogs by viewModel.syncLogs.collectAsStateWithLifecycle()
    val stockIndices by viewModel.stockIndices.collectAsStateWithLifecycle()
    val cryptoAssets by viewModel.cryptoAssets.collectAsStateWithLifecycle()
    val marketOverview by viewModel.marketOverview.collectAsStateWithLifecycle()

    val isSyncing by viewModel.isGlobalSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val isAutoSyncServiceActive by viewModel.isAutoSyncServiceActive.collectAsStateWithLifecycle()
    val syncIntervalMinutes by viewModel.autoSyncIntervalMinutes.collectAsStateWithLifecycle()

    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val selectedTypeFilter by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()

    val snackBarMessage by viewModel.snackBarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLinkBankDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showQuickTransferDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    // Active TradingView analysis symbol if open
    var tradingViewSymbol by remember { mutableStateOf<String?>(null) }

    // App Lock Gate: If locked, show security screen
    if (isAppLocked) {
        AppLockScreen(
            onUnlockWithPin = { viewModel.unlockAppWithPin(it) },
            onUnlockWithBiometrics = { viewModel.unlockAppWithBiometrics() }
        )
        return
    }

    // Handle back button for secondary tabs or TradingView
    if (tradingViewSymbol != null) {
        BackHandler {
            tradingViewSymbol = null
        }
    } else if (selectedTab != MainTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(MainTab.DASHBOARD)
        }
    }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackBar()
        }
    }

    if (tradingViewSymbol != null) {
        TradingViewAnalysisScreen(
            stockIndices = stockIndices,
            cryptoAssets = cryptoAssets,
            currencySymbol = currencySymbol,
            initialSymbol = tradingViewSymbol,
            currentModel = selectedModel,
            onSelectModel = { viewModel.selectModel(it) },
            onBack = { tradingViewSymbol = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                MainTab.DASHBOARD -> "OmniTrade"
                                MainTab.ACCOUNTS -> "Bank Accounts"
                                MainTab.TRANSACTIONS -> "Activity Feed"
                                MainTab.MARKETS -> "Global Markets"
                                MainTab.BUDGETS -> "Budgets & Limits"
                                MainTab.SETTINGS -> "Sync & Settings"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        // Quick Privacy Shield Toggle (Mask sensitive numbers on screen)
                        IconButton(
                            onClick = { viewModel.togglePrivacyMode() },
                            modifier = Modifier.testTag("btn_toggle_privacy_mode")
                        ) {
                            Icon(
                                imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPrivacyMode) "Show Balances" else "Hide Balances",
                                tint = if (isPrivacyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Global Worldwide Currency Pill Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showCurrencyDialog = true }
                                .testTag("btn_top_bar_currency_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = selectedCurrency.flag, fontSize = 16.sp)
                                Text(
                                    text = "${selectedCurrency.code} (${selectedCurrency.symbol})",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change Currency",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val navItems = listOf(
                        Triple(MainTab.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                        Triple(MainTab.ACCOUNTS, "Accounts", Icons.Default.AccountBalance),
                        Triple(MainTab.TRANSACTIONS, "Activity", Icons.Default.ReceiptLong),
                        Triple(MainTab.MARKETS, "Markets", Icons.Default.Public),
                        Triple(MainTab.BUDGETS, "Budgets", Icons.Default.PieChart),
                        Triple(MainTab.SETTINGS, "Sync", Icons.Default.Settings)
                    )

                    navItems.forEach { (tab, label, icon) ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> {
                        DashboardScreen(
                            netWorthSummary = netWorthSummary,
                            accounts = accounts,
                            recentTransactions = transactions,
                            categorySpendings = categorySpendings,
                            stockIndices = stockIndices,
                            cryptoAssets = cryptoAssets,
                            currencySymbol = currencySymbol,
                            isSyncing = isSyncing,
                            lastSyncTimestamp = lastSyncTime,
                            isPrivacyMode = isPrivacyMode,
                            onSyncAll = { viewModel.syncAllAccounts() },
                            onSyncAccount = { viewModel.syncSingleAccount(it) },
                            onToggleAccountAutoSync = { id, enabled -> viewModel.toggleAccountAutoSync(id, enabled) },
                            onLinkBankClick = { showLinkBankDialog = true },
                            onAddTransactionClick = { showAddTransactionDialog = true },
                            onQuickTransferClick = { showQuickTransferDialog = true },
                            onTradingAnalysisClick = { symbol -> tradingViewSymbol = symbol },
                            onLockAppClick = { viewModel.lockApp() },
                            onNavigateToMarkets = { viewModel.selectTab(MainTab.MARKETS) },
                            onNavigateToAccounts = { viewModel.selectTab(MainTab.ACCOUNTS) },
                            onNavigateToTransactions = { viewModel.selectTab(MainTab.TRANSACTIONS) },
                            onSimulateWebhook = { viewModel.simulateInstantBankPush() }
                        )
                    }

                    MainTab.ACCOUNTS -> {
                        AccountsScreen(
                            accounts = accounts,
                            currencySymbol = currencySymbol,
                            onSyncAccount = { viewModel.syncSingleAccount(it) },
                            onToggleAutoSync = { id, enabled -> viewModel.toggleAccountAutoSync(id, enabled) },
                            onDeleteAccount = { viewModel.deleteAccount(it) },
                            onLinkBankClick = { showLinkBankDialog = true }
                        )
                    }

                    MainTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            transactions = transactions,
                            accounts = accounts,
                            currencySymbol = currencySymbol,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            selectedCategoryFilter = selectedCategoryFilter,
                            onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                            selectedTypeFilter = selectedTypeFilter,
                            onTypeFilterChange = { viewModel.setTypeFilter(it) },
                            onAddTransactionClick = { showAddTransactionDialog = true },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )
                    }

                    MainTab.MARKETS -> {
                        MarketsScreen(
                            stockIndices = stockIndices,
                            cryptoAssets = cryptoAssets,
                            overview = marketOverview,
                            onRefresh = { viewModel.refreshMarkets() },
                            onOpenTradingView = { symbol -> tradingViewSymbol = symbol }
                        )
                    }

                    MainTab.BUDGETS -> {
                        BudgetsScreen(
                            budgets = budgets,
                            categorySpendings = categorySpendings,
                            currencySymbol = currencySymbol,
                            onSaveBudget = { cat, limit -> viewModel.saveBudget(cat, limit) },
                            onDeleteBudget = { viewModel.deleteBudget(it) }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SyncSettingsScreen(
                            accounts = accounts,
                            syncLogs = syncLogs,
                            isAutoSyncActive = isAutoSyncServiceActive,
                            onToggleAutoSyncService = { viewModel.toggleAutoSyncService(it) },
                            syncIntervalMinutes = syncIntervalMinutes,
                            onSetSyncInterval = { viewModel.setAutoSyncInterval(it) },
                            selectedCurrency = selectedCurrency,
                            onOpenCurrencySelector = { showCurrencyDialog = true },
                            onSyncAllNow = { viewModel.syncAllAccounts() },
                            onSimulateWebhook = { viewModel.simulateInstantBankPush() },
                            isPinEnabled = isPinEnabled,
                            onTogglePinSecurity = { viewModel.togglePinSecurity(it) },
                            isBiometricEnabled = isBiometricEnabled,
                            onToggleBiometrics = { viewModel.toggleBiometrics(it) },
                            isPrivacyMode = isPrivacyMode,
                            onTogglePrivacyMode = { viewModel.togglePrivacyMode() },
                            onLockAppNow = { viewModel.lockApp() }
                        )
                    }
                }
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectorDialog(
            selectedCurrency = selectedCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelectCurrency = { newCurr ->
                viewModel.selectCurrency(newCurr)
                showCurrencyDialog = false
            }
        )
    }

    if (showLinkBankDialog) {
        LinkBankDialog(
            institutions = viewModel.supportedInstitutions,
            onDismiss = { showLinkBankDialog = false },
            onLinkAccount = { inst, name, type, balance ->
                viewModel.linkNewBankAccount(inst, name, type, balance)
            }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            accounts = accounts,
            currencySymbol = currencySymbol,
            onDismiss = { showAddTransactionDialog = false },
            onAdd = { title, merchant, amount, type, category, accountId, notes ->
                viewModel.addManualTransaction(title, merchant, amount, type, category, accountId, notes)
            }
        )
    }

    if (showQuickTransferDialog) {
        QuickTransferDialog(
            accounts = accounts,
            currencySymbol = currencySymbol,
            onDismiss = { showQuickTransferDialog = false },
            onExecuteTransfer = { fromId, toId, amt, note ->
                viewModel.executeInterAccountTransfer(fromId, toId, amt, note)
            }
        )
    }
}
