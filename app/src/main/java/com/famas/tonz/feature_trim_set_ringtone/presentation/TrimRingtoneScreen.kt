package com.famas.tonz.feature_trim_set_ringtone.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.famas.tonz.R
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.core.TAG
import com.famas.tonz.core.ad_util.AdMobInterstitial
import com.famas.tonz.core.ui.navigation.TrimRingtoneNavArgs
import com.famas.tonz.core.ui.navigation.toDirection
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.RingtoneApi
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.feature_trim_set_ringtone.presentation.components.LeftAndRightHandleManualEdit
import com.famas.tonz.feature_trim_set_ringtone.presentation.components.ThumbnailForContact
import com.famas.tonz.feature_trim_set_ringtone.presentation.components.ZoomControls
import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File


@Composable
@Destination(navArgsDelegate = TrimRingtoneNavArgs::class)
fun TrimRingtoneScreen(
    viewModel: TrimRingtoneScreenVM = hiltViewModel(),
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    adMobInterstitial: AdMobInterstitial,
    mainVm: MainActivityVM
) {
    val context = LocalContext.current
    val contacts = remember {
        viewModel.navArgs.contactsArg.contacts
    }
    val state = viewModel.trimRingtoneScreenState.value
    val loggedInUser = mainVm.userData.value

    val launchSettingsPermissionIfNotProvided = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.System.canWrite(context)) {
            state.filePathToSaveAsRingtoneWithPendingPermission?.let {
                Log.d(TAG, "Path is: ${state.filePathToSaveAsRingtoneWithPendingPermission}")
                if (RingtoneApi(context).setDefaultRingtone(File(it))) {
                    CoroutineScope(Dispatchers.Main).launch {
                        if ((loggedInUser?.adsCoins ?: 0) < 10) {
                            adMobInterstitial.showAd()
                        }

                        snackbarHostState.showSnackbar(
                            UiText.StringResource(R.string.ringtone_set_successfully)
                                .getString(context)
                        )
                        navController.popBackStack(HomeScreenDestination.route, false)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar("Failed to set ringtone")
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = state.filePathToSaveAsRingtoneWithPendingPermission, block = {
        if (state.filePathToSaveAsRingtoneWithPendingPermission != null && !Settings.System.canWrite(
                context
            )
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchSettingsPermissionIfNotProvided.launch(intent)
        }
    })

    BackHandler(enabled = true) {
        viewModel.onEvent(TrimRingtoneEvent.ShowGoBackDialog)
    }

    LaunchedEffect(key1 = Unit, block = {
        viewModel.interstitial = adMobInterstitial
        if ((loggedInUser?.adsCoins ?: 0) < 10) {
            adMobInterstitial.loadAd(false)
        }

        viewModel.uiEventFlow.collectLatest {
            when (it) {
                is UiEvent.ShowSnackBar -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        snackbarHostState.showSnackbar(it.uiText.getString(context))
                    }
                }

                is UiEvent.OnNavigate -> {
                    navController.navigate(it.screen.toDirection())
                }

                UiEvent.NavigateBack -> {
                    navController.navigateUp()
                }
            }
        }
    })

    TrimRingtoneMainLt(
        state = viewModel.trimRingtoneScreenState.value,
        onPlayClicked = viewModel::updatePlaybackState,
        onProgressChange = viewModel::updateProgress,
        onEvent = viewModel::onEvent,
        onGoBack = {
            viewModel.onEvent(TrimRingtoneEvent.ShowGoBackDialog)
        },
        contacts = contacts.ifEmpty { null }
    )

    if (viewModel.trimRingtoneScreenState.value.loading) {
        Dialog(onDismissRequest = { }) {
            CircularProgressIndicator()
        }
    }

    if (state.showGoBackAlert) {
        AlertDialog(onDismissRequest = {
            viewModel.onEvent(TrimRingtoneEvent.DismissGoBackDialog)
        }, confirmButton = {
            TextButton(onClick = { navController.navigateUp() }) {
                Text(text = "Yes")
            }
        }, dismissButton = {
            TextButton(onClick = {
                viewModel.onEvent(TrimRingtoneEvent.DismissGoBackDialog)
            }) {
                Text(text = "Cancel")
            }
        }, title = {
            Text(text = "Alert")
        }, text = {
            Text(text = "Are you really want to discard and go back")
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrimRingtoneMainLt(
    state: TrimRingtoneState,
    onPlayClicked: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onEvent: (TrimRingtoneEvent) -> Unit,
    onGoBack: () -> Unit,
    contacts: List<UiContact>?
) {
    var activeTrimHandle by remember { mutableStateOf<TrimHandle?>(null) }
    val context = LocalContext.current
    var size by remember { mutableStateOf<Size?>(null) }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val handleLineColor = MaterialTheme.colorScheme.secondary
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var refreshDragValuesToDragGesture by remember {
        mutableIntStateOf(0)
    }
    val handlesHeightDiff = remember {
        60f
    }

    var verticalScrollEnabled by remember {
        mutableStateOf(false)
    }

    val leftHandleTopOffset = remember(size, state.trimLeftHandlePosition) {
        Offset(x = (size?.width ?: 0f) * state.trimLeftHandlePosition, y = handlesHeightDiff)
    }

    val leftHandleBottomOffset = remember(size, state.trimLeftHandlePosition) {
        Offset(
            x = (size?.width ?: 0f) * state.trimLeftHandlePosition,
            y = (size?.height ?: 0f) - handlesHeightDiff
        )
    }

    val rightHandleTopOffset = remember(key1 = size, key2 = state.trimRightHandlePosition) {
        Offset(x = (size?.width ?: 0f) * state.trimRightHandlePosition, y = handlesHeightDiff)
    }

    val rightHandleBottomOffset = remember(key1 = size, key2 = state.trimRightHandlePosition) {
        Offset(
            x = (size?.width ?: 0f) * state.trimRightHandlePosition,
            y = (size?.height ?: 0f) - handlesHeightDiff
        )
    }

    var leftHandleTimeToShow by remember {
        mutableFloatStateOf(0f)
    }
    var updateLeftHandleJob: Job? = remember { null }
    LaunchedEffect(state.trimLeftHandlePosition, state.totalDuration) {
        delay(100)
        updateLeftHandleJob?.cancel()

        updateLeftHandleJob = coroutineScope.launch {
            leftHandleTimeToShow = (state.totalDuration?.toFloat()
                ?.times(state.trimLeftHandlePosition))?.div(1000f)?.div(60f) ?: 0f
        }
    }

    var rightHandleTimeToShow by remember {
        mutableFloatStateOf(0f)
    }
    var updateRightHandleJob: Job? = remember { null }
    LaunchedEffect(state.trimRightHandlePosition, state.totalDuration) {
        delay(100)
        updateRightHandleJob?.cancel()

        updateRightHandleJob = coroutineScope.launch {
            rightHandleTimeToShow = (state.totalDuration?.toFloat()
                ?.times(state.trimRightHandlePosition))?.div(1000f)?.div(60f) ?: 0f
        }
    }


    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = "Trim and set ringtone")
        }, navigationIcon = {
            IconButton(onClick = { onGoBack() }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = { onEvent(TrimRingtoneEvent.SetShowShareRingtone(true)) }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.set_ringtone),
                    modifier = Modifier.padding(end = 5.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = stringResource(id = R.string.set_ringtone)
                )
            }
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(enabled = verticalScrollEnabled, state = rememberScrollState()),
        ) {
            if (contacts != null) {
                if (contacts.size == 1) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                    ) {
                        ThumbnailForContact(
                            contactId = contacts[0].contactId,
                            name = contacts[0].name,
                        )
                        Text(
                            text = contacts[0].name,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .basicMarquee()
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                    ) {
                        Box {
                            contacts.take(4).forEachIndexed { index, contact ->
                                ThumbnailForContact(
                                    contactId = contact.contactId,
                                    name = contact.name,
                                    modifier = Modifier
                                        .padding(start = (index * 20).dp)
                                        .zIndex(-index.toFloat()),
                                    useBorder = true
                                )
                            }
                        }

                        Text(
                            text = "${contacts[0].name} and ${contacts.size - 1} Others",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .basicMarquee()
                        )
                    }
                }
//                LazyRow(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    item {
//                        Spacer(modifier = Modifier.width((screenWidth * 0.45f).dp))
//                    }
//                    items(contacts) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 5.dp)
//                        ) {
//                            ThumbnailForContact(contactId = it.contactId, name = it.name)
//                            Text(
//                                text = it.name,
//                                style = MaterialTheme.typography.titleLarge,
//                                modifier = Modifier
//                                    .padding(top = 8.dp)
//                                    .basicMarquee()
//                            )
//                        }
//                    }
//                }
            }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
                    .basicMarquee(),
                text = state.audioDisplayName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Spacer(modifier = Modifier.weight(0.2f))
                LeftAndRightHandleManualEdit(onEvent, leftHandleTimeToShow, rightHandleTimeToShow)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.30f)
                        .horizontalScroll(scrollState)
                        .pointerInput(size, refreshDragValuesToDragGesture) {
                            detectDragGestures(
                                onDragStart = {
                                    verticalScrollEnabled = false
                                    activeTrimHandle = if (Rect(
                                            leftHandleBottomOffset,
                                            80f
                                        ).contains(it)
                                    ) TrimHandle.Left else if (Rect(
                                            rightHandleBottomOffset,
                                            80f
                                        ).contains(it)
                                    ) TrimHandle.Right else null
                                },
                                onDragEnd = {
                                    activeTrimHandle = null
                                    refreshDragValuesToDragGesture += 1
                                    verticalScrollEnabled = true
                                },
                                onDragCancel = {
                                    activeTrimHandle = null
                                    refreshDragValuesToDragGesture += 1
                                    verticalScrollEnabled = true
                                },
                                onDrag = { change, dragAmount ->
                                    if (activeTrimHandle != null) {
                                        onEvent(
                                            TrimRingtoneEvent.OnDragHandle(
                                                activeTrimHandle!!,
                                                change.position.x,
                                                size?.width
                                            )
                                        )
                                        change.consume()
                                    } else {
                                        coroutineScope.launch {
                                            scrollState.scrollBy(-dragAmount.x)
                                        }
                                        change.consume()
                                    }
                                }
                            )
                        }
                        .padding(horizontal = 16.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            shape = RoundedCornerShape(8)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AudioWaveform(
                        modifier = Modifier
                            .width(((screenWidth - 32) * state.waveFormWidthFactor).dp),
                        style = Fill,
                        waveformAlignment = WaveformAlignment.Center,
                        amplitudeType = AmplitudeType.Avg,
                        progressBrush = animatedGradientBrush(state, size),
                        waveformBrush = SolidColor(Color.LightGray),
                        spikeWidth = Dp(5F),
                        spikePadding = Dp(2F),
                        spikeRadius = Dp(10F),
                        progress = state.progress,
                        amplitudes = state.amplitudes.map { maxOf(1, it) },
                        onProgressChange = {
                            onProgressChange(it)
                        }
                    )

                    Canvas(
                        modifier = Modifier
                            .matchParentSize(),
                        onDraw = {
                            size = this.size
                            drawLine(
                                color = handleLineColor,
                                start = leftHandleTopOffset,
                                end = leftHandleBottomOffset,
                                strokeWidth = 8f
                            )
                            drawCircle(
                                color = handleLineColor,
                                radius = 25f,
                                leftHandleBottomOffset
                            )

                            drawLine(
                                color = handleLineColor,
                                start = rightHandleTopOffset,
                                end = rightHandleBottomOffset,
                                strokeWidth = 8f
                            )
                            drawCircle(
                                color = handleLineColor,
                                radius = 25f,
                                rightHandleBottomOffset
                            )
                        })
                }

                ZoomControls(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 10.dp, top = 16.dp),
                    onEvent = onEvent,
                    trimDuration = state.totalDuration?.times(state.trimRightHandlePosition - state.trimLeftHandlePosition)
                )

                PlayPauseButtons(
                    onEvent,
                    onPlayClicked, state, modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }



        if (state.showShareRingtoneDlg) {

            AlertDialog(onDismissRequest = { }, confirmButton = {
                TextButton(onClick = {
                    onEvent(TrimRingtoneEvent.SetShowShareRingtone(false))
                    onEvent(TrimRingtoneEvent.OnSetRingtone)
                }) {
                    Text(text = stringResource(id = R.string.set_ringtone))
                }
            }, dismissButton = {
                TextButton(onClick = { onEvent(TrimRingtoneEvent.SetShowShareRingtone(false)) }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }, text = {
                val composition by
                rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.share))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )

                Column {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.height(size?.height?.times(0.6)?.dp ?: 200.dp),
                    )

                    Column(modifier = Modifier.fillMaxWidth(0.90f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = state.shareRingtoneToPublic,
                                onCheckedChange = { onEvent(TrimRingtoneEvent.OnToggleShare(it)) })
                            Text(text = stringResource(R.string.share_ringtone_to_public))
                        }

                        Text(
                            text = stringResource(R.string.we_are_sharing_ringtone),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                        )
                    }
                }
            })
        }
    }
}

