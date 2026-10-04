package com.fieldsync.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(form: FormEntity)

    @Update
    suspend fun update(form: FormEntity)

    @Query("SELECT * FROM forms ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FormEntity>>

    @Query("SELECT * FROM forms ORDER BY createdAt DESC")
    suspend fun getAll(): List<FormEntity>

    @Query("SELECT * FROM forms WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun getPending(): List<FormEntity>

    @Query("SELECT * FROM forms WHERE id = :id")
    suspend fun getById(id: String): FormEntity?

    @Query("UPDATE forms SET status = :status, syncedAt = :syncedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, syncedAt: String?, updatedAt: String)

    @Query("UPDATE forms SET syncAttempts = syncAttempts + 1 WHERE id = :id")
    suspend fun incrementAttempts(id: String)

    @Query("DELETE FROM forms WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM forms WHERE status = 'pending'")
    suspend fun countPending(): Int
}
