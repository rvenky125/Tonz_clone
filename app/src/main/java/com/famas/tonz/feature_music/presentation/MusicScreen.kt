package com.famas.tonz.feature_music.presentation

import android.util.Log
import android.webkit.JavascriptInterface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.famas.tonz.adblock.AdBlocker
import com.famas.tonz.core.MainActivityEvent
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.core.TAG
import com.famas.tonz.core.ad_util.AdmobBanner
import com.famas.tonz.core.components.CircularTextField
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.feature_feed.presentation.components.UserNotLoggedInLt
import com.famas.tonz.hexToColor
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Destination
@Composable
fun MusicScreen(
    viewModel: MusicScreenVM = hiltViewModel(),
    mainViewModel: MainActivityVM,
    snackbarHostState: SnackbarHostState,
    adBlocker: AdBlocker,
    initialSearchQuery: String? = null
) {
    val loggedInUser = mainViewModel.userData.value
    val isUserLoggedIn = loggedInUser != null

    val state = viewModel.musicScreenState
    val webViewState = rememberWebViewState(state.selectedWebPage?.url ?: "")
    val webViewNavigator = rememberWebViewNavigator()

    val searchEngineWebViewState =
        rememberWebViewState(state.searchEngineUrl ?: "")
    val searchEngineWebViewNavigator = rememberWebViewNavigator()

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        initialSearchQuery?.let {
            viewModel.onEvent(MusicScreenEvent.OnSearchValueChange(it))
        }
    }

    LaunchedEffect(key1 = Unit, block = {
        viewModel.onAddWorkId = {
            mainViewModel.onEvent(MainActivityEvent.SetWorkIdToShowDialog(it))
        }

        viewModel.uiEventFlow.collectLatest {
            when (it) {
                is UiEvent.ShowSnackBar -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(it.uiText.getString(context))
                    }
                }

                else -> {}
            }
        }
    })

    BackHandler(enabled = state.selectedWebPage?.url != null || state.searchEngineUrl != null) {
        if (state.searchEngineUrl != null && searchEngineWebViewNavigator.canGoBack) {
            searchEngineWebViewNavigator.navigateBack()
            return@BackHandler
        } else if (webViewNavigator.canGoBack) {
            webViewNavigator.navigateBack()
            return@BackHandler
        }

        viewModel.onEvent(MusicScreenEvent.OnSelectMusicWebPage(null))
        viewModel.onEvent(MusicScreenEvent.CloseSearchEngine)
    }

    if (adBlocker.loading.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "We are loading configuration, it's almost done...")
            CircularProgressIndicator()
        }
    } else if (adBlocker.isAdHostsEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = {
                adBlocker.loadFromAssets()
            }) {
                Text(text = "Reload config")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxSize()) {
                if (state.loadingWebPages || state.loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (state.selectedWebPage == null) {
                    Column {
                        CircularTextField(value = state.searchQuery, onValueChange = {
                            viewModel.onEvent(MusicScreenEvent.OnSearchValueChange(it))
                        }, leadingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures {
                                            viewModel.onEvent(
                                                MusicScreenEvent.OnSearchValueChange(
                                                    ""
                                                )
                                            )
                                        }
                                    })
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null
                                )
                            }
                        }, placeholder = {
                            Text(text = "Search anything...")
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                        if (state.searchQuery.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(text = "Search only in suggested webpages", style = MaterialTheme.typography.labelMedium)
                                Switch(checked = state.searchOnlyInSuggested, onCheckedChange = {
                                    viewModel.onEvent(MusicScreenEvent.OnToggleSearchOnlyInSuggested)
                                })
                            }
                        }
                    }
                }
                state.searchEngineUrl?.let { seUrl ->
                    WebViewScreen(
                        webViewState = searchEngineWebViewState,
                        onEvent = viewModel::onEvent,
                        webViewNavigator = searchEngineWebViewNavigator,
//                        downloadHint = null,
                        enableJavascript = true,
                        showAds = (loggedInUser?.adsCoins ?: 10) < 10,
                        adBlocker = adBlocker,
                        modifier = Modifier.weight(1f)
//                        allAcceptedHosts = state.webpages.mapNotNull { it.url }
                    )
                } ?: state.selectedWebPage?.let { webPage ->
                    WebViewScreen(
                        webViewState = webViewState,
                        onEvent = viewModel::onEvent,
                        webViewNavigator = webViewNavigator,
//                        downloadHint = webPage.downloadHint,
                        enableJavascript = webPage.enableJavascript ?: false,
                        showAds = (loggedInUser?.adsCoins ?: 10) < 10,
                        adBlocker = adBlocker,
                        modifier = Modifier.weight(1f)
//                        allAcceptedHosts = state.webpages.mapNotNull { it.url }
                    )
                } ?: run {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                        itemsIndexed(state.webpages) { index, webpage ->
                            if (
                                ((loggedInUser?.adsCoins
                                    ?: 10) < 10) && index > 0 && index % 4 == 0
                            ) {
                                AdmobBanner(
                                    width = LocalConfiguration.current.screenWidthDp - 16,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(10))
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            viewModel.onEvent(
                                                MusicScreenEvent.OnSelectMusicWebPage(
                                                    webpage
                                                )
                                            )
                                        }
                                    },
                            ) {
                                AsyncImage(
                                    model = webpage.pictureUrl,
                                    contentDescription = webpage.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentScale = ContentScale.Crop,
                                )
                                webpage.cardColor?.let { color ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    0f to color
                                                        .hexToColor()
                                                        .copy(alpha = 0.05f),
                                                    0.5f to color
                                                        .hexToColor()
                                                        .copy(alpha = 0.8f),
                                                    1f to color
                                                        .hexToColor(),
                                                )
                                            )
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .padding(top = 16.dp)
                                        .fillMaxWidth(0.5f)
                                        .height(150.dp)
                                        .align(Alignment.CenterEnd)
                                ) {
                                    Text(
                                        text = webpage.name ?: "Music Web Page",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = webpage.label ?: "",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(250.dp))
                        }
                    }
                }
            }

            if ((loggedInUser?.adsCoins ?: 10) < 10) {
                AdmobBanner(modifier = Modifier.align(Alignment.BottomCenter))
            }

            if (!isUserLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    UserNotLoggedInLt(
                        { viewModel.onEvent(MusicScreenEvent.OnLogin(it)) },
                        onStartLogin = { viewModel.onEvent(MusicScreenEvent.SetLoginLoading(true)) },
                        loading = state.loginLoading,
                        onCancelLogin = {
                            viewModel.onEvent(MusicScreenEvent.SetLoginLoading(false))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(it)
                            }
                        },
                    )
                }
            }

            if (!state.loadingWebPages && state.webpages.isEmpty()) {
                TextButton(onClick = { viewModel.refreshWebpages() }) {
                    Text(text = "Reload")
                }
            }
        }
    }
}

class JavaScriptInterface {
    @JavascriptInterface
    fun startDownload(downloadUrl: String?) {
        startDownloadFunc(downloadUrl)
    }
}

fun startDownloadFunc(url: String?) {
    Log.d(TAG, "Download: $url")
    // Create a DownloadManager request and enqueue the download
    // (same as in the previous response)
}
