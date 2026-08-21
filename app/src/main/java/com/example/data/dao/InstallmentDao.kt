package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.example.data.model.Installment

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installments ORDER BY nextDueDate ASC")
    fun getAllInstallments(): Flow<List<Installment>>

    @Insert
    suspend fun insertInstallment(installment: Installment)

    @Update
    suspend fun updateInstallment(installment: Installment)

    @Delete
    suspend fun deleteInstallment(installment: Installment)
}
