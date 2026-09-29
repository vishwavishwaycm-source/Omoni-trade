package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val institutionName: String,
    val accountName: String,
    val accountNumberMask: String,
    val accountType: AccountType,
    val balance: Double,
    val currency: String = "USD",
    val institutionBrandColorHex: Long = 0xFF0D47A1,
    val isAutoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 60,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val lastSyncStatusMessage: String = "Up to date",
    val externalInstitutionId: String = ""
)
