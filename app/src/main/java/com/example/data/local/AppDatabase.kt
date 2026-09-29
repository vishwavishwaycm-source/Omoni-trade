package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.ConnectionStatus
import com.example.data.model.SyncLogEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SyncLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vaultsync_finance_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val accountDao = database.accountDao()
                val transactionDao = database.transactionDao()
                val budgetDao = database.budgetDao()
                val syncLogDao = database.syncLogDao()

                // Initial bank & investment accounts
                val now = System.currentTimeMillis()
                val chaseId = accountDao.insertAccount(
                    AccountEntity(
                        institutionName = "Chase Bank",
                        accountName = "Total Checking",
                        accountNumberMask = "•••• 4821",
                        accountType = AccountType.CHECKING,
                        balance = 4850.25,
                        institutionBrandColorHex = 0xFF117ACA,
                        isAutoSyncEnabled = true,
                        syncIntervalMinutes = 60,
                        lastSyncedTimestamp = now - 180000, // 3 mins ago
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastSyncStatusMessage = "Automatic sync completed",
                        externalInstitutionId = "chase"
                    )
                )

                val amexId = accountDao.insertAccount(
                    AccountEntity(
                        institutionName = "American Express",
                        accountName = "Gold Rewards Card",
                        accountNumberMask = "•••• 1008",
                        accountType = AccountType.CREDIT_CARD,
                        balance = -742.60,
                        institutionBrandColorHex = 0xFF002663,
                        isAutoSyncEnabled = true,
                        syncIntervalMinutes = 60,
                        lastSyncedTimestamp = now - 600000,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastSyncStatusMessage = "Automatic sync completed",
                        externalInstitutionId = "amex"
                    )
                )

                val capOneId = accountDao.insertAccount(
                    AccountEntity(
                        institutionName = "Capital One",
                        accountName = "360 Performance Savings",
                        accountNumberMask = "•••• 9342",
                        accountType = AccountType.SAVINGS,
                        balance = 18420.00,
                        institutionBrandColorHex = 0xFFD03027,
                        isAutoSyncEnabled = true,
                        syncIntervalMinutes = 120,
                        lastSyncedTimestamp = now - 1200000,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastSyncStatusMessage = "Automatic sync completed",
                        externalInstitutionId = "capitalone"
                    )
                )

                val vanguardId = accountDao.insertAccount(
                    AccountEntity(
                        institutionName = "Vanguard",
                        accountName = "VTSAX Index Brokerage",
                        accountNumberMask = "•••• 5519",
                        accountType = AccountType.INVESTMENT,
                        balance = 32180.50,
                        institutionBrandColorHex = 0xFF9E1B32,
                        isAutoSyncEnabled = true,
                        syncIntervalMinutes = 240,
                        lastSyncedTimestamp = now - 3600000,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastSyncStatusMessage = "Market positions refreshed",
                        externalInstitutionId = "vanguard"
                    )
                )

                val cryptoId = accountDao.insertAccount(
                    AccountEntity(
                        institutionName = "Coinbase Vault",
                        accountName = "Crypto Portfolio",
                        accountNumberMask = "•••• 7731",
                        accountType = AccountType.INVESTMENT,
                        balance = 8945.30,
                        institutionBrandColorHex = 0xFF0052FF,
                        isAutoSyncEnabled = true,
                        syncIntervalMinutes = 30,
                        lastSyncedTimestamp = now - 120000,
                        connectionStatus = ConnectionStatus.CONNECTED,
                        lastSyncStatusMessage = "Synced with blockchain live node",
                        externalInstitutionId = "coinbase"
                    )
                )

                // Seed Transactions
                val oneDay = 86400000L
                val seedTxs = listOf(
                    TransactionEntity(
                        accountId = chaseId,
                        title = "Direct Deposit - TechCorp",
                        merchantName = "TechCorp Payroll",
                        amount = 3850.00,
                        type = TransactionType.INCOME,
                        category = TransactionCategory.SALARY,
                        dateTimestamp = now - 2 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Semi-monthly salary"
                    ),
                    TransactionEntity(
                        accountId = amexId,
                        title = "Whole Foods Market",
                        merchantName = "Whole Foods",
                        amount = -118.45,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.GROCERIES,
                        dateTimestamp = now - 3600000 * 4,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Weekly organic groceries"
                    ),
                    TransactionEntity(
                        accountId = amexId,
                        title = "Starbucks Reserve",
                        merchantName = "Starbucks",
                        amount = -7.85,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.FOOD_DINING,
                        dateTimestamp = now - 3600000 * 2,
                        isPending = true,
                        autoCategorized = true,
                        notes = "Morning cold brew"
                    ),
                    TransactionEntity(
                        accountId = chaseId,
                        title = "Metropolitan Electric & Gas",
                        merchantName = "Metro Utility",
                        amount = -142.10,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.UTILITIES,
                        dateTimestamp = now - oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Electric bill autopay"
                    ),
                    TransactionEntity(
                        accountId = amexId,
                        title = "Apple Subscription",
                        merchantName = "Apple.com/bill",
                        amount = -14.99,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.ENTERTAINMENT,
                        dateTimestamp = now - 3 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "iCloud & Apple Music"
                    ),
                    TransactionEntity(
                        accountId = chaseId,
                        title = "Shell Gas Station",
                        merchantName = "Shell Oil",
                        amount = -48.20,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.TRANSPORTATION,
                        dateTimestamp = now - 4 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Refuel commute"
                    ),
                    TransactionEntity(
                        accountId = capOneId,
                        title = "Capital One Monthly Interest",
                        merchantName = "Capital One Bank",
                        amount = 64.30,
                        type = TransactionType.INCOME,
                        category = TransactionCategory.SALARY,
                        dateTimestamp = now - 5 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "4.35% APY High-yield interest"
                    ),
                    TransactionEntity(
                        accountId = vanguardId,
                        title = "Vanguard Quarterly Dividend",
                        merchantName = "Vanguard Funds",
                        amount = 184.50,
                        type = TransactionType.INCOME,
                        category = TransactionCategory.INVESTMENTS,
                        dateTimestamp = now - 7 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Reinvested dividend"
                    ),
                    TransactionEntity(
                        accountId = amexId,
                        title = "Target Retail Store",
                        merchantName = "Target",
                        amount = -86.50,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.SHOPPING,
                        dateTimestamp = now - 2 * oneDay,
                        isPending = false,
                        autoCategorized = true,
                        notes = "Home essentials"
                    ),
                    TransactionEntity(
                        accountId = chaseId,
                        title = "Sweetgreen Salad",
                        merchantName = "Sweetgreen",
                        amount = -16.40,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.FOOD_DINING,
                        dateTimestamp = now - 3600000 * 8,
                        isPending = true,
                        autoCategorized = true,
                        notes = "Lunch bowl"
                    )
                )
                transactionDao.insertTransactions(seedTxs)

                // Seed Budgets
                val budgets = listOf(
                    BudgetEntity(category = TransactionCategory.FOOD_DINING, monthlyLimit = 500.0, periodMonthYear = "current"),
                    BudgetEntity(category = TransactionCategory.GROCERIES, monthlyLimit = 600.0, periodMonthYear = "current"),
                    BudgetEntity(category = TransactionCategory.SHOPPING, monthlyLimit = 350.0, periodMonthYear = "current"),
                    BudgetEntity(category = TransactionCategory.UTILITIES, monthlyLimit = 250.0, periodMonthYear = "current"),
                    BudgetEntity(category = TransactionCategory.TRANSPORTATION, monthlyLimit = 200.0, periodMonthYear = "current"),
                    BudgetEntity(category = TransactionCategory.ENTERTAINMENT, monthlyLimit = 150.0, periodMonthYear = "current")
                )
                budgetDao.insertBudgets(budgets)

                // Seed initial sync log
                syncLogDao.insertLog(
                    SyncLogEntity(
                        accountId = chaseId,
                        accountName = "Chase Total Checking",
                        status = "SUCCESS",
                        transactionsSyncedCount = 3,
                        message = "Automatic live sync completed successfully."
                    )
                )
                syncLogDao.insertLog(
                    SyncLogEntity(
                        accountId = amexId,
                        accountName = "Amex Gold Card",
                        status = "SUCCESS",
                        transactionsSyncedCount = 4,
                        message = "Cleared pending authorization charges."
                    )
                )
            }
        }
    }
}
