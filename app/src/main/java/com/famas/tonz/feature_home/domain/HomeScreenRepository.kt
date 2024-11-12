package com.famas.tonz.feature_home.domain

import com.famas.tonz.core.util.UiContact

interface HomeScreenRepository {
    suspend fun getContacts(nextKey: Int, pageSize: Int, searchValue: String): Result<List<UiContact>>
}