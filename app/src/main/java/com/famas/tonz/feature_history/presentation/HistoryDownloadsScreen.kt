package com.famas.tonz.feature_history.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.famas.tonz.R
import com.famas.tonz.core.MainActivityEvent
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.feature_music.data.workers.DownloadAudioFileWorker
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun HistoryDownloadsScreen(
    mainActivityVM: MainActivityVM,
    navController: NavController
) {
    val workInfos = mainActivityVM.workInfos.observeAsState().value?.filter {
        !it.state.isFinished || it.outputData.getString(DownloadAudioFileWorker.FILE_URI) != null
    }?.sortedByDescending {
        it.outputData.getLong(DownloadAudioFileWorker.TIME_STAMP, System.currentTimeMillis())
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = stringResource(id = R.string.download_history))
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(imageVector = Icons.Rounded.NavigateBefore, contentDescription = null)
            }
        })
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it), contentAlignment = Alignment.Center
        ) {
            workInfos?.let { wkInfos ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    items(wkInfos) { workInfo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                if (!workInfo.state.isFinished) {
                                    val progress =
                                        workInfo.progress.getInt(
                                            DownloadAudioFileWorker.PROGRESS,
                                            0
                                        ).div(100f)

                                    Text(
                                        text = "Downloading",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${
                                            workInfo.progress.getString(
                                                DownloadAudioFileWorker.FILE_NAME
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .padding(top = 10.dp)
                                            .fillMaxWidth(),
                                        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Downloaded successfully",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${
                                            workInfo.outputData.getString(
                                                DownloadAudioFileWorker.FILE_NAME
                                            )
                                        }",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                    TextButton(onClick = {
                                        mainActivityVM.onEvent(
                                            MainActivityEvent.OnSelectFileUriToSetRingtone(
                                                workInfo.outputData.getString(
                                                    DownloadAudioFileWorker.FILE_URI
                                                )
                                            )
                                        )
                                    }, modifier = Modifier.align(Alignment.End)) {
                                        Text(text = "Set As Ringtone")
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: Text(
                text = "No download history found",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}