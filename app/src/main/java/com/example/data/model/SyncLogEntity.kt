package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_logs")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val accountName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val transactionsSyncedCount: Int,
    val message: String
)
