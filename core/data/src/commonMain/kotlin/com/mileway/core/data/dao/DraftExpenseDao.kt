package com.mileway.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mileway.core.data.model.db.DraftExpenseEntity

@Dao
interface DraftExpenseDao {
    @Query("SELECT * FROM draft_expenses WHERE draftId = :draftId LIMIT 1")
    suspend fun getDraft(draftId: String = DraftExpenseEntity.SINGLETON_ID): DraftExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(draft: DraftExpenseEntity)

    @Query("DELETE FROM draft_expenses WHERE draftId = :draftId")
    suspend fun deleteDraft(draftId: String = DraftExpenseEntity.SINGLETON_ID)
}
