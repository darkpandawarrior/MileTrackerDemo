package com.mileway

import com.mileway.core.data.dao.DraftExpenseDao
import com.mileway.core.data.model.db.DraftExpenseEntity

/** In-memory fake for [DraftExpenseDao] — mirrors the single-row `draft_expenses` table (P1.5). */
class FakeDraftExpenseDao : DraftExpenseDao {
    private val drafts = LinkedHashMap<String, DraftExpenseEntity>()

    override suspend fun getDraft(draftId: String): DraftExpenseEntity? = drafts[draftId]

    override suspend fun upsertDraft(draft: DraftExpenseEntity) {
        drafts[draft.draftId] = draft
    }

    override suspend fun deleteDraft(draftId: String) {
        drafts.remove(draftId)
    }
}
