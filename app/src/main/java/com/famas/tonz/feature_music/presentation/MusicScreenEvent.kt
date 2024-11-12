package com.famas.tonz.feature_music.presentation

import com.famas.tonz.feature_music.data.MusicWebPage

sealed class MusicScreenEvent {
    data class OnGetDownloadFileUrl(
        val url: String,
        val headers: Map<String, String>,
        val method: String,
        val fileName: String
    ) : MusicScreenEvent()

    data class OnSelectMusicWebPage(val webPage: MusicWebPage?) : MusicScreenEvent()
    data class OnClickTab(val tabIndex: Int) : MusicScreenEvent()
    data class OnLogin(val credential: String) : MusicScreenEvent()
    data class SetLoginLoading(val value: Boolean) : MusicScreenEvent()
    data class OnSearchValueChange(val text: String) : MusicScreenEvent()
    data object CloseSearchEngine : MusicScreenEvent()
    data object OnToggleSearchOnlyInSuggested: MusicScreenEvent()
}
