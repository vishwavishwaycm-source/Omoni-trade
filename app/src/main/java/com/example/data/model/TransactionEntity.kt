package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["dateTimestamp"]),
        Index(value = ["category"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val title: String,
    val merchantName: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false,
    val autoCategorized: Boolean = true,
    val notes: String = "",
    val syncBatchId: String = ""
)
