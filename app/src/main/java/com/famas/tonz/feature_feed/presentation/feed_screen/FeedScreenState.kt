package com.famas.tonz.feature_feed.presentation.feed_screen

import com.famas.tonz.feature_feed.domain.models.RingtonePostModel

data class FeedScreenState(
    val loading: Boolean = false,
    val ringtonePosts: List<RingtonePostModel> = emptyList(),
    val feedListTags: List<FeedListTag> = listOf(FeedListTag.RecentUploads),
    val selectedRingtonePostModelIndex: Int? = null,
    val searchValue: String = "",
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val totalDuration: Long? = null,
    val isPlayerLoading: Boolean = false,
    val currentPage: Int = 1,
    val ringtoneForBottomSheet: RingtonePostModel? = null,
    val errMessage: String? = null,
    val referralCode: String = "",
    val referralCodeErrMessage: String? = null,
    val submittingReferralCode: Boolean = false,
    val isEndReached: Boolean = false,
    val loadingExtra: Boolean = false,
    val loginLoading: Boolean = false,
    val ringtonePostToShowExplicitly: RingtonePostModel? = null,
    val languages: List<String> = emptyList(),
    val showLanguagesSheet: Boolean = false,
    val selectedLanguage: String? = null
)