@Composable
fun animatedGradientBrush(
    uiState: TrimRingtoneState,
    size: Size?
): Brush {
//    val transition = rememberInfiniteTransition()
//
//    val animationFloat = transition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = InfiniteRepeatableSpec(
//            tween(1000)
//        )
//    )

    return Brush.linearGradient(
        uiState.trimLeftHandlePosition * 1.01f to Color.LightGray,
        uiState.trimLeftHandlePosition * 1.01f to Color(0xFFDA22FF),
        uiState.trimLeftHandlePosition + (uiState.trimRightHandlePosition - uiState.trimLeftHandlePosition).div(
            1.5f
        ) to Color(0xFF6200EA),
        1f to Color(0xFF00B8D4),
        start = Offset.Zero.copy(y = (size?.height ?: 0f) * 0.45f)
    )
}

@Composable
private fun PlayPauseButtons(
    onEvent: (TrimRingtoneEvent) -> Unit,
    onPlayClicked: () -> Unit,
    uiState: TrimRingtoneState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DefIconButton(
            onClick = {
                onEvent(TrimRingtoneEvent.SkipPrevious)
            }, modifier = Modifier.padding(end = 22.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(32.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20))
                .clickable {
                    onPlayClicked()
                }
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp),
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(32.dp)
            )
        }

        Spacer(modifier = Modifier.size(50.dp))
    }
}

@Composable
fun DefIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable {
                onClick()
            }
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        icon()
    }
}

