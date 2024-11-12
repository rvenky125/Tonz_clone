package com.famas.tonz.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import com.famas.tonz.NavGraphs
import com.famas.tonz.R
import com.famas.tonz.adblock.AdBlocker
import com.famas.tonz.core.ad_util.AdMobInterstitial
import com.famas.tonz.core.components.MainTopBar
import com.famas.tonz.core.data.preferences.Preferences
import com.famas.tonz.core.data.preferences.ReferralData
import com.famas.tonz.core.ui.navigation.AppBottomNav
import com.famas.tonz.core.ui.navigation.BottomBarDestination
import com.famas.tonz.core.ui.navigation.ContactsNavArg
import com.famas.tonz.core.ui.navigation.TrimRingtoneNavArgs
import com.famas.tonz.core.ui.theme.TonzAppTheme
import com.famas.tonz.core.ui.util.LocalActivity
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.ui.util.UiText
import com.famas.tonz.core.util.InAppReviewHelper
import com.famas.tonz.core.util.RingtoneApi
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.destinations.FeedScreenDestination
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.destinations.SelectContactsScreenDestination
import com.famas.tonz.destinations.TrimRingtoneScreenDestination
import com.famas.tonz.extensions.addWriteExternalStorage
import com.famas.tonz.extensions.getReadStoragePermission
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileWorker
import com.famas.tonz.feature_profile.presentation.screen_refer_earn.UserCoinsSheetContent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.getOr
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject


const val TAG = "myTag"

@AndroidEntryPoint
@SuppressLint("Range")
@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
class MainActivity : ComponentActivity() {
    private lateinit var mainViewModel: MainActivityVM
    private lateinit var navController: NavHostController

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var adBlocker: AdBlocker

    @Inject
    lateinit var inAppReviewHelper: InAppReviewHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        callUpdate()

        val vm: MainActivityVM by viewModels()
        mainViewModel = vm

