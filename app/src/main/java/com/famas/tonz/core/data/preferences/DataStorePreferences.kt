package com.famas.tonz.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.famas.tonz.feature_feed.data.remote.responses.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStorePreferences(
    private val context: Context,
    private val json: Json
) : Preferences {
    companion object {
        const val MAIN_DATA_STORE = "__MAIN_DATA_STORE__"
        val USER_DATA_KEY = stringPreferencesKey("__user_data__")
        val ADS_PROMOTION_COUNT = intPreferencesKey("__ads_promotion_count__")
        val REFERRAL_DATA = stringPreferencesKey("__referral_data__")
        val UPDATE_DLG_SHOW_COUNT = intPreferencesKey("__update_dlg_to_show__")
    }

    private val Context.datastore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(
        name = MAIN_DATA_STORE
    )

    override fun getUserData(): Flow<User?> {
        return context.datastore.data.map {
            json.decodeFromString<User>(it[USER_DATA_KEY] ?: return@map null)
        }
    }

    override suspend fun setUserData(user: User) {
        context.datastore.edit {
            it[USER_DATA_KEY] = json.encodeToString(user)
        }
    }

    override suspend fun clearUserData() {
        context.datastore.edit {
            it.remove(USER_DATA_KEY)
        }
    }

    override fun getAdsPromotionCount(): Flow<Int> {
        return context.datastore.data.mapNotNull {
            it[ADS_PROMOTION_COUNT]
        }
    }

    override suspend fun setAdsPromotionCount(count: Int) {
        context.datastore.edit {
            it[ADS_PROMOTION_COUNT] = count
        }
    }

    override suspend fun clearAdsPromotionCount() {
        context.datastore.edit {
            it.remove(ADS_PROMOTION_COUNT)
        }
    }

    override suspend fun setReferralData(referralData: ReferralData) {
        context.datastore.edit {
            it[REFERRAL_DATA] = json.encodeToString(referralData)
        }
    }

    override suspend fun getReferralData(): ReferralData? {
        return context.datastore.data.map {
            json.decodeFromString<ReferralData>(
                it[REFERRAL_DATA] ?: return@map null
            )
        }.firstOrNull()
    }

    override suspend fun observeReferralData(): Flow<ReferralData?> {
        return context.datastore.data.mapNotNull {
            json.decodeFromString<ReferralData>(
                it[REFERRAL_DATA] ?: return@mapNotNull null
            )
        }
    }

    override suspend fun incrementUpdateDialogToShowCount() {
        context.datastore.edit {
            val count = getUpdateDialogToShowCount().firstOrNull() ?: 0
            it[UPDATE_DLG_SHOW_COUNT] = count + 1
        }
    }

    override fun getUpdateDialogToShowCount(): Flow<Int> {
        return context.datastore.data.mapNotNull {
            it[UPDATE_DLG_SHOW_COUNT] ?: return@mapNotNull 0
        }
    }

    override suspend fun disableUpdateDialogToShow() {
        context.datastore.edit {
            it[UPDATE_DLG_SHOW_COUNT] = -1
        }
    }
}