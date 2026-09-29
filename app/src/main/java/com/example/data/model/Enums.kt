package com.example.data.model

enum class AccountType(val displayName: String) {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    CREDIT_CARD("Credit Card"),
    INVESTMENT("Investment"),
    LOAN("Loan")
}

enum class ConnectionStatus(val displayName: String) {
    CONNECTED("Connected"),
    SYNCING("Syncing..."),
    ERROR("Sync Issue"),
    REQUIRES_REAUTH("Action Required")
}

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class TransactionCategory(val displayName: String, val iconName: String) {
    FOOD_DINING("Food & Dining", "Restaurant"),
    GROCERIES("Groceries", "ShoppingCart"),
    SHOPPING("Shopping", "Store"),
    HOUSING("Housing & Rent", "Home"),
    TRANSPORTATION("Transportation", "DirectionsCar"),
    UTILITIES("Bills & Utilities", "Bolt"),
    ENTERTAINMENT("Entertainment", "Theaters"),
    HEALTHCARE("Healthcare", "LocalHospital"),
    SALARY("Income & Salary", "AttachMoney"),
    INVESTMENTS("Investments", "TrendingUp"),
    TRAVEL("Travel", "Flight"),
    EDUCATION("Education", "School"),
    OTHER("Other", "Category")
}
