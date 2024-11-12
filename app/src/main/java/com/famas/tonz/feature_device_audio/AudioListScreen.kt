package com.famas.tonz.feature_device_audio

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.famas.tonz.R
import com.famas.tonz.buildCoilDefaultImageLoader
import com.famas.tonz.core.model.PermissionsState
import com.famas.tonz.core.ui.navigation.AudioListScreenNavArgs
import com.famas.tonz.core.ui.navigation.ContactsNavArg
import com.famas.tonz.core.ui.navigation.TrimRingtoneNavArgs
import com.famas.tonz.destinations.TrimRingtoneScreenDestination
import com.famas.tonz.extensions.getReadStoragePermission
import com.famas.tonz.feature_device_audio.model.AudioListState
import com.famas.tonz.feature_device_audio.model.LocalAudioItemState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate


@Composable
@Destination
fun AudioListScreen(
    viewModel: AudioListViewModel = hiltViewModel(),
    navController: NavController,
    navArgs: AudioListScreenNavArgs,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::updatePermissionsState
    )
    val uiState = viewModel.audioListState

    LaunchedEffect(Unit) {
        launcher.launch(getReadStoragePermission())
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = uiState.isLoadingAudios),
        onRefresh = { viewModel.loadAudioFiles() }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (uiState.permissionsState) {
                PermissionsState.Unknown ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                PermissionsState.Granted -> {
                    if (uiState.audioFiles.isEmpty() && !uiState.isLoadingAudios) {
                        Text(
                            text = "No audio file found on your device. Check your file manager once.",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        AudioList(uiState = uiState, onQueryChange = viewModel::updateSearchQuery) {
                            navController.navigate(
                                TrimRingtoneScreenDestination(
                                    TrimRingtoneNavArgs(
                                        contactsArg = ContactsNavArg(navArgs.contacts ?: listOf()),
                                        contentId = it
                                    )
                                )
                            )
                        }
                    }
                }

                PermissionsState.Denied ->
                    PermissionsRationale(onGrantClick = {
                        launcher.launch(getReadStoragePermission())
                    })
            }
            AnimatedVisibility(visible = uiState.isLoadingAudios && uiState.audioFiles.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun PermissionsRationale(
    modifier: Modifier = Modifier,
    onGrantClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(0.8F),
            text = stringResource(id = R.string.read_permissions_rationale),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGrantClick) {
            Text(text = stringResource(id = R.string.grant_permissions))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AudioList(
    modifier: Modifier = Modifier,
    uiState: AudioListState,
    onQueryChange: (String) -> Unit,
    onAudioSelected: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize()
    ) {
        stickyHeader {
            SearchField(value = uiState.searchQuery, onValueChange = onQueryChange)
        }
        items(items = uiState.audioFiles, key = { it.id }) {
            AudioItem(itemState = it, onAudioSelected = { onAudioSelected(it.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = stringResource(id = R.string.search)) },
        leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AudioItem(
    modifier: Modifier = Modifier,
    itemState: LocalAudioItemState,
    onAudioSelected: () -> Unit
) {
    val image = rememberAudioCoverImage(filePath = itemState.path)

    Surface(onClick = onAudioSelected, shape = RoundedCornerShape(10)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .then(modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shadowElevation = 5.dp,
                    shape = RoundedCornerShape(10),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    image?.let {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = it,
                                buildCoilDefaultImageLoader()
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .height(50.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(
                        modifier = Modifier
                            .padding(10.dp)
                            .height(30.dp)
                            .aspectRatio(1f),
//                            .padding(20.dp),
                        imageVector = Icons.Rounded.Audiotrack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = itemState.displayName,
                    )
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${itemState.size} | ${itemState.extension}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Divider()
        }
    }
}