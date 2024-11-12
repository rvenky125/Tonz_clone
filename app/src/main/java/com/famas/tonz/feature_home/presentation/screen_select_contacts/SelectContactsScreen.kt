package com.famas.tonz.feature_home.presentation.screen_select_contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.famas.tonz.R
import com.famas.tonz.core.TAG
import com.famas.tonz.core.components.CardWithAnimatedBorder
import com.famas.tonz.core.components.CircularTextField
import com.famas.tonz.core.ui.navigation.toDirection
import com.famas.tonz.core.ui.util.UiEvent
import com.famas.tonz.core.util.UiContact
import com.famas.tonz.feature_trim_set_ringtone.presentation.components.ThumbnailForContact
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Destination
@Composable
fun SelectContactsScreen(
    selectContactsVM: SelectContactsVM = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    selectedContactsResult: ResultBackNavigator<List<UiContact>>
) {
    val state = selectContactsVM.selectContactsScreen.value
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val allContactsSelected = state.selectedContacts.containsAll(
        state.contacts
    )

    val permissionsLauncher = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        )
    )
    val currentScreen = navController.currentDestinationAsState().value

    LaunchedEffect(key1 = currentScreen, block = {
        selectContactsVM.onContactsSelectionCompleted = {
            selectedContactsResult.navigateBack(it)
        }

        if (!state.isLoading) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
            delay(500)
            selectContactsVM.onEvent(SelectContactsEvent.ReloadContacts)
        }
    })

    LaunchedEffect(key1 = Unit, block = {
        permissionsLauncher.launchMultiplePermissionRequest()

        selectContactsVM.uiEventFlow.collectLatest {
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

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.select_contact))
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                    contentDescription = null
                )
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            selectContactsVM.onEvent(SelectContactsEvent.OnClickFloatingActionBtn)
        }) {
            Text(
                text = if (state.selectedContacts.isNotEmpty()) "Set Ringtone to ${state.selectedContacts.size} contacts" else "Set as default Ringtone",
                modifier = Modifier.padding(horizontal = 10.dp)
            )
        }
    }) { values ->
        Column(modifier = Modifier.padding(values)) {
            if (state.isLoading) {
                LinearProgressIndicator()
            }
            CircularTextField(value = state.searchValue, onValueChange = {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
                selectContactsVM.onEvent(SelectContactsEvent.OnSearchValueChange(it))
            }, leadingIcon = {
                Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
            }, placeholder = {
                Text(text = "Search")
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )

            if (permissionsLauncher.allPermissionsGranted) {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp),
                    state = lazyListState
                ) {
                    item {
                        if (state.searchValue.isBlank()) {
                            CardWithAnimatedBorder(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = R.drawable.default_ringtone_btn),
                                    contentDescription = stringResource(
                                        id = R.string.default_ringtone
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(95.dp)
                                        .clickable {
                                            selectedContactsResult.navigateBack(emptyList())
                                        },
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(items = state.contacts) { contact ->
                        SelectContactsContactItem(
                            contact = contact, state = state, onEvent = selectContactsVM::onEvent
                        )
                    }
                }
            }

            if (permissionsLauncher.shouldShowRationale && !permissionsLauncher.allPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Please grant all contacts permissions from to enjoy all features")
                    Button(onClick = { permissionsLauncher.launchMultiplePermissionRequest() }) {
                        Text(text = "Launch permissions")
                    }
                }
            }

            if (!permissionsLauncher.shouldShowRationale && !permissionsLauncher.allPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "You've denied the contacts access to us. We are sure, we don't store your contacts. They will be safe and secure.")
                    Text(
                        text = "Please provide contact permission from settings to continue with app",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Button(onClick = {
                        try {
                            val settingsIntent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                            settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(settingsIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(text = "Open App settings")
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SelectContactsContactItem(
    contact: UiContact,
    state: SelectContactsState,
    onEvent: (SelectContactsEvent) -> Unit,
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    LaunchedEffect(key1 = state.selectedContacts, block = {
        Log.d(TAG, "${state.selectedContacts.size}")
    })

    Card(onClick = {
        onEvent(SelectContactsEvent.OnClickContact(contact))
    }, colors = cardColors, modifier = Modifier.padding(bottom = 10.dp)) {
        Row(modifier = Modifier.padding(bottom = 10.dp, top = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ThumbnailForContact(
                    photoUri = contact.profilePicUri,
                    name = contact.name,
                    size = 45f,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name, style = MaterialTheme.typography.bodyLarge
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            contact.phoneNumbers.firstOrNull()?.let {
                                Text(
                                    text = "Mobile $it", style = MaterialTheme.typography.bodySmall
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
                Checkbox(checked = state.selectedContacts.contains(contact), onCheckedChange = {
                    onEvent(SelectContactsEvent.OnToggleContact(contact))
                })
            }
        }
    }
}