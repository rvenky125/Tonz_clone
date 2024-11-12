package com.famas.tonz.feature_feed.data

import android.content.Context
import android.util.Log
import com.famas.tonz.core.core_states.UserDataState
import com.famas.tonz.core.data.BasicResponse
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.feature_feed.data.remote.FeedApi
import com.famas.tonz.feature_feed.data.remote.responses.Ringtone
import com.famas.tonz.feature_feed.domain.FeedRepository
import com.famas.tonz.feature_feed.presentation.feed_screen.FeedListTag
import java.io.IOException
import java.nio.channels.UnresolvedAddressException

class FeedRepositoryImpl(
    private val feedApi: FeedApi,
    private val userDataState: UserDataState,
    private val preferences: Preferences,
    private val context: Context
) : FeedRepository {
    override suspend fun getRingtones(
        tags: List<FeedListTag>,
        pageNumber: Int,
        searchValue: String,
        language: String?
    ): BasicResponse<Ringtone> {
        return try {
            feedApi.getRingtones(
                tags.map { it.value },
                userDataState.userData.value?.id ?: return BasicResponse(
                    msg = "Please logout and login again",
                    successful = false
                ),
                pageNumber = pageNumber,
                search = searchValue,
                language = language
            )
        }  catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = "Something went wrong", successful = false)
        }
    }

    override suspend fun toggleLike(id: String): BasicResponse<Unit> {
        return try {
            feedApi.toggleLike(
                id,
                userDataState.userData.value?.id ?: return BasicResponse(
                    msg = "Please logout and login again",
                    successful = false
                )
            )
        }  catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = "Something went wrong", successful = false)
        }
    }

    override suspend fun getRingtone(ringtoneId: String): BasicResponse<Ringtone> {
        return try {
            feedApi.getRingtone(
                ringtoneId,
                userDataState.userData.value?.id ?: return BasicResponse(
                    msg = "Please logout and login again",
                    successful = false
                )
            )
        }  catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = "Something went wrong", successful = false)
        }
    }

    override suspend fun getLanguages(): BasicResponse<String> {
        return try {
            feedApi.getLanguages()
        }  catch (e: IOException) {
            Log.d("myTag", e.localizedMessage, e)
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: UnresolvedAddressException) {
            BasicResponse(
                msg = "Couldn't reach server. Check your internet connection.", successful = false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BasicResponse(msg = "Something went wrong", successful = false)
        }
    }
}