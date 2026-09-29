package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.CryptoAsset
import com.example.data.model.CurrencyInfo
import com.example.data.model.GlobalMarketOverview
import com.example.data.model.StockIndex
import com.example.data.model.SyncLogEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.WorldCurrencies
import com.example.data.repository.BankInstitution
import com.example.data.repository.BankSyncEngine
import com.example.data.repository.CategorySpending
import com.example.data.repository.FinanceRepository
import com.example.data.repository.MarketRepository
import com.example.data.repository.NetWorthSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    DASHBOARD("Dashboard"),
    ACCOUNTS("Accounts"),
    TRANSACTIONS("Activity"),
    MARKETS("Markets"),
    BUDGETS("Budgets"),
    SETTINGS("Sync & Settings")
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val syncEngine = BankSyncEngine(
        accountDao = database.accountDao(),
        transactionDao = database.transactionDao(),
        syncLogDao = database.syncLogDao()
    )
    val financeRepository = FinanceRepository(
        accountDao = database.accountDao(),
        transactionDao = database.transactionDao(),
        budgetDao = database.budgetDao(),
        syncLogDao = database.syncLogDao(),
        syncEngine = syncEngine
    )
    private val marketRepository = MarketRepository()

    // Navigation state
    private val _selectedTab = MutableStateFlow(MainTab.DASHBOARD)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    // Currency conversion state
    private val _selectedCurrency = MutableStateFlow(WorldCurrencies.defaultCurrency)
    val selectedCurrency: StateFlow<CurrencyInfo> = _selectedCurrency.asStateFlow()

    private val _currencySymbol = MutableStateFlow(_selectedCurrency.value.symbol)
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    fun selectCurrency(currency: CurrencyInfo) {
        _selectedCurrency.value = currency
        _currencySymbol.value = currency.symbol
        _snackBarMessage.value = "Currency switched to ${currency.flag} ${currency.code} (${currency.symbol})"
    }

    // Selected Financial & Trading Model
    private val _selectedModel = MutableStateFlow(com.example.data.model.FinancialAnalysisModel.QUANT_MOMENTUM)
    val selectedModel: StateFlow<com.example.data.model.FinancialAnalysisModel> = _selectedModel.asStateFlow()

    fun selectModel(model: com.example.data.model.FinancialAnalysisModel) {
        _selectedModel.value = model
        _snackBarMessage.value = "Active Model: ${model.badge} - ${model.title}"
    }

    // Base live data streams (stored in USD base)
    val rawAccounts: StateFlow<List<AccountEntity>> = financeRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawTransactions: StateFlow<List<TransactionEntity>> = financeRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawNetWorthSummary: StateFlow<NetWorthSummary> = financeRepository.netWorthSummary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NetWorthSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        )

    val rawCategorySpendings: StateFlow<List<CategorySpending>> = financeRepository.categorySpendings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawBudgets: StateFlow<List<BudgetEntity>> = financeRepository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncLogs: StateFlow<List<SyncLogEntity>> = financeRepository.recentSyncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Worldwide Markets
    val stockIndices: StateFlow<List<StockIndex>> = marketRepository.stockIndices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawCryptoAssets: StateFlow<List<CryptoAsset>> = marketRepository.cryptoAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketOverview: StateFlow<GlobalMarketOverview> = marketRepository.marketOverview
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalMarketOverview())

    // DYNAMICALLY CONVERTED DATA STREAMS (Reflects selected country currency!)
    val accounts: StateFlow<List<AccountEntity>> = combine(rawAccounts, _selectedCurrency) { accs, curr ->
        accs.map { acc ->
            acc.copy(
                balance = acc.balance * curr.rateToUsd,
                currency = curr.code
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = combine(rawTransactions, _selectedCurrency) { txs, curr ->
        txs.map { tx ->
            tx.copy(
                amount = tx.amount * curr.rateToUsd
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val netWorthSummary: StateFlow<NetWorthSummary> = combine(rawNetWorthSummary, _selectedCurrency) { summary, curr ->
        val rate = curr.rateToUsd
        summary.copy(
            netWorth = summary.netWorth * rate,
            totalAssets = summary.totalAssets * rate,
            totalLiabilities = summary.totalLiabilities * rate,
            monthlyIncome = summary.monthlyIncome * rate,
            monthlyExpenses = summary.monthlyExpenses * rate,
            netSavings = summary.netSavings * rate
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetWorthSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))

    val categorySpendings: StateFlow<List<CategorySpending>> = combine(rawCategorySpendings, _selectedCurrency) { spendings, curr ->
        spendings.map { sp ->
            sp.copy(totalSpent = sp.totalSpent * curr.rateToUsd)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = combine(rawBudgets, _selectedCurrency) { bgs, curr ->
        bgs.map { bg ->
            bg.copy(monthlyLimit = bg.monthlyLimit * curr.rateToUsd)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cryptoAssets: StateFlow<List<CryptoAsset>> = combine(rawCryptoAssets, _selectedCurrency) { cryptos, curr ->
        cryptos.map { c ->
            c.copy(
                priceUsd = c.priceUsd * curr.rateToUsd,
                high24h = c.high24h * curr.rateToUsd,
                low24h = c.low24h * curr.rateToUsd,
                sparkline = c.sparkline.map { it * curr.rateToUsd.toFloat() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync State
    private val _isGlobalSyncing = MutableStateFlow(false)
    val isGlobalSyncing: StateFlow<Boolean> = _isGlobalSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _autoSyncIntervalMinutes = MutableStateFlow(60)
    val autoSyncIntervalMinutes: StateFlow<Int> = _autoSyncIntervalMinutes.asStateFlow()

    private val _isAutoSyncServiceActive = MutableStateFlow(true)
    val isAutoSyncServiceActive: StateFlow<Boolean> = _isAutoSyncServiceActive.asStateFlow()

    // Transaction filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAccountFilter = MutableStateFlow<Long?>(null)
    val selectedAccountFilter: StateFlow<Long?> = _selectedAccountFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<TransactionCategory?>(null)
    val selectedCategoryFilter: StateFlow<TransactionCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    // UI Feedback Banner
    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()

    private var autoSyncJob: Job? = null

    init {
        startAutomatedSyncWorker()
    }

    private fun startAutomatedSyncWorker() {
        autoSyncJob?.cancel()
        autoSyncJob = viewModelScope.launch {
            while (isActive) {
                if (_isAutoSyncServiceActive.value) {
                    val intervalMs = _autoSyncIntervalMinutes.value * 60 * 1000L
                    delay(intervalMs.coerceAtLeast(30000L))
                    performAutomaticSyncInternal()
                } else {
                    delay(30000L)
                }
            }
        }
    }

    private suspend fun performAutomaticSyncInternal() {
        if (_isGlobalSyncing.value) return
        _isGlobalSyncing.value = true
        try {
            val syncedCount = syncEngine.syncAllEnabledAccounts()
            _lastSyncTime.value = System.currentTimeMillis()
            if (syncedCount > 0) {
                _snackBarMessage.value = "Auto-sync updated $syncedCount transaction(s)."
            }
        } catch (_: Exception) {
            // handle gracefully
        } finally {
            _isGlobalSyncing.value = false
        }
    }

    fun syncAllAccounts() {
        viewModelScope.launch {
            if (_isGlobalSyncing.value) return@launch
            _isGlobalSyncing.value = true
            try {
                val syncedCount = syncEngine.syncAllEnabledAccounts()
                _lastSyncTime.value = System.currentTimeMillis()
                _snackBarMessage.value = "Bank synchronization complete. $syncedCount transaction(s) verified."
            } catch (e: Exception) {
                _snackBarMessage.value = "Bank synchronization encountered an error: ${e.message}"
            } finally {
                _isGlobalSyncing.value = false
            }
        }
    }

    fun syncSingleAccount(accountId: Long) {
        viewModelScope.launch {
            _isGlobalSyncing.value = true
            try {
                syncEngine.syncAccount(accountId)
                _lastSyncTime.value = System.currentTimeMillis()
                _snackBarMessage.value = "Account sync completed."
            } finally {
                _isGlobalSyncing.value = false
            }
        }
    }

    fun toggleAccountAutoSync(accountId: Long, enabled: Boolean) {
        viewModelScope.launch {
            financeRepository.toggleAccountAutoSync(accountId, enabled)
            _snackBarMessage.value = if (enabled) "Auto-sync enabled for account" else "Auto-sync paused for account"
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            financeRepository.deleteAccount(accountId)
            _snackBarMessage.value = "Account unlinked"
        }
    }

    fun linkNewBankAccount(
        institution: BankInstitution,
        accountName: String,
        accountType: AccountType,
        initialBalance: Double
    ) {
        viewModelScope.launch {
            _isGlobalSyncing.value = true
            try {
                // Normalize balance back to USD base before storing
                val rate = _selectedCurrency.value.rateToUsd
                val normalizedBalance = if (rate > 0) initialBalance / rate else initialBalance
                val newId = syncEngine.linkNewAccount(institution, accountName, accountType, normalizedBalance)
                _lastSyncTime.value = System.currentTimeMillis()
                _snackBarMessage.value = "Connected to ${institution.name} successfully!"
            } catch (e: Exception) {
                _snackBarMessage.value = "Failed to connect: ${e.message}"
            } finally {
                _isGlobalSyncing.value = false
            }
        }
    }

    fun addManualTransaction(
        title: String,
        merchant: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        accountId: Long,
        notes: String
    ) {
        viewModelScope.launch {
            // Normalize amount back to USD base before storing
            val rate = _selectedCurrency.value.rateToUsd
            val normalizedAmount = if (rate > 0) amount / rate else amount
            val adjustedAmount = if (type == TransactionType.EXPENSE) -Math.abs(normalizedAmount) else Math.abs(normalizedAmount)
            val tx = TransactionEntity(
                accountId = accountId,
                title = title,
                merchantName = merchant.ifBlank { title },
                amount = adjustedAmount,
                type = type,
                category = category,
                dateTimestamp = System.currentTimeMillis(),
                isPending = false,
                autoCategorized = false,
                notes = notes
            )
            financeRepository.insertTransaction(tx)
            _snackBarMessage.value = "Transaction recorded."
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            financeRepository.deleteTransaction(id)
            _snackBarMessage.value = "Transaction removed."
        }
    }

    fun saveBudget(category: TransactionCategory, monthlyLimit: Double) {
        viewModelScope.launch {
            // Normalize limit back to USD base
            val rate = _selectedCurrency.value.rateToUsd
            val normalizedLimit = if (rate > 0) monthlyLimit / rate else monthlyLimit
            val budget = BudgetEntity(
                category = category,
                monthlyLimit = normalizedLimit,
                periodMonthYear = "current"
            )
            financeRepository.saveBudget(budget)
            _snackBarMessage.value = "Budget for ${category.displayName} updated to ${_currencySymbol.value}$monthlyLimit."
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            financeRepository.deleteBudget(id)
            _snackBarMessage.value = "Budget removed."
        }
    }

    fun refreshMarkets() {
        viewModelScope.launch {
            marketRepository.refreshMarkets()
            _snackBarMessage.value = "Worldwide market quotes updated."
        }
    }

    fun simulateInstantBankPush() {
        viewModelScope.launch {
            _isGlobalSyncing.value = true
            delay(600)
            val autoAccounts = rawAccounts.value.filter { it.isAutoSyncEnabled }
            if (autoAccounts.isNotEmpty()) {
                val targetAccount = autoAccounts.random()
                val sample = listOf(
                    Triple("Uber Eats Dinner", -28.40, TransactionCategory.FOOD_DINING),
                    Triple("Costco Wholesale", -112.50, TransactionCategory.GROCERIES),
                    Triple("Amazon Prime Order", -45.99, TransactionCategory.SHOPPING),
                    Triple("Shell Fuel Station", -36.25, TransactionCategory.TRANSPORTATION),
                    Triple("Direct Transfer Cleared", 250.00, TransactionCategory.SALARY)
                ).random()

                val newTx = TransactionEntity(
                    accountId = targetAccount.id,
                    title = sample.first,
                    merchantName = sample.first,
                    amount = sample.second,
                    type = if (sample.second > 0) TransactionType.INCOME else TransactionType.EXPENSE,
                    category = sample.third,
                    dateTimestamp = System.currentTimeMillis(),
                    isPending = false,
                    autoCategorized = true,
                    notes = "Live webhook push from ${targetAccount.institutionName}"
                )
                financeRepository.insertTransaction(newTx)
                _snackBarMessage.value = "⚡ Live bank event: ${sample.first}"
            }
            _isGlobalSyncing.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAccountFilter(accountId: Long?) {
        _selectedAccountFilter.value = accountId
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun setAutoSyncInterval(minutes: Int) {
        _autoSyncIntervalMinutes.value = minutes
        startAutomatedSyncWorker()
        _snackBarMessage.value = "Auto-sync frequency set to every $minutes minutes."
    }

    fun toggleAutoSyncService(enabled: Boolean) {
        _isAutoSyncServiceActive.value = enabled
        _snackBarMessage.value = if (enabled) "Background auto-sync active" else "Background auto-sync paused"
    }

    fun clearSnackBar() {
        _snackBarMessage.value = null
    }

    // Strong Security Manager
    private val securityManager = com.example.ui.security.AppSecurityManager(application)
    val isAppLocked: StateFlow<Boolean> = securityManager.isLocked
    val isPinEnabled: StateFlow<Boolean> = securityManager.isPinEnabled
    val isBiometricEnabled: StateFlow<Boolean> = securityManager.isBiometricEnabled
    val isPrivacyMode: StateFlow<Boolean> = securityManager.isPrivacyMode

    fun unlockAppWithPin(pin: String): Boolean = securityManager.unlockWithPin(pin)
    fun unlockAppWithBiometrics() = securityManager.unlockWithBiometrics()
    fun togglePinSecurity(enabled: Boolean, pin: String = "1234") {
        securityManager.togglePinSecurity(enabled, pin)
        _snackBarMessage.value = if (enabled) "PIN & Biometric Lock enabled" else "App Lock disabled"
    }
    fun toggleBiometrics(enabled: Boolean) {
        securityManager.toggleBiometrics(enabled)
        _snackBarMessage.value = if (enabled) "Biometric unlock enabled" else "Biometric unlock disabled"
    }
    fun togglePrivacyMode() {
        securityManager.togglePrivacyMode()
        _snackBarMessage.value = if (securityManager.isPrivacyMode.value) "Privacy Shield ON (Balances masked)" else "Privacy Shield OFF"
    }
    fun lockApp() = securityManager.lockApp()

    // Easy Inter-Account Transfer
    fun executeInterAccountTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            val fromAcc = rawAccounts.value.find { it.id == fromAccountId }
            val toAcc = rawAccounts.value.find { it.id == toAccountId }
            if (fromAcc == null || toAcc == null) return@launch

            val rate = _selectedCurrency.value.rateToUsd
            val normalizedAmount = if (rate > 0) amount / rate else amount
            val now = System.currentTimeMillis()

            // 1. Debit from source
            val debitTx = TransactionEntity(
                accountId = fromAccountId,
                title = "Transfer to ${toAcc.accountName}",
                merchantName = "Internal Transfer",
                amount = -normalizedAmount,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.OTHER,
                dateTimestamp = now,
                isPending = false,
                autoCategorized = true,
                notes = note
            )
            financeRepository.insertTransaction(debitTx)

            // 2. Credit to destination
            val creditTx = TransactionEntity(
                accountId = toAccountId,
                title = "Transfer from ${fromAcc.accountName}",
                merchantName = "Internal Transfer",
                amount = normalizedAmount,
                type = TransactionType.INCOME,
                category = TransactionCategory.OTHER,
                dateTimestamp = now,
                isPending = false,
                autoCategorized = true,
                notes = note
            )
            financeRepository.insertTransaction(creditTx)

            _snackBarMessage.value = "Transferred ${_currencySymbol.value}$amount from ${fromAcc.accountName} to ${toAcc.accountName}."
        }
    }

    val supportedInstitutions: List<BankInstitution> = syncEngine.supportedInstitutions
}
