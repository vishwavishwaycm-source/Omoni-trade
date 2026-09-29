package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccountType
import com.example.data.model.ConnectionStatus
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType?): String = value?.name ?: AccountType.CHECKING.name

    @TypeConverter
    fun toAccountType(value: String?): AccountType =
        value?.let { runCatching { AccountType.valueOf(it) }.getOrDefault(AccountType.CHECKING) }
            ?: AccountType.CHECKING

    @TypeConverter
    fun fromConnectionStatus(value: ConnectionStatus?): String =
        value?.name ?: ConnectionStatus.CONNECTED.name

    @TypeConverter
    fun toConnectionStatus(value: String?): ConnectionStatus =
        value?.let { runCatching { ConnectionStatus.valueOf(it) }.getOrDefault(ConnectionStatus.CONNECTED) }
            ?: ConnectionStatus.CONNECTED

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String =
        value?.name ?: TransactionType.EXPENSE.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType =
        value?.let { runCatching { TransactionType.valueOf(it) }.getOrDefault(TransactionType.EXPENSE) }
            ?: TransactionType.EXPENSE

    @TypeConverter
    fun fromTransactionCategory(value: TransactionCategory?): String =
        value?.name ?: TransactionCategory.OTHER.name

    @TypeConverter
    fun toTransactionCategory(value: String?): TransactionCategory =
        value?.let { runCatching { TransactionCategory.valueOf(it) }.getOrDefault(TransactionCategory.OTHER) }
            ?: TransactionCategory.OTHER
}
