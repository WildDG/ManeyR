package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.example.data.model.SavingAllocation

@Dao
interface SavingAllocationDao {
    @Query("SELECT * FROM saving_allocations WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun getAllocationsForGoal(goalId: Int): Flow<List<SavingAllocation>>

    @Insert
    suspend fun insertAllocation(allocation: SavingAllocation)

    @Update
    suspend fun updateAllocation(allocation: SavingAllocation)

    @Delete
    suspend fun deleteAllocation(allocation: SavingAllocation)
}