        setContent {
            CompositionLocalProvider(LocalActivity provides this) {
                TonzAppTheme {
                    // A surface container using the 'background' color from the theme
                    val adsCoinsSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val coroutineScope = rememberCoroutineScope()

                    val adMobInterstitial = remember {
                        AdMobInterstitial(this, this) {
                            if (this::mainViewModel.isInitialized) {
                                coroutineScope.launch {
                                    Log.d(
                                        TAG,
                                        "${mainViewModel.currentAdsCount % 3} ${mainViewModel.userData.value?.adsCoins}"
                                    )

                                    if (mainViewModel.currentAdsCount % 3 == 0 && (mainViewModel.userData.value?.adsCoins
                                            ?: 0) < 10
                                    ) {
                                        adsCoinsSheet.expand()
                                    }

                                    mainViewModel.onEvent(MainActivityEvent.InterstitialAdClosed)
                                }
                            }
                        }
                    }
                    mainViewModel.interstitial = adMobInterstitial

                    val currentUser =
                        mainViewModel.userData.value
                    navController = rememberNavController()
//                    val bottomSheetNavigator = rememberBottomSheetNavigator()
//                    navController.navigatorProvider += bottomSheetNavigator


                    val snackbarHostState = remember {
                        SnackbarHostState()
                    }

                    val currentDestination =
                        navController.currentDestinationAsState().value ?: HomeScreenDestination

                    val systemUiController = rememberSystemUiController()
                    val isSystemInDarkThem = isSystemInDarkTheme()
                    val background = MaterialTheme.colorScheme.background

                    val workInfos = mainViewModel.workInfos.observeAsState()

                    val latestWorkInfoToShowPopUp =
                        workInfos.value?.find { it.id == mainViewModel.workIdToShowDialog }
                    val latestWorkInfoToShowLaterPopUp =
                        workInfos.value?.find { it.id == mainViewModel.workIdToShowDialogLaterDownload && it.state.isFinished }

                    val workInfo = latestWorkInfoToShowPopUp
                        ?: latestWorkInfoToShowLaterPopUp

                    val context = LocalContext.current


                    val launchSettingsPermissionIfNotProvided = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {}

                    val referralSheet = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true,
                        confirmValueChange = { false })

                    var oldCoinsCount: Int? = remember {
                        null
                    }
                    LaunchedEffect(key1 = currentUser, block = {
                        if (oldCoinsCount == null) {
                            oldCoinsCount = currentUser?.adsCoins
                            return@LaunchedEffect
                        }

                        if (oldCoinsCount!! < (currentUser?.adsCoins ?: 0)) {
                            coroutineScope.launch {
                                adsCoinsSheet.show()
                            }
                        }
                    })

                    LaunchedEffect(
                        key1 = mainViewModel.filePathToSaveAsRingtoneWithPendingPermission,
                        block = {
                            if (mainViewModel.filePathToSaveAsRingtoneWithPendingPermission == null) {
                                return@LaunchedEffect
                            }

                            if (!Settings.System.canWrite(context)) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                                intent.data = Uri.parse("package:" + context.packageName)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                launchSettingsPermissionIfNotProvided.launch(intent)
                            } else {
                                val file =
                                    Uri.parse(mainViewModel.filePathToSaveAsRingtoneWithPendingPermission)
                                        .toFile()

                                if (RingtoneApi(context).setDefaultRingtone(file)) {
                                    if ((mainViewModel.userData.value?.adsCoins ?: 0) < 10) {
                                        adMobInterstitial.showAd()
                                    } else mainViewModel.askForReview()

                                    mainViewModel.startFileUploadWorker(file)
                                    mainViewModel.filePathToSaveAsRingtoneWithPendingPermission =
                                        null

                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(
                                            UiText.StringResource(R.string.ringtone_set_successfully)
                                                .getString(context)
                                        )
                                    }
                                } else {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar("Failed to set ringtone")
                                    }
                                }
                            }
                        })


                    LaunchedEffect(key1 = Unit, block = {
                        mainViewModel.uiEventFlow.collectLatest {
                            when (it) {
                                is UiEvent.ShowSnackBar -> {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(it.uiText.getString(context))
                                    }
                                }

                                else -> {}
                            }
                        }
                    })

                    DisposableEffect(systemUiController, isSystemInDarkThem) {
                        // Update all of the system bar colors to be transparent, and use
                        // dark icons if we're in light theme
                        systemUiController.setStatusBarColor(
                            color = background,
                            darkIcons = !isSystemInDarkThem
                        )

                        // setStatusBarColor() and setNavigationBarColor() also exist

                        onDispose {}
                    }

                    LaunchedEffect(mainViewModel.selectedFileUriToSetRingtone) {
                        if (mainViewModel.selectedFileUriToSetRingtone != null) {
                            navController.navigate(SelectContactsScreenDestination)
                        }
                    }

                    val permissionsLauncher = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS,
                            getReadStoragePermission()
                        ).addWriteExternalStorage()
                    )

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Scaffold(bottomBar = {
                            if (BottomBarDestination.entries.map { it.direction.route }
                                    .contains(currentDestination.route)) {
                                AppBottomNav(
                                    navController = navController,
                                    currentRoute = currentDestination.route,
                                    allPermissionsGranted = permissionsLauncher.allPermissionsGranted
                                )
                            }
                        }, snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        }, topBar = {
                            if (BottomBarDestination.entries
                                    .any { it.direction.route != HomeScreenDestination.route && it.direction.route == currentDestination.route }
                            ) {
                                MainTopBar(
                                    navController,
//                                    onClickSurpriseBox = {
//                                        mainViewModel.onEvent(MainActivityEvent.OnToggleSurpriseBox)
//                                    },
                                    showWorkInfosBadge = workInfos.value?.any { !it.state.isFinished } == true)
                            }
                        }) { paddingValues ->
                            Column(modifier = Modifier.padding(paddingValues)) {
                                NavigationWithBottomSheet(
                                    snackbarHostState = snackbarHostState,
                                    mainViewModel = mainViewModel,
                                    navController = navController,
                                    adMobInterstitial = adMobInterstitial,
                                    adBlocker = adBlocker
                                )

                                if (workInfo != null) {
                                    WorkStatusDialog(workInfo, mainViewModel, navController)
                                }



                                if (adsCoinsSheet.isVisible) {
                                    ModalBottomSheet(
                                        onDismissRequest = {
                                            coroutineScope.launch {
                                                adsCoinsSheet.hide()
                                            }
                                        },
                                        sheetState = adsCoinsSheet
                                    ) {
                                        UserCoinsSheetContent(
                                            adsCoins = currentUser?.adsCoins,
                                            navController = navController,
                                            onClose = {
                                                coroutineScope.launch {
                                                    adsCoinsSheet.hide()
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(if (systemUiController.isNavigationBarVisible) 58.dp else 16.dp))
                                    }
                                }

                                if (currentUser?.isJustRegistered == true) {
                                    ReferralSheet(referralSheet, coroutineScope)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (intent.action == Intent.ACTION_SEND) {
            lifecycleScope.launch(Dispatchers.IO) {
                var fileUri = ""
                fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)?.toString()
                        ?: return@launch
                } else {
                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?)?.toString()
                        ?: return@launch
                }
                val fileToSave = File(cacheDir, "AUD_RING_" + UUID.randomUUID().toString() + ".mp3")
                contentResolver.openInputStream(fileUri.toUri())?.use { inp ->
                    fileToSave.outputStream().use { oup ->
                        inp.copyTo(oup)
                    }
                }

                mainViewModel.onEvent(
                    MainActivityEvent.OnSelectFileUriToSetRingtone(
                        fileToSave.toUri().toString()
                    )
                )
            }
        }

        if (intent.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            val ringtoneId = data?.getQueryParameter("id")
            lifecycleScope.launch {
                delay(400)
                if (!ringtoneId.isNullOrBlank()) {
                    navController.navigate(FeedScreenDestination(ringtoneId = ringtoneId))
                }
            }
        }

        if (intent.extras?.getString("id") != null) {
            val ringtoneId = intent.extras?.getString("id")
            lifecycleScope.launch {
                delay(400)
                if (!ringtoneId.isNullOrBlank()) {
                    navController.navigate(FeedScreenDestination(ringtoneId = ringtoneId))
                }
            }
        }

        lifecycleScope.launch {
            preferences.observeReferralData().filterNotNull().collectLatest {
                if (it.ringtoneId != null) {
                    navController.navigate(FeedScreenDestination(ringtoneId = it.ringtoneId))
                    preferences.setReferralData(
                        ReferralData(
                            referralCode = it.referralCode,
                            ringtoneId = null
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun ReferralSheet(
        referralSheet: SheetState,
        coroutineScope: CoroutineScope
    ) {
        LaunchedEffect(key1 = Unit, block = {
            preferences.getReferralData()?.let {
                it.referralCode?.let {
                    mainViewModel.referralCode = it
                }
            }
        })

        ModalBottomSheet(
            onDismissRequest = {},
            dragHandle = {},
            sheetState = referralSheet
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Enter the referral code here",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "You'll get 100 Ad Skip coins upon successful referral.",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = mainViewModel.referralCode,
                    onValueChange = {
                        mainViewModel.referralCode = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    singleLine = true,
                    placeholder = {
                        Text(text = "Referral Code")
                    }
                )
                Text(
                    text = mainViewModel.referralCodeErrMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            referralSheet.hide()
                        }
                        mainViewModel.onEvent(MainActivityEvent.OnDismissReferral)
                    }) {
                        Text(text = "Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        mainViewModel.onEvent(
                            MainActivityEvent.OnConfirm
                        )
                    }) {
                        if (mainViewModel.submittingReferralCode) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = "Confirm")
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    private fun callUpdate() {
        val updateManager = AppUpdateManagerFactory.create(this)
        updateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && updateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                try {
                    updateManager.startUpdateFlowForResult(
                        updateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        3
                    )
                } catch (e: Exception) {
                    e.message?.let { Log.d("myTag", it) }
                }
            }
        }
    }

    @Composable
    fun NavigationWithBottomSheet(
        snackbarHostState: SnackbarHostState,
        mainViewModel: MainActivityVM,
        navController: NavHostController,
        adMobInterstitial: AdMobInterstitial,
        adBlocker: AdBlocker
    ) {
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            dependenciesContainerBuilder = {
                dependency(snackbarHostState)
                dependency(mainViewModel)
                dependency(adMobInterstitial)
                dependency(adBlocker)

                resultRecipient<SelectContactsScreenDestination, List<UiContact>>().onNavResult(
                    listener = { result ->
                        val selectedUiContacts =
                            result.getOr { null }

                        /**Need to capture these values before making them reset. The below event will reset these**/
                        val selectedFileUriToSetRingtone =
                            mainViewModel.selectedFileUriToSetRingtone
                        val trimRingtone = mainViewModel.trimRingtone

                        mainViewModel.onEvent(
                            MainActivityEvent.OnSelectFileUriToSetRingtone(
                                null
                            )
                        )

                        if (trimRingtone) {
                            navController.navigate(
                                TrimRingtoneScreenDestination(
                                    TrimRingtoneNavArgs(
                                        contactsArg = ContactsNavArg(
                                            selectedUiContacts ?: return@onNavResult
                                        ),
                                        songUri = selectedFileUriToSetRingtone
                                    )
                                )
                            )
                            return@onNavResult
                        }

                        mainViewModel.onEvent(
                            MainActivityEvent.SetRingtoneToContacts(
                                selectedFileUriToSetRingtone,
                                selectedUiContacts
                            )
                        )
                    }
                )
            },
            navController = navController,
        )
    }

    @Composable
    fun WorkStatusDialog(
        workInfo: WorkInfo,
        mainViewModel: MainActivityVM,
        navController: NavHostController
    ) {
        val progress =
            workInfo.progress.getInt(
                DownloadAudioFileWorker.PROGRESS,
                0
            ).div(100f)

        if (!workInfo.state.isFinished) {
            Dialog(onDismissRequest = { }) {
                Card(modifier = Modifier) {
                    DownloadInProgressDialogContent(
                        progress = progress,
                        workInfo = workInfo,
                        mainViewModel = mainViewModel,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        } else if (!mainViewModel.showOnlyProgressOfWorker) {
            Dialog(onDismissRequest = {
                mainViewModel.onEvent(
                    MainActivityEvent.SetWorkIdToShowDialog(
                        null
                    )
                )
                mainViewModel.onEvent(
                    MainActivityEvent.SetWorkIdToShowLaterDialog(
                        null
                    )
                )
            }) {
                Card(modifier = Modifier) {
                    DownloadCompletedContent(
                        workInfo = workInfo,
                        mainViewModel = mainViewModel,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun DownloadCompletedContent(
        workInfo: WorkInfo,
        mainViewModel: MainActivityVM,
        modifier: Modifier = Modifier,
        lottieModifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            if (workInfo.outputData.getString(DownloadAudioFileWorker.FILE_URI) != null) {
                Text(
                    text = "Downloaded successfully",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${workInfo.outputData.getString(DownloadAudioFileWorker.FILE_NAME)}. Now, set it as your ringtone!",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(15)
                        )
                        .clip(RoundedCornerShape(15)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mainViewModel.onEvent(
                                    MainActivityEvent.OnSelectFileUriToSetRingtone(
                                        workInfo.outputData.getString(
                                            DownloadAudioFileWorker.FILE_URI
                                        )
                                    )
                                )
                                mainViewModel.onEvent(
                                    MainActivityEvent.SetWorkIdToShowDialog(
                                        null
                                    )
                                )
                                mainViewModel.onEvent(
                                    MainActivityEvent.SetWorkIdToShowLaterDialog(
                                        null
                                    )
                                )
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.trim_and_set_ringtone),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Divider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                mainViewModel.onEvent(
                                    MainActivityEvent.OnSelectFileUriToSetRingtone(
                                        workInfo.outputData.getString(
                                            DownloadAudioFileWorker.FILE_URI
                                        ),
                                        false
                                    )
                                )
                                mainViewModel.onEvent(
                                    MainActivityEvent.SetWorkIdToShowDialog(
                                        null
                                    )
                                )
                                mainViewModel.onEvent(
                                    MainActivityEvent.SetWorkIdToShowLaterDialog(
                                        null
                                    )
                                )
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.set_ringtone_directly),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "Downloaded failed",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Failed to download ${
                        workInfo.outputData.getString(
                            DownloadAudioFileWorker.FILE_NAME
                        )
                    }, please try again from other website",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 5.dp)
                )
                Button(onClick = {
                    mainViewModel.onEvent(
                        MainActivityEvent.SetWorkIdToShowDialog(
                            null
                        )
                    )
                    mainViewModel.onEvent(
                        MainActivityEvent.SetWorkIdToShowLaterDialog(
                            null
                        )
                    )
                }, modifier = Modifier.align(Alignment.End)) {
                    Text(text = "Ok")
                }
            }
        }
    }

    @Composable
    private fun DownloadInProgressDialogContent(
        progress: Float,
        workInfo: WorkInfo,
        mainViewModel: MainActivityVM,
        modifier: Modifier = Modifier,
        lottieModifier: Modifier = Modifier
    ) {
        Column(modifier = modifier) {
            Text(
                text = "Downloading",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${workInfo.progress.getString(DownloadAudioFileWorker.FILE_NAME)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp)
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            TextButton(
                onClick = {
                    mainViewModel.onEvent(
                        MainActivityEvent.SetWorkIdToShowLaterDialog(
                            mainViewModel.workIdToShowDialog
                        )
                    )

                    mainViewModel.onEvent(
                        MainActivityEvent.SetWorkIdToShowDialog(null)
                    )
                }, modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.End)
            ) {
                Text(text = "Download in background")
            }
        }
    }


    override fun onResume() {
        super.onResume()

        if (this::inAppReviewHelper.isInitialized) {
            inAppReviewHelper.initialize(this)
        }

        lifecycleScope.launch {
            if (mainViewModel.filePathToSaveAsRingtoneWithPendingPermission != null && Settings.System.canWrite(
                    applicationContext
                )
            ) {
                val filePath = mainViewModel.filePathToSaveAsRingtoneWithPendingPermission
                mainViewModel.filePathToSaveAsRingtoneWithPendingPermission = null
                delay(500)
                mainViewModel.filePathToSaveAsRingtoneWithPendingPermission = filePath
            }
        }

        if (this::mainViewModel.isInitialized) {
            mainViewModel.syncUserData()
        }
    }
}