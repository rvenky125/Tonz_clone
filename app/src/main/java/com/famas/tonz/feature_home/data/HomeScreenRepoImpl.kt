package com.famas.tonz.feature_home.data

import com.famas.tonz.core.util.UiContact
import com.famas.tonz.core.util.toUiContact
import com.famas.tonz.feature_home.domain.HomeScreenRepository
import contacts.core.Contacts

class HomeScreenRepoImpl(
    private val contactsApi: Contacts
) : HomeScreenRepository {
    override suspend fun getContacts(
        nextKey: Int,
        pageSize: Int,
        searchValue: String
    ): Result<List<UiContact>> {
        return try {
            val contacts = if (searchValue.isNotBlank()) contactsApi
                .broadQuery()
//                .offset(nextKey * pageSize)
//                .limit(pageSize)
                .wherePartiallyMatches(searchValue)
                .find()
                .mapNotNull { it.toUiContact() }
            else contactsApi.query()
//                .offset(nextKey * pageSize)
//                .limit(pageSize)
                .find()
                .mapNotNull { it.toUiContact() }

            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}