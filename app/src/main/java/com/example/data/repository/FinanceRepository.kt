package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.BudgetDao
import com.example.data.local.SyncLogDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.BudgetEntity
import com.example.data.model.SyncLogEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class NetWorthSummary(
    val netWorth: Double,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val netSavings: Double,
    val savingsRate: Double
)

data class CategorySpending(
    val category: TransactionCategory,
    val totalSpent: Double,
    val percentageOfTotal: Float,
    val transactionCount: Int
)

class FinanceRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val syncLogDao: SyncLogDao,
    val syncEngine: BankSyncEngine
) {
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val recentSyncLogs: Flow<List<SyncLogEntity>> = syncLogDao.getRecentLogs(30)

    val netWorthSummary: Flow<NetWorthSummary> = combine(
        allAccounts,
        allTransactions
    ) { accounts, transactions ->
        var assets = 0.0
        var liabilities = 0.0

        for (acc in accounts) {
            if (acc.accountType == AccountType.CREDIT_CARD || acc.accountType == AccountType.LOAN) {
                // If negative or positive, treat magnitude as liability
                liabilities += Math.abs(acc.balance)
            } else {
                assets += acc.balance
            }
        }
        val net = assets - liabilities

        // Monthly calculations
        var income = 0.0
        var expenses = 0.0
        for (tx in transactions) {
            if (tx.type == TransactionType.INCOME) {
                income += tx.amount
            } else if (tx.type == TransactionType.EXPENSE) {
                expenses += Math.abs(tx.amount)
            }
        }
        val savings = income - expenses
        val rate = if (income > 0) ((savings / income) * 100).coerceIn(-100.0, 100.0) else 0.0

        NetWorthSummary(
            netWorth = net,
            totalAssets = assets,
            totalLiabilities = liabilities,
            monthlyIncome = income,
            monthlyExpenses = expenses,
            netSavings = savings,
            savingsRate = rate
        )
    }

    val categorySpendings: Flow<List<CategorySpending>> = allTransactions.combine(allBudgets) { txs, _ ->
        val expenseTxs = txs.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenseTxs.sumOf { Math.abs(it.amount) }

        val map = mutableMapOf<TransactionCategory, Double>()
        val countMap = mutableMapOf<TransactionCategory, Int>()
        for (tx in expenseTxs) {
            map[tx.category] = (map[tx.category] ?: 0.0) + Math.abs(tx.amount)
            countMap[tx.category] = (countMap[tx.category] ?: 0) + 1
        }

        map.map { (cat, amount) ->
            CategorySpending(
                category = cat,
                totalSpent = amount,
                percentageOfTotal = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f,
                transactionCount = countMap[cat] ?: 0
            )
        }.sortedByDescending { it.totalSpent }
    }

    suspend fun insertTransaction(tx: TransactionEntity): Long {
        return transactionDao.insertTransaction(tx)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun toggleAccountAutoSync(accountId: Long, enabled: Boolean) {
        accountDao.toggleAutoSync(accountId, enabled)
    }

    suspend fun deleteAccount(accountId: Long) {
        accountDao.deleteAccountById(accountId)
    }

    suspend fun saveBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudgetById(id)
    }
}
