package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransactionTag
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionTags(transactionTags: List<TransactionTag>)

    @Query("DELETE FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun deleteTagsForTransaction(transactionId: Int)

    @Query("SELECT tagId FROM transaction_tags WHERE transactionId = :transactionId")
    fun getTagIdsForTransaction(transactionId: Int): Flow<List<String>>

    @Query("SELECT tagId FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun getTagIdsForTransactionSync(transactionId: Int): List<String>
}
