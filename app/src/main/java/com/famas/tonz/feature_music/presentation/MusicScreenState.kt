package com.famas.tonz.feature_music.presentation

import com.famas.tonz.feature_music.data.MusicWebPage

data class MusicScreenState(
    val loading: Boolean = false,
    val loadingWebPages: Boolean = false,
    val selectedWebPage: MusicWebPage? = null,
    val webpages: List<MusicWebPage> = emptyList(),
    val selectedTabIndex: Int = 0,
    val loginLoading: Boolean = false,
    val searchQuery: String = "",
    val searchEngineUrl: String? = null,
    val searchOnlyInSuggested: Boolean = true
)