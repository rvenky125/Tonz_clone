package com.famas.tonz.feature_feed.presentation.feed_screen

import android.content.Context
import com.famas.tonz.feature_feed.domain.models.RingtonePostModel

sealed class FeedScreenEvent {
    data class OnLogin(val tokenId: String) : FeedScreenEvent()
    data class OnSelectTag(val tag: FeedListTag) : FeedScreenEvent()
    data class OnClickGridItem(val index: Int) : FeedScreenEvent()
    data class OnSearchValueChange(val value: String) : FeedScreenEvent()
    data object OnDismissSelectedRingtonePost : FeedScreenEvent()
    data class OnProgressChange(val it: Float) : FeedScreenEvent()
    data object TogglePlay : FeedScreenEvent()
    data object PauseAudio : FeedScreenEvent()
    data class OnClickSetRingtonFromDlg(val ringtonePost: RingtonePostModel) : FeedScreenEvent()
    data class OnSelectOptionFromBottomSheet(
        val trimRingtone: Boolean = false,
        val downloadOnly: Boolean = false
    ) : FeedScreenEvent()

    data class OnToggleLikeRingtone(val ringtonePost: RingtonePostModel) : FeedScreenEvent()
    data object IncrementPage : FeedScreenEvent()
    data object ClearAudio : FeedScreenEvent()
    data object OnDismissOptionsSheet : FeedScreenEvent()
    data object Refresh : FeedScreenEvent()
    data object OnDismissExplicitRingtonePost : FeedScreenEvent()
    data object ToggleLanguageSheet : FeedScreenEvent()
    data class OnSelectLanguage(val language: String?) : FeedScreenEvent()

    data class LoadSongWithId(val ringtoneId: String) : FeedScreenEvent()
    data class OnShareRingtoneToOtherApps(
        val ringtone: RingtonePostModel,
        val context: Context
    ) : FeedScreenEvent()

    data class OnClickRingtoneForOptions(val ringtonePost: RingtonePostModel) : FeedScreenEvent()
    data class OnChangeReferralCode(val value: String) : FeedScreenEvent()
    data class SetLoginLoading(val loading: Boolean) : FeedScreenEvent()
}