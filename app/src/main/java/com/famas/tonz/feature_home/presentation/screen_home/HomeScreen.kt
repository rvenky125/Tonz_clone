package com.famas.tonz.feature_home.presentation.screen_home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.famas.tonz.BuildConfig
import com.famas.tonz.R
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.core.TAG
import com.famas.tonz.core.components.CircularTextField
import com.famas.tonz.core.components.DownloadBadgedBoxButton
import com.famas.tonz.core.ui.navigation.AudioListScreenNavArgs
import com.famas.tonz.core.ui.navigation.toDirection
import com.famas.tonz.core.ui.theme.md_theme_dark_primary
import com.famas.tonz.core.ui.theme.md_theme_light_primary
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.destinations.AudioListScreenDestination
import com.famas.tonz.destinations.HistoryDownloadsScreenDestination
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.destinations.ReferAndEarnScreenDestination
import com.famas.tonz.extensions.addWriteExternalStorage
import com.famas.tonz.extensions.getReadStoragePermission
import com.famas.tonz.feature_feed.presentation.components.OneTapSignInState
import com.famas.tonz.feature_feed.presentation.components.OneTapSignInWithGoogle
import com.famas.tonz.feature_feed.presentation.components.rememberOneTapSignInState
import com.famas.tonz.feature_trim_set_ringtone.presentation.components.ThumbnailForContact
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.GoogleAuthProvider
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
@OptIn(
    ExperimentalPermissionsApi::class
)
@RootNavGraph(start = true)
@Destination()
@Composable
fun HomeScreen(
    homeScreenVM: HomeScreenVM = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    mainVM: MainActivityVM,
    navController: NavController,
) {
    val state = homeScreenVM.homeScreenState.value
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val animationProgress = remember {
        Animatable(1f)
    }
    var heightOfDefRingtoneLt by remember {
        mutableFloatStateOf(100f)
    }
    val systemUiController = rememberSystemUiController()
    val oneTapSignInState = rememberOneTapSignInState()
    val currentUser = mainVM.userData.value

    //Colors
    val primary = md_theme_light_primary
    val background = MaterialTheme.colorScheme.background
    val onBackground = MaterialTheme.colorScheme.onBackground

    val launchSettingsPermissionIfNotProvided = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        homeScreenVM.initializeDefaultRingtone(context)
    }

    LaunchedEffect(key1 = Unit) {
        homeScreenVM.launchSystemSettingsCallback = {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchSettingsPermissionIfNotProvided.launch(intent)
        }
    }

    val firstItemTranslationY by remember {
        derivedStateOf {
            when {
                lazyListState.layoutInfo.visibleItemsInfo.isNotEmpty() && lazyListState.firstVisibleItemIndex == 0 -> lazyListState.firstVisibleItemScrollOffset.toFloat()
                else -> heightOfDefRingtoneLt
            }
        }
    }
    val animatableContentColor =
        animateColorAsState(
            targetValue = if (animationProgress.value == 0f) onBackground else Color.White,
            label = "content_color_animation"
        )

    LaunchedEffect(key1 = state.searchValue, block = {
        if (state.searchValue.isNotBlank() && animationProgress.value != 0f) {
            animationProgress.animateTo(0f)
        } else if (state.searchValue.isBlank() && animationProgress.value != 1f) {
            animationProgress.animateTo(1f)
        }
    })

    val permissionsLauncher = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            getReadStoragePermission()
        ).addWriteExternalStorage()
    )

    var systemUiColorJob: Job? = remember { null }
    LaunchedEffect(key1 = firstItemTranslationY, mainVM.isContactsLoaded.value, block = {
        val progress = 1f - (firstItemTranslationY / heightOfDefRingtoneLt).coerceIn(0f, 1f)

        coroutineScope.launch {
            animationProgress.stop()
            animationProgress.animateTo(
                progress,
                tween()
            )
        }

        systemUiColorJob?.cancel()
        systemUiColorJob = coroutineScope.launch {
            delay(10)
            systemUiController.setStatusBarColor(
                if (progress == 0f) background else primary.copy(
                    alpha = progress
                ),
                progress == 0f
            )
        }
    })

    val notificationPermissions =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    val currentScreen = navController.currentDestinationAsState().value

    val isSystemInDark = isSystemInDarkTheme()
    LaunchedEffect(key1 = currentScreen, key2 = permissionsLauncher.allPermissionsGranted, block = {
        if (permissionsLauncher.allPermissionsGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissions.launchPermissionRequest()
            }
        }

        if (currentScreen?.route != HomeScreenDestination.route) {
            if (currentScreen?.route != ReferAndEarnScreenDestination.route) {
                systemUiController.setStatusBarColor(background, darkIcons = !isSystemInDark)
            }
            homeScreenVM.onEvent(HomeScreenEvent.ClearAudio)
        } else if (!state.isLoading) {
            state.contacts.firstOrNull { it.contactId == state.expandedCardId }?.let {
                homeScreenVM.onEvent(
                    HomeScreenEvent.ToggleClickContactCard(
                        it
                    )
                )
            }
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
                delay(100)
                systemUiController.setStatusBarColor(primary, darkIcons = false)
            }
            delay(500)
            homeScreenVM.onEvent(HomeScreenEvent.ReloadContacts(context))
        }
    })

    LaunchedEffect(key1 = Unit, block = {
        homeScreenVM.contactsLoadedCallback = {
            mainVM.isContactsLoaded.value = true
        }
        if (permissionsLauncher.permissions[0].status != PermissionStatus.Granted) {
            homeScreenVM.contactsLoadedCallback?.invoke()
        }
        permissionsLauncher.launchMultiplePermissionRequest()

        homeScreenVM.uiEventFlow.collectLatest {
            when (it) {
                is UiEvent.ShowSnackBar -> {
                    CoroutineScope(Dispatchers.IO).launch {
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

//    val infiniteTransition = rememberInfiniteTransition(label = "border_color_transition")
//    val borderColor by infiniteTransition.animateColor(
//        initialValue = Color.White,
//        targetValue = primary,
//        animationSpec = infiniteRepeatable(
//            animation = tween(1000),
//            repeatMode = RepeatMode.Reverse
//        ), label = "border_color_anim"
//    )

    Column {
        if (permissionsLauncher.allPermissionsGranted) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.bg_music),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((with(LocalDensity.current) { heightOfDefRingtoneLt.toDp() }) * 3)
                        .graphicsLayer {
                            alpha = animationProgress.value
                        },
                    contentScale = ContentScale.FillBounds
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Tonz",
                            style = MaterialTheme.typography.headlineSmall,
                            color = animatableContentColor.value,
                            fontWeight = FontWeight.SemiBold
                        )
                        MainTopBarActionButtons(
                            navController = navController,
                            mainVM = mainVM,
                            animatableContentColor = animatableContentColor,
                            homeScreenVM = homeScreenVM,
                            state = state,
                            context = context,
                            isUserLoggedIn = currentUser != null,
                            oneTapSignInState = oneTapSignInState
                        )
                    }

                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Fetching contacts..",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp),
                            color = Color.White
                        )
                    }
                    CircularTextField(
                        value = state.searchValue,
                        onValueChange = {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                            homeScreenVM.onEvent(HomeScreenEvent.OnSearchValueChange(it))
                        },
                        leadingIcon = {
                            if (state.searchValue.isNotBlank()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures {
                                            homeScreenVM.onEvent(
                                                HomeScreenEvent.OnSearchValueChange(
                                                    ""
                                                )
                                            )
                                        }
                                    })
                            } else {
                                Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                            }
                        },
                        placeholder = {
                            Text(text = "Search")
                        },
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 5.dp)
                            .fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp * animationProgress.value)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .onGloballyPositioned {
                                    heightOfDefRingtoneLt = it.size.height.toFloat()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Default Ringtone",
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 14.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            shape = RoundedCornerShape(50),
                                            color = Color.White
//                                            color = borderColor
                                        )
                                        .pointerInput(Unit) {
                                            detectTapGestures {
                                                navController.navigate(
                                                    AudioListScreenDestination(
                                                        AudioListScreenNavArgs(null)
                                                    )
                                                )
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "Edit Ringtone",
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = md_theme_dark_primary
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    homeScreenVM.onEvent(HomeScreenEvent.TogglePlayDefault)
                                }) {
                                    if (state.loadingDefault) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp,
                                            color = md_theme_dark_primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (state.playingDefault && state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                            contentDescription = stringResource(id = R.string.play_audio),
                                            tint = md_theme_dark_primary,
                                            modifier = Modifier.size(32.dp),
                                        )
                                    }
                                }
                                Slider(
                                    value = if (state.playingDefault) state.progress else 0f,
                                    onValueChange = {
                                        if (state.playingDefault) {
                                            homeScreenVM.onEvent(
                                                HomeScreenEvent.OnProgressChange(
                                                    it
                                                )
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = SliderDefaults.colors(
                                        inactiveTrackColor = primary,
                                        activeTrackColor = md_theme_dark_primary,
                                        thumbColor = md_theme_dark_primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Column {
                            Spacer(modifier = Modifier.height((with(LocalDensity.current) { heightOfDefRingtoneLt.toDp() } + 22.dp) * animationProgress.value))

                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                state = lazyListState,
                                modifier = Modifier
                                    .fillMaxSize()
//                                .padding(top = (with(LocalDensity.current) { heightOfDefRingtoneLt.toDp() } + 22.dp) * animationProgress.value)
                                    .shadow(
                                        (8 * animationProgress.value).dp,
                                        shape = RoundedCornerShape(
                                            topStart = (35 * animationProgress.value).dp,
                                            topEnd = (35 * animationProgress.value).dp
                                        )
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = (35 * animationProgress.value).dp,
                                            topEnd = (35 * animationProgress.value).dp
                                        )
                                    )
                                    .background(MaterialTheme.colorScheme.background),
                            ) {
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }

                                items(state.contacts) { contact ->
                                    HomeScreenContactItem(
                                        contact = contact,
                                        state = state,
                                        navController = navController,
                                        onEvent = homeScreenVM::onEvent
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(250.dp))
                                }
                            }
                        }

                        if (state.selectedContacts.isNotEmpty()) {
                            Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                                Spacer(modifier = Modifier.weight(1f))
                                FloatingActionButton(onClick = {
                                    homeScreenVM.onEvent(
                                        HomeScreenEvent.DiscardContactSelection
                                    )
                                }, modifier = Modifier.padding(end = 10.dp)) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Discard selected contacts",
                                    )
                                }
                                FloatingActionButton(
                                    onClick = {
                                        navController.navigate(
                                            AudioListScreenDestination(
                                                AudioListScreenNavArgs(state.selectedContacts)
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .padding(bottom = 16.dp, end = 16.dp)
                                ) {
                                    Text(
                                        text = "Set Ringtone to ${state.selectedContacts.size} contacts",
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!permissionsLauncher.allPermissionsGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (permissionsLauncher.permissions[0].status != PermissionStatus.Granted || permissionsLauncher.permissions[1].status != PermissionStatus.Granted) {
                    Image(
                        painter = painterResource(id = R.drawable.security_animate),
                        contentDescription = "Security contacts",
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .aspectRatio(1f)
                    )
                    Text(
                        text = "Tonz needs contacts permission",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "We promise you that we don't store your contacts and we adhere to all security concerns. Please grant contacts permissions.",
                        textAlign = TextAlign.Center
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.no_files),
                        contentDescription = "Security contacts",
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .aspectRatio(1f)
                    )
                    Text(
                        text = "Hey, Tonz needs storage permission to set ringtones to contacts. Please grant it and enjoy the features.",
                        textAlign = TextAlign.Center
                    )
                }

                if (permissionsLauncher.shouldShowRationale) {
                    Button(
                        onClick = { permissionsLauncher.launchMultiplePermissionRequest() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "Launch permissions")
                    }
                } else {
                    Button(onClick = {
                        val settingsIntent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                        settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(settingsIntent)
                    }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(text = "Open App settings")
                    }
                }
            }
        }

        OneTapSignInWithGoogle(
            state = oneTapSignInState,
            clientId = remember { BuildConfig.GOOGLE_AUTH_CLIENT_ID },
            onTokenIdReceived = { tokenId ->
                val credentials = GoogleAuthProvider.getCredential(tokenId, null)
                homeScreenVM.signInWithGoogle(credentials)
            },
            onDialogDismissed = { message ->
                Log.d("LOG", message)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )

        if (state.loading) {
            Dialog(onDismissRequest = { /*TODO*/ }) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainTopBarActionButtons(
    navController: NavController,
    mainVM: MainActivityVM,
    animatableContentColor: State<Color>,
    homeScreenVM: HomeScreenVM,
    state: HomeScreenState,
    context: Context,
    isUserLoggedIn: Boolean,
    oneTapSignInState: OneTapSignInState
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DownloadBadgedBoxButton(
            { navController.navigate(HistoryDownloadsScreenDestination) },
            mainVM.workInfos.observeAsState().value?.any { !it.state.isFinished } == true,
            animatableContentColor.value
        )

        Column {
            IconButton(onClick = {
                homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert)
            }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = animatableContentColor.value
                )
            }
            DropdownMenu(
                expanded = state.isMenuVisible,
                onDismissRequest = { homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert) },
                modifier = Modifier.clip(RoundedCornerShape(20))
            ) {
                AnimatedVisibility(
                    visible = state.isMenuVisible,
                    enter = expandVertically(tween(delayMillis = 0, durationMillis = 1000)),
                    exit = shrinkVertically(),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Column {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.refresh)) },
                            onClick = {
                                homeScreenVM.onEvent(HomeScreenEvent.ReloadContacts(context))
                                homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = stringResource(R.string.refresh)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.refer_and_earn)) },
                            onClick = {
                                navController.navigate(ReferAndEarnScreenDestination)
                                homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = stringResource(R.string.refer_and_earn)
                                )
                            }
                        )

                        if (!isUserLoggedIn) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.login_or_register)) },
                                onClick = {
                                    oneTapSignInState.open()
                                    homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = stringResource(R.string.login_or_register)
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.contacts_us)) },
                            onClick = {

                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://tonz.co.in")
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                                    contentDescription = stringResource(R.string.contacts_us)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.rate_app)) },
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = stringResource(R.string.rate_app)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.privacy_policy)) },
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://tonz.co.in/privacy_policy.html")
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = stringResource(R.string.rate_app)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomSlider(
    modifier: Modifier = Modifier,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    trackUnfilledColor: Color = MaterialTheme.colorScheme.primaryContainer,
    trackFilledColor: Color = MaterialTheme.colorScheme.primary,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 15f,
    thumbRadius: Float = strokeWidth * 2f,
    noOfWaves: Int = 8
) {
    var isDragging by remember {
        mutableStateOf(false)
    }
    var size by remember {
        mutableStateOf(Size.Zero)
    }
    val height = remember(size) { size.height }
    val width = remember(size) { size.width }
    val pointsSize = remember { noOfWaves * 2 }
    val stepWidth = remember(width) { width / pointsSize }
    val points = remember(size) {
        List(pointsSize) {
            Offset(
                stepWidth * it,
                if (it == 0) height / 2 else if (it % 2 == 0) 0f else height
            )
        }
    }
    val thumbOffset =
        remember(size, progress, thumbRadius) {
            Offset(
                x = (progress * width) + thumbRadius / 2,
                y = height / 2
            )
        }

    var reloadDrag by remember {
        mutableStateOf(false)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height((strokeWidth * 0.8f).dp)
            .then(modifier)
            .pointerInput(reloadDrag) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (Rect(thumbOffset, 70f).contains(it)) {
                            isDragging = true
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        reloadDrag = !reloadDrag
                    },
                    onDragEnd = {
                        isDragging = false
                        reloadDrag = !reloadDrag
                    },
                    onHorizontalDrag = { change, amount ->
                        Log.d(TAG, "$amount $isDragging")
                        if (isDragging) {
                            val progressToChange = (progress + (amount / width)).coerceIn(0f, 1f)
                            onProgressChange(
                                progressToChange
                            )
                        }
                    }
                )
            },
        onDraw = {
            size = this.size
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y / 2)
            }
            points.forEachIndexed { index, offset ->
                if (index < points.size - 1) {
                    val nextPoint = points[index + 1]
                    path.quadraticBezierTo(
                        offset.x,
                        offset.y,
                        (offset.x + nextPoint.x) / 2,
                        (offset.y + nextPoint.y) / 2
                    )
                } else {
                    path.quadraticBezierTo(
                        offset.x,
                        offset.y,
                        width,
                        height / 2
                    )
                }
            }

            clipRect(left = 0f, top = 0f, right = width * progress) {
                drawPath(path = path, color = trackFilledColor, style = Stroke(width = strokeWidth))
            }
            drawLine(
                color = trackUnfilledColor,
                start = Offset(x = progress * width, y = height / 2),
                strokeWidth = strokeWidth,
                end = Offset(x = width, y = height / 2)
            )
            drawCircle(
                color = trackFilledColor,
                radius = thumbRadius,
                center = thumbOffset
            )
        }
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeScreenContactItem(
    contact: UiContact,
    state: HomeScreenState,
    navController: NavController,
    onEvent: (HomeScreenEvent) -> Unit,
) {
    val expanded = contact.contactId == state.expandedCardId
    val cardColors = CardDefaults.cardColors(
        containerColor = if (expanded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    Card(
        colors = cardColors,
        modifier = Modifier
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(10))
            .combinedClickable(onLongClick = {
                onEvent(HomeScreenEvent.OnToggleSelectContact(contact))
            }, onClick = {
                onEvent(HomeScreenEvent.ToggleClickContactCard(contact))
            })
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThumbnailForContact(
                    photoUri = contact.profilePicUri,
                    name = contact.name,
                    size = 45f,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                onEvent(HomeScreenEvent.OnToggleSelectContact(contact))
                            }
                        },
                    selected = state.selectedContacts.contains(contact)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    AnimatedVisibility(
                        expanded,
                        modifier = Modifier.padding(top = 5.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                contact.phoneNumbers.firstOrNull()?.let {
                                    Text(
                                        text = "Mobile $it",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "(${contact.phoneNumbers.size} more)",
                                    modifier = Modifier
                                        .padding(start = 3.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                    }
                }

                IconButton(onClick = {
                    navController.navigate(
                        AudioListScreenDestination(
                            AudioListScreenNavArgs(contacts = listOf(contact))
                        )
                    )
                }, modifier = Modifier.align(Alignment.Top)) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(id = R.string.add_or_edit_ringtone)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Column {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (contact.currentRingtoneUri != null) {
                        Column {
                            Text(
                                text = "Ringtone",
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    bottom = 5.dp,
                                    start = 16.dp
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            MusicProgressSliderWithPlayBtn(
                                isPlaying = if (!state.playingDefault) state.isPlaying else false,
                                progress = if (!state.playingDefault) state.progress else 0f,
                                onTogglePlay = { onEvent(HomeScreenEvent.TogglePlay) },
                                onProgressChange = {
                                    if (!state.playingDefault) {
                                        onEvent(
                                            HomeScreenEvent.OnProgressChange(it)
                                        )
                                    }
                                })
                        }
                    } else {
                        Text(
                            text = "No ringtone set to this contact",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusicProgressSliderWithPlayBtn(
    isPlaying: Boolean,
    progress: Float,
    isPlayerLoading: Boolean = false,
    onTogglePlay: () -> Unit,
    onProgressChange: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            onTogglePlay()
        }) {
            if (isPlayerLoading) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(id = R.string.play_audio)
                )
            }
        }
        Slider(
            value = progress,
            onValueChange = {
                onProgressChange(it)
            },
            colors = SliderDefaults.colors(
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.5f
                )
            )
        )
    }
}