package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: Long): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE isAutoSyncEnabled = 1")
    suspend fun getAutoSyncAccounts(): List<AccountEntity>

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("""
        UPDATE accounts 
        SET balance = :newBalance, 
            lastSyncedTimestamp = :lastSynced, 
            connectionStatus = :status, 
            lastSyncStatusMessage = :message 
        WHERE id = :id
    """)
    suspend fun updateAccountSyncState(
        id: Long,
        newBalance: Double,
        lastSynced: Long,
        status: ConnectionStatus,
        message: String
    )

    @Query("UPDATE accounts SET isAutoSyncEnabled = :enabled WHERE id = :id")
    suspend fun toggleAutoSync(id: Long, enabled: Boolean)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)
}
