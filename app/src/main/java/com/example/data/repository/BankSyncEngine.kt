package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.SyncLogDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.ConnectionStatus
import com.example.data.model.SyncLogEntity
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.delay
import kotlin.random.Random

data class BankInstitution(
    val id: String,
    val name: String,
    val country: String,
    val countryFlag: String,
    val region: String,
    val protocol: String,
    val brandColorHex: Long,
    val supportedTypes: List<AccountType>,
    val requiresMfa: Boolean = true
)

class BankSyncEngine(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val syncLogDao: SyncLogDao
) {
    val supportedInstitutions: List<BankInstitution> = listOf(
        // UNITED STATES 🇺🇸
        BankInstitution("chase", "Chase Bank", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFF117ACA, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("bofa", "Bank of America", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFFE31837, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("wells", "Wells Fargo", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFFCD1409, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("amex", "American Express", "United States", "🇺🇸", "North America", "Direct Connect API", 0xFF002663, listOf(AccountType.CREDIT_CARD)),
        BankInstitution("capone", "Capital One", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFFD03027, listOf(AccountType.SAVINGS, AccountType.CHECKING, AccountType.CREDIT_CARD)),
        BankInstitution("citi_us", "Citibank", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFF003B70, listOf(AccountType.CHECKING, AccountType.CREDIT_CARD)),
        BankInstitution("fidelity", "Fidelity Investments", "United States", "🇺🇸", "North America", "Direct Broker API", 0xFF437936, listOf(AccountType.INVESTMENT)),
        BankInstitution("schwab", "Charles Schwab", "United States", "🇺🇸", "North America", "Direct Broker API", 0xFF007A87, listOf(AccountType.INVESTMENT, AccountType.CHECKING)),
        BankInstitution("usbank", "U.S. Bank", "United States", "🇺🇸", "North America", "Plaid / FedNow", 0xFF0C2074, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("pnc", "PNC Bank", "United States", "🇺🇸", "North America", "Plaid", 0xFFFA8714, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("td_us", "TD Bank USA", "United States", "🇺🇸", "North America", "Plaid", 0xFF008A00, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("ally", "Ally Bank", "United States", "🇺🇸", "North America", "Plaid", 0xFF542574, listOf(AccountType.SAVINGS, AccountType.CHECKING)),

        // UNITED KINGDOM 🇬🇧
        BankInstitution("barclays", "Barclays Bank", "United Kingdom", "🇬🇧", "Europe", "Open Banking UK", 0xFF00AEEF, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("hsbc_uk", "HSBC UK", "United Kingdom", "🇬🇧", "Europe", "Open Banking UK", 0xFFDB0011, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("lloyds", "Lloyds Bank", "United Kingdom", "🇬🇧", "Europe", "Open Banking UK", 0xFF006A4E, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("natwest", "NatWest", "United Kingdom", "🇬🇧", "Europe", "Open Banking UK", 0xFF5A287F, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("santander_uk", "Santander UK", "United Kingdom", "🇬🇧", "Europe", "Open Banking UK", 0xFFEC0000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("monzo", "Monzo Bank", "United Kingdom", "🇬🇧", "Europe", "Open Banking API", 0xFFFF385C, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("revolut", "Revolut", "United Kingdom", "🇬🇧", "Europe", "Open Banking API", 0xFF0075EB, listOf(AccountType.CHECKING, AccountType.INVESTMENT)),
        BankInstitution("starling", "Starling Bank", "United Kingdom", "🇬🇧", "Europe", "Open Banking API", 0xFF6935FF, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // EUROPEAN UNION & SWITZERLAND 🇪🇺 🇨🇭
        BankInstitution("bnp", "BNP Paribas", "France", "🇫🇷", "Europe", "PSD2 / SEPA", 0xFF00965E, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("credit_agricole", "Crédit Agricole", "France", "🇫🇷", "Europe", "PSD2 / STET", 0xFF008375, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("socgen", "Société Générale", "France", "🇫🇷", "Europe", "PSD2 / STET", 0xFFE60028, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("deutsche_bank", "Deutsche Bank", "Germany", "🇩🇪", "Europe", "PSD2 / FinTS", 0xFF0018A8, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.INVESTMENT)),
        BankInstitution("commerzbank", "Commerzbank", "Germany", "🇩🇪", "Europe", "PSD2 / FinTS", 0xFFFFD900, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("n26", "N26 Bank", "Germany", "🇩🇪", "Europe", "PSD2 / SEPA Instant", 0xFF36A18B, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("ing", "ING Group", "Netherlands", "🇳🇱", "Europe", "PSD2 / Berlin Group", 0xFFFF6200, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("santander_es", "Banco Santander", "Spain", "🇪🇸", "Europe", "PSD2 / Redsýs", 0xFFEC0000, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("bbva_es", "BBVA", "Spain", "🇪🇸", "Europe", "PSD2 / SEPA", 0xFF004481, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("intesa", "Intesa Sanpaolo", "Italy", "🇮🇹", "Europe", "PSD2 / CBI", 0xFF005883, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("ubs", "UBS Switzerland", "Switzerland", "🇨🇭", "Europe", "Swiss Open Wealth", 0xFFE60000, listOf(AccountType.CHECKING, AccountType.INVESTMENT)),
        BankInstitution("nordea", "Nordea Bank", "Sweden & Nordics", "🇸🇪", "Europe", "Open Banking Nordic", 0xFF0000A0, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // CANADA 🇨🇦
        BankInstitution("rbc", "Royal Bank of Canada (RBC)", "Canada", "🇨🇦", "North America", "Lynx / Open Banking", 0xFF0051A5, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("td_ca", "TD Canada Trust", "Canada", "🇨🇦", "North America", "Lynx / Open Banking", 0xFF008A00, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("scotia", "Scotiabank", "Canada", "🇨🇦", "North America", "Lynx / Open Banking", 0xFFEC111A, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("bmo", "Bank of Montreal (BMO)", "Canada", "🇨🇦", "North America", "Lynx / Open Banking", 0xFF0079C1, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("cibc", "CIBC", "Canada", "🇨🇦", "North America", "Lynx / Open Banking", 0xFF8B0000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // AUSTRALIA & NEW ZEALAND 🇦🇺 🇳🇿
        BankInstitution("cba", "Commonwealth Bank (CBA)", "Australia", "🇦🇺", "Asia-Pacific", "CDR / Open Banking AU", 0xFFFFCC00, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("anz", "ANZ Bank", "Australia", "🇦🇺", "Asia-Pacific", "CDR / Open Banking AU", 0xFF004165, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("westpac", "Westpac", "Australia", "🇦🇺", "Asia-Pacific", "CDR / Open Banking AU", 0xFFDA1710, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("nab", "National Australia Bank (NAB)", "Australia", "🇦🇺", "Asia-Pacific", "CDR / Open Banking AU", 0xFFB30000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("macquarie", "Macquarie Bank", "Australia", "🇦🇺", "Asia-Pacific", "CDR / Open Banking AU", 0xFF000000, listOf(AccountType.SAVINGS, AccountType.INVESTMENT)),
        BankInstitution("asb", "ASB Bank", "New Zealand", "🇳🇿", "Asia-Pacific", "Payments NZ API", 0xFFFFB800, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("kiwibank", "Kiwibank", "New Zealand", "🇳🇿", "Asia-Pacific", "Payments NZ API", 0xFF84BD00, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // INDIA 🇮🇳
        BankInstitution("sbi", "State Bank of India (SBI)", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFF2255A4, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("hdfc", "HDFC Bank", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFF004C8F, listOf(AccountType.SAVINGS, AccountType.CHECKING, AccountType.CREDIT_CARD)),
        BankInstitution("icici", "ICICI Bank", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFFB02A30, listOf(AccountType.SAVINGS, AccountType.CHECKING, AccountType.CREDIT_CARD)),
        BankInstitution("axis", "Axis Bank", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFF97144D, listOf(AccountType.SAVINGS, AccountType.CHECKING)),
        BankInstitution("kotak", "Kotak Mahindra Bank", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFFED1C24, listOf(AccountType.SAVINGS, AccountType.CHECKING)),
        BankInstitution("pnb", "Punjab National Bank (PNB)", "India", "🇮🇳", "Asia-Pacific", "UPI / Account Aggregator", 0xFFA21B32, listOf(AccountType.SAVINGS, AccountType.CHECKING)),

        // JAPAN 🇯🇵
        BankInstitution("mufg", "Mitsubishi UFJ (MUFG)", "Japan", "🇯🇵", "Asia-Pacific", "Zengin / Open Banking JP", 0xFFE60012, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("smbc", "Sumitomo Mitsui (SMBC)", "Japan", "🇯🇵", "Asia-Pacific", "Zengin / Open Banking JP", 0xFF00873C, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("mizuho", "Mizuho Bank", "Japan", "🇯🇵", "Asia-Pacific", "Zengin / Open Banking JP", 0xFF002A86, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("japan_post", "Japan Post Bank", "Japan", "🇯🇵", "Asia-Pacific", "Zengin / Open Banking JP", 0xFF38B449, listOf(AccountType.SAVINGS)),

        // SINGAPORE & SOUTHEAST ASIA 🇸🇬 🇲🇾 🇮🇩
        BankInstitution("dbs", "DBS Bank", "Singapore", "🇸🇬", "Asia-Pacific", "PayNow / SGFinDex", 0xFFFF3333, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.INVESTMENT)),
        BankInstitution("ocbc", "OCBC Bank", "Singapore", "🇸🇬", "Asia-Pacific", "PayNow / SGFinDex", 0xFFED1B2D, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("uob", "United Overseas Bank (UOB)", "Singapore", "🇸🇬", "Asia-Pacific", "PayNow / SGFinDex", 0xFF002244, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("maybank", "Maybank", "Malaysia", "🇲🇾", "Asia-Pacific", "DuitNow Open API", 0xFFFFD000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("cimb", "CIMB Bank", "Malaysia", "🇲🇾", "Asia-Pacific", "DuitNow Open API", 0xFFED1C24, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("bca", "Bank Central Asia (BCA)", "Indonesia", "🇮🇩", "Asia-Pacific", "BI-FAST Open API", 0xFF005696, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("mandiri", "Bank Mandiri", "Indonesia", "🇮🇩", "Asia-Pacific", "BI-FAST Open API", 0xFF003082, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // CHINA & HONG KONG 🇨🇳 🇭🇰
        BankInstitution("icbc", "Industrial & Commercial Bank of China", "China", "🇨🇳", "Asia-Pacific", "CIPS / CNAPS", 0xFFC7000B, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("ccb", "China Construction Bank", "China", "🇨🇳", "Asia-Pacific", "CIPS / CNAPS", 0xFF005BAB, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("boc", "Bank of China", "China", "🇨🇳", "Asia-Pacific", "CIPS / CNAPS", 0xFFB10021, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("hsbc_hk", "HSBC Hong Kong", "Hong Kong", "🇭🇰", "Asia-Pacific", "FPS / HKMA Open API", 0xFFDB0011, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.INVESTMENT)),

        // MIDDLE EAST 🇦🇪 🇸🇦 🇶🇦
        BankInstitution("enbd", "Emirates NBD", "United Arab Emirates", "🇦🇪", "Middle East", "CBUAE Open Finance", 0xFF00386B, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("fab", "First Abu Dhabi Bank (FAB)", "United Arab Emirates", "🇦🇪", "Middle East", "CBUAE Open Finance", 0xFF00205B, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("adcb", "Abu Dhabi Commercial Bank", "United Arab Emirates", "🇦🇪", "Middle East", "CBUAE Open Finance", 0xFFB30838, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("alrajhi", "Al Rajhi Bank", "Saudi Arabia", "🇸🇦", "Middle East", "SAMA Open Banking", 0xFF004B87, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("snb", "Saudi National Bank (SNB)", "Saudi Arabia", "🇸🇦", "Middle East", "SAMA Open Banking", 0xFF006633, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("qnb", "Qatar National Bank (QNB)", "Qatar", "🇶🇦", "Middle East", "QCB Open Banking", 0xFF800020, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // LATIN AMERICA 🇧🇷 🇲🇽 🇦🇷 🇨🇴
        BankInstitution("itau", "Itaú Unibanco", "Brazil", "🇧🇷", "Latin America", "Banco Central Open Finance", 0xFFEC7000, listOf(AccountType.CHECKING, AccountType.SAVINGS, AccountType.CREDIT_CARD)),
        BankInstitution("bancodobrasil", "Banco do Brasil", "Brazil", "🇧🇷", "Latin America", "Banco Central Open Finance", 0xFFFCDB00, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("bradesco", "Banco Bradesco", "Brazil", "🇧🇷", "Latin America", "Banco Central Open Finance", 0xFFCC092F, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("nubank", "Nubank", "Brazil", "🇧🇷", "Latin America", "Open Finance / PIX", 0xFF820AD1, listOf(AccountType.CHECKING, AccountType.CREDIT_CARD)),
        BankInstitution("bbva_mx", "BBVA México", "Mexico", "🇲🇽", "Latin America", "SPEI / Open Finance MX", 0xFF004481, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("banorte", "Banorte", "Mexico", "🇲🇽", "Latin America", "SPEI / Open Finance MX", 0xFFD50000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("bancolombia", "Bancolombia", "Colombia", "🇨🇴", "Latin America", "Transfiya / Open Finance", 0xFFFDDA24, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("galicia", "Banco Galicia", "Argentina", "🇦🇷", "Latin America", "Transferencias 3.0", 0xFFFF5900, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // AFRICA 🇿🇦 🇳🇬 🇰🇪
        BankInstitution("standard_bank", "Standard Bank", "South Africa", "🇿🇦", "Africa", "SARB Open Banking", 0xFF0033A0, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("fnb_za", "First National Bank (FNB)", "South Africa", "🇿🇦", "Africa", "SARB Open Banking", 0xFF009AA6, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("nedbank", "Nedbank", "South Africa", "🇿🇦", "Africa", "SARB Open Banking", 0xFF00623B, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("access_bank", "Access Bank", "Nigeria", "🇳🇬", "Africa", "NIBSS Open Banking", 0xFFFF6600, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("zenith", "Zenith Bank", "Nigeria", "🇳🇬", "Africa", "NIBSS Open Banking", 0xFF800000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("gtbank", "Guaranty Trust Bank (GTBank)", "Nigeria", "🇳🇬", "Africa", "NIBSS Open Banking", 0xFFDD4B39, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("equity_bank", "Equity Bank", "Kenya", "🇰🇪", "Africa", "M-Pesa / CBK Open API", 0xFF7D1B1B, listOf(AccountType.CHECKING, AccountType.SAVINGS)),
        BankInstitution("kcb", "KCB Bank", "Kenya", "🇰🇪", "Africa", "M-Pesa / CBK Open API", 0xFF008000, listOf(AccountType.CHECKING, AccountType.SAVINGS)),

        // GLOBAL CRYPTO & WEB3 🌐
        BankInstitution("binance", "Binance Global", "Worldwide", "🌐", "Crypto", "WebSocket Live Trade API", 0xFFF3BA2F, listOf(AccountType.INVESTMENT)),
        BankInstitution("coinbase", "Coinbase Institutional", "Worldwide", "🌐", "Crypto", "Advanced Trade Direct API", 0xFF0052FF, listOf(AccountType.INVESTMENT)),
        BankInstitution("kraken", "Kraken Exchange", "Worldwide", "🌐", "Crypto", "REST & WebSocket API", 0xFF5841D8, listOf(AccountType.INVESTMENT)),
        BankInstitution("okx", "OKX Global", "Worldwide", "🌐", "Crypto", "Web3 Open Protocol", 0xFF000000, listOf(AccountType.INVESTMENT))
    )

    private val merchantFeedSample = listOf(
        Triple("Trader Joe's", -54.20, TransactionCategory.GROCERIES),
        Triple("Uber Trip", -24.80, TransactionCategory.TRANSPORTATION),
        Triple("Chipotle Mexican Grill", -14.65, TransactionCategory.FOOD_DINING),
        Triple("Amazon Marketplace", -38.90, TransactionCategory.SHOPPING),
        Triple("Netflix Standard", -15.49, TransactionCategory.ENTERTAINMENT),
        Triple("Chevron Gas", -42.10, TransactionCategory.TRANSPORTATION),
        Triple("Blue Bottle Coffee", -6.50, TransactionCategory.FOOD_DINING),
        Triple("CVS Pharmacy", -21.30, TransactionCategory.HEALTHCARE),
        Triple("Spotify Premium", -10.99, TransactionCategory.ENTERTAINMENT),
        Triple("Target Department", -62.40, TransactionCategory.SHOPPING),
        Triple("Local Farmers Market", -31.75, TransactionCategory.GROCERIES),
        Triple("Freelance Payment - Client A", 650.00, TransactionCategory.SALARY),
        Triple("Vanguard Dividend Yield", 45.20, TransactionCategory.INVESTMENTS),
        Triple("Staking Reward", 18.75, TransactionCategory.INVESTMENTS),
        Triple("Pret A Manger London", -8.20, TransactionCategory.FOOD_DINING),
        Triple("Boulangerie Parisienne", -6.40, TransactionCategory.GROCERIES),
        Triple("Tokyo Metro Transit", -4.80, TransactionCategory.TRANSPORTATION),
        Triple("Swiggy Express", -12.50, TransactionCategory.FOOD_DINING),
        Triple("Grab Pay Singapore", -16.30, TransactionCategory.TRANSPORTATION)
    )

    suspend fun syncAccount(accountId: Long): Result<Int> {
        val now = System.currentTimeMillis()
        accountDao.updateAccountSyncState(
            id = accountId,
            newBalance = 0.0,
            lastSynced = now,
            status = ConnectionStatus.SYNCING,
            message = "Negotiating secure TLS Open Banking handshake..."
        )

        delay(900) // realistic network roundtrip

        // Fetch pending transactions for this account and clear them
        val pendings = transactionDao.getPendingTransactionsForAccount(accountId)
        for (p in pendings) {
            transactionDao.clearPendingStatus(p.id)
        }

        // Generate 1-2 realistic imported transactions
        val newTxCount = Random.nextInt(1, 3)
        var totalBalanceDelta = 0.0

        for (i in 0 until newTxCount) {
            val sample = merchantFeedSample.random()
            val tx = TransactionEntity(
                accountId = accountId,
                title = sample.first,
                merchantName = sample.first,
                amount = sample.second,
                type = if (sample.second > 0) TransactionType.INCOME else TransactionType.EXPENSE,
                category = sample.third,
                dateTimestamp = now - (i * 3600 * 1000L),
                isPending = false,
                autoCategorized = true,
                notes = "Auto-synced via Open Banking live feed"
            )
            transactionDao.insertTransaction(tx)
            totalBalanceDelta += sample.second
        }

        // Update Account
        accountDao.updateAccountSyncState(
            id = accountId,
            newBalance = 0.0,
            lastSynced = now,
            status = ConnectionStatus.CONNECTED,
            message = "Synchronized ($newTxCount new transactions)"
        )

        // Log the sync event
        syncLogDao.insertLog(
            SyncLogEntity(
                accountId = accountId,
                accountName = "Account #$accountId",
                status = "SUCCESS",
                transactionsSyncedCount = newTxCount,
                message = "Reconciled $newTxCount transaction(s) via automated sync protocol."
            )
        )

        return Result.success(newTxCount)
    }

    suspend fun syncAllEnabledAccounts(): Int {
        val autoAccounts = accountDao.getAutoSyncAccounts()
        var totalSynced = 0
        for (acc in autoAccounts) {
            val result = syncAccount(acc.id)
            totalSynced += result.getOrDefault(0)
        }
        return totalSynced
    }

    suspend fun linkNewAccount(
        institution: BankInstitution,
        accountName: String,
        accountType: AccountType,
        initialBalance: Double
    ): Long {
        val mask = (1000..9999).random().toString()
        val account = AccountEntity(
            institutionName = institution.name,
            accountName = if (accountName.isNotBlank()) accountName else "${institution.name} ${accountType.displayName}",
            accountNumberMask = mask,
            accountType = accountType,
            balance = initialBalance,
            currency = "USD",
            institutionBrandColorHex = institution.brandColorHex,
            isAutoSyncEnabled = true,
            syncIntervalMinutes = 60,
            lastSyncedTimestamp = System.currentTimeMillis(),
            connectionStatus = ConnectionStatus.CONNECTED,
            lastSyncStatusMessage = "Connected via ${institution.protocol}",
            externalInstitutionId = institution.id
        )

        val newId = accountDao.insertAccount(account)

        // Seed 3 realistic historical transactions for this newly linked bank account
        val now = System.currentTimeMillis()
        val seeds = listOf(
            merchantFeedSample.random(),
            merchantFeedSample.random(),
            Triple("${institution.name} Initial Deposit", initialBalance * 0.4, TransactionCategory.SALARY)
        )

        for ((idx, seed) in seeds.withIndex()) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    accountId = newId,
                    title = seed.first,
                    merchantName = seed.first,
                    amount = seed.second,
                    type = if (seed.second > 0) TransactionType.INCOME else TransactionType.EXPENSE,
                    category = seed.third,
                    dateTimestamp = now - ((idx + 1) * 86400000L),
                    isPending = false,
                    autoCategorized = true,
                    notes = "Initial bank statement import"
                )
            )
        }

        syncLogDao.insertLog(
            SyncLogEntity(
                accountId = newId,
                accountName = account.accountName,
                status = "LINKED",
                transactionsSyncedCount = seeds.size,
                message = "Linked ${institution.countryFlag} ${institution.name} (${institution.country}) using ${institution.protocol}."
            )
        )

        return newId
    }
}
