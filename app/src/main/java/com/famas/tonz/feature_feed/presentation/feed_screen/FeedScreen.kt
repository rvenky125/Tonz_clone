package com.famas.tonz.feature_feed.presentation.feed_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.famas.tonz.core.MainActivityEvent
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.core.ad_util.AdmobBanner
import com.famas.tonz.core.components.CircularTextField
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.destinations.FeedScreenDestination
import com.famas.tonz.destinations.MusicScreenDestination
import com.famas.tonz.feature_feed.presentation.components.GridItem
import com.famas.tonz.feature_feed.presentation.components.SongDetailModalFooterContent
import com.famas.tonz.feature_feed.presentation.components.SongDetailsLt
import com.famas.tonz.feature_feed.presentation.components.UserNotLoggedInLt
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun FeedScreen(
    feedScreenVM: FeedScreenVM = hiltViewModel(),
    mainActivityVM: MainActivityVM,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    ringtoneId: String? = null
) {
    val loggedInUser = mainActivityVM.userData.value
    val isUserLoggedIn = loggedInUser != null
    val coroutineScope = rememberCoroutineScope()
    val state = feedScreenVM.feedScreenState.value
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(confirmValueChange = {
        if (it == SheetValue.Hidden) {
            feedScreenVM.onEvent(FeedScreenEvent.OnDismissOptionsSheet)
            true
        } else true
    })

    val pagerState = rememberPagerState(
        initialPage = state.selectedRingtonePostModelIndex ?: 0,
        pageCount = { state.ringtonePosts.size })

    var coroutineJob: Job? = remember {
        null
    }

    LaunchedEffect(key1 = state.selectedRingtonePostModelIndex, block = {
        if (state.selectedRingtonePostModelIndex == pagerState.settledPage)
            return@LaunchedEffect
        coroutineScope.launch {
            pagerState.scrollToPage(state.selectedRingtonePostModelIndex ?: return@launch)
        }
    })

    LaunchedEffect(pagerState.settledPage) {
        if (state.ringtonePosts.isEmpty()) return@LaunchedEffect
        if (pagerState.settledPage == state.selectedRingtonePostModelIndex) return@LaunchedEffect
        coroutineJob?.cancel()
        coroutineJob = coroutineScope.launch {
            delay(300)
            if (pagerState.settledPage == -1) {
                return@launch
            }
            feedScreenVM.onEvent(FeedScreenEvent.OnClickGridItem(pagerState.settledPage))
            if (pagerState.settledPage == state.ringtonePosts.size - 2) {
                feedScreenVM.onEvent(FeedScreenEvent.IncrementPage)
            }
        }
    }

    LaunchedEffect(key1 = ringtoneId, block = {
        if (ringtoneId != null) {
            feedScreenVM.onEvent(FeedScreenEvent.LoadSongWithId(ringtoneId))
        }
    })

    LaunchedEffect(key1 = state.ringtoneForBottomSheet, block = {
        if (state.ringtoneForBottomSheet == null && sheetState.hasExpandedState) {
            sheetState.hide()
        }
        if (state.ringtoneForBottomSheet != null && !sheetState.hasExpandedState) {
            sheetState.expand()
        }
    })

    LaunchedEffect(key1 = Unit, block = {
        feedScreenVM.onAddWorkId = { workId, showOnlyProgress ->
            mainActivityVM.onEvent(
                MainActivityEvent.SetWorkIdToShowDialog(
                    id = workId,
                    showOnlyProgressOfWorker = showOnlyProgress
                )
            )
        }

        feedScreenVM.onGetFileUriToSetRingtone = { fileUri, trim ->
            mainActivityVM.onEvent(MainActivityEvent.OnSelectFileUriToSetRingtone(fileUri, trim))
        }

        feedScreenVM.uiEventFlow.collectLatest {
            when (it) {
                is UiEvent.ShowSnackBar -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        snackbarHostState.showSnackbar(it.uiText.getString(context))
                    }
                }

                UiEvent.NavigateBack -> {
                    navController.navigateUp()
                }

                else -> {}
            }
        }
    })

    val currentScreen = navController.currentDestinationAsState().value
    LaunchedEffect(key1 = currentScreen, block = {
        if (currentScreen?.route != FeedScreenDestination.route) {
            feedScreenVM.onEvent(FeedScreenEvent.ClearAudio)
        }
    })

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!state.loading && state.ringtonePosts.isEmpty()) {
            TextButton(onClick = { feedScreenVM.onEvent(FeedScreenEvent.Refresh) }) {
                Text(text = "Reload")
            }
        }

        SwipeRefresh(
            state = rememberSwipeRefreshState(state.loading),
            onRefresh = { feedScreenVM.onEvent(FeedScreenEvent.Refresh) },
        ) {
            FeedGrid(
                state = state,
                onEvent = feedScreenVM::onEvent,
                showAds = (loggedInUser?.adsCoins ?: 10) < 10,
                onClickNavigateToWeb = {
                    navController.navigate(MusicScreenDestination(initialSearchQuery = state.searchValue))
                }
            )
        }

        if (state.loading && state.currentPage == 1) {
            CircularProgressIndicator()
        }

        if (!isUserLoggedIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .pointerInput(Unit) {}, contentAlignment = Alignment.Center
            ) {
                UserNotLoggedInLt(
                    { feedScreenVM.onEvent(FeedScreenEvent.OnLogin(it)) },
                    onStartLogin = { feedScreenVM.onEvent(FeedScreenEvent.SetLoginLoading(true)) },
                    loading = state.loginLoading,
                    onCancelLogin = {
                        feedScreenVM.onEvent(FeedScreenEvent.SetLoginLoading(false))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                )
            }
        }
    }

    if (state.selectedRingtonePostModelIndex != null) {
        Dialog(onDismissRequest = {
            feedScreenVM.onEvent(FeedScreenEvent.OnDismissSelectedRingtonePost)
        }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fill,
                    contentPadding = PaddingValues(horizontal = 52.dp),
                    pageSpacing = 10.dp,
                    beyondBoundsPageCount = 1,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    if (pageIndex < state.ringtonePosts.size) {
                        SongDetailsLt(
                            ringtonePostModel = state.ringtonePosts[pageIndex],
                            onLike = {
                                feedScreenVM.onEvent(
                                    FeedScreenEvent.OnToggleLikeRingtone(
                                        state.ringtonePosts[pageIndex]
                                    )
                                )
                            }
                        )
                    }
                }

                SongDetailModalFooterContent(
                    isPlaying = state.isPlaying,
                    progress = state.progress,
                    onTogglePlay = {
                        feedScreenVM.onEvent(
                            FeedScreenEvent.TogglePlay
                        )
                    },
                    onProgressChange = { pro ->
                        feedScreenVM.onEvent(
                            FeedScreenEvent.OnProgressChange(
                                pro
                            )
                        )
                    },
                    isPlayerLoading = state.isPlayerLoading,
                    showAds = (loggedInUser?.adsCoins
                        ?: 10) < 10,
                    onClickSetRingtone = {
                        feedScreenVM.onEvent(FeedScreenEvent.OnClickSetRingtonFromDlg(state.ringtonePosts[pagerState.currentPage]))
                    },
                    onShare = {
                        feedScreenVM.onEvent(
                            FeedScreenEvent.OnShareRingtoneToOtherApps(
                                state.ringtonePosts[pagerState.currentPage],
                                context
                            )
                        )
                    },
                    skipPrevious = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.scrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    skipNext = {
                        if (pagerState.currentPage < (state.ringtonePosts.size - 1)) {
                            coroutineScope.launch {
                                pagerState.scrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                )
            }
        }
    }


    if (state.ringtonePostToShowExplicitly != null) {
        Dialog(onDismissRequest = {
            feedScreenVM.onEvent(FeedScreenEvent.OnDismissExplicitRingtonePost)
        }) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                SongDetailsLt(ringtonePostModel = state.ringtonePostToShowExplicitly,
                    onLike = {
                        feedScreenVM.onEvent(
                            FeedScreenEvent.OnToggleLikeRingtone(
                                state.ringtonePostToShowExplicitly
                            )
                        )
                    }
                )

                SongDetailModalFooterContent(
                    isPlaying = state.isPlaying,
                    progress = state.progress,
                    onTogglePlay = {
                        feedScreenVM.onEvent(
                            FeedScreenEvent.TogglePlay
                        )
                    },
                    onProgressChange = { pro ->
                        feedScreenVM.onEvent(
                            FeedScreenEvent.OnProgressChange(
                                pro
                            )
                        )
                    },
                    isPlayerLoading = state.isPlayerLoading,
                    showAds = (loggedInUser?.adsCoins
                        ?: 10) < 10,
                    onClickSetRingtone = {
                        feedScreenVM.onEvent(FeedScreenEvent.OnClickSetRingtonFromDlg(state.ringtonePostToShowExplicitly))
                    },
                    onShare = {
                        feedScreenVM.onEvent(
                            FeedScreenEvent.OnShareRingtoneToOtherApps(
                                state.ringtonePostToShowExplicitly,
                                context
                            )
                        )
                    }
                )
            }
        }
    }

    if (state.ringtoneForBottomSheet != null) {
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = {
            feedScreenVM.onEvent(FeedScreenEvent.OnDismissOptionsSheet)
        }, dragHandle = {}) {
            Column(modifier = Modifier.padding(16.dp)) {
                DropdownMenuItem(text = {
                    Text(text = "Set it as Ringtone")
                }, onClick = {
                    feedScreenVM.onEvent(FeedScreenEvent.OnSelectOptionFromBottomSheet(false))
                })

                DropdownMenuItem(text = {
                    Text(text = "Cut and Set Ringtone")
                }, onClick = {
                    feedScreenVM.onEvent(FeedScreenEvent.OnSelectOptionFromBottomSheet(true))
                })

                DropdownMenuItem(text = {
                    Text(text = "Download the ringtone")
                }, onClick = {
                    feedScreenVM.onEvent(FeedScreenEvent.OnSelectOptionFromBottomSheet(downloadOnly = true))
                })
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    if (state.showLanguagesSheet) {
        ModalBottomSheet(onDismissRequest = {
            feedScreenVM.onEvent(FeedScreenEvent.ToggleLanguageSheet)
        }) {
            TextButton(
                onClick = { feedScreenVM.onEvent(FeedScreenEvent.OnSelectLanguage(null)) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp)
            ) {
                Text(text = "Clear selection")
            }
            LazyColumn {
                items(state.languages) {
                    ListItem(headlineContent = { Text(text = it) }, trailingContent = {
                        if (state.selectedLanguage == it) {
                            Icon(imageVector = Icons.Default.Done, contentDescription = "selected")
                        }
                    }, modifier = Modifier.clickable {
                        feedScreenVM.onEvent(FeedScreenEvent.OnSelectLanguage(it))
                    })
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun FeedGrid(
    state: FeedScreenState,
    showAds: Boolean,
    onEvent: (FeedScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
    onClickNavigateToWeb: () -> Unit
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            CircularTextField(value = state.searchValue, onValueChange = {
                onEvent(FeedScreenEvent.OnSearchValueChange(it))
            }, leadingIcon = {
                if (state.searchValue.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                onEvent(FeedScreenEvent.OnSearchValueChange(""))
                            }
                        })
                } else {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                }
            }, placeholder = {
                Text(text = "Search song, movie or artist")
            }, modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = if (state.languages.isEmpty()) 16.dp else 0.dp)
            )

            if (state.languages.isNotEmpty()) {
                BadgedBox(badge = {
                    if (state.selectedLanguage != null) {
                        Badge()
                    }
                },
                    modifier = Modifier
                        .padding(end = 16.dp, start = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            onEvent(FeedScreenEvent.ToggleLanguageSheet)
                        }
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Change language"
                    )
                }
            }
        }

        state.errMessage?.let {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
//                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            ) {
                Text(
                    text = "Can't find one?",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Button(onClick = { onClickNavigateToWeb() }) {
                    Text(text = "Click to search on web")
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.width(16.dp))
            FeedListTag.values().forEach {
                FilterChip(selected = state.feedListTags.contains(it), onClick = {
                    onEvent(FeedScreenEvent.OnSelectTag(it))
                }, label = {
                    Text(text = it.label)
                }, modifier = Modifier.padding(end = 10.dp)
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            modifier = Modifier.weight(1f),
            verticalItemSpacing = 4.dp
        ) {
            itemsIndexed(state.ringtonePosts) { index, item ->
                if (index == state.ringtonePosts.size - 1 && index > ((state.currentPage * 20) - 2) && !state.loading && !state.isEndReached) {
                    onEvent(FeedScreenEvent.IncrementPage)
                }
                GridItem(
                    ringtonePostModel = item,
                    onLike = {
                        onEvent(FeedScreenEvent.OnToggleLikeRingtone(item))
                    },
                    onClick = {
                        onEvent(FeedScreenEvent.OnClickGridItem(index))
                    },
                    onOptionsClick = { onEvent(FeedScreenEvent.OnClickRingtoneForOptions(item)) },
                    onShare = {
                        onEvent(FeedScreenEvent.OnShareRingtoneToOtherApps(item, context))
                    }
                )
            }
        }

        if (state.loadingExtra) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        if (showAds) {
            AdmobBanner()
        }
    }
}