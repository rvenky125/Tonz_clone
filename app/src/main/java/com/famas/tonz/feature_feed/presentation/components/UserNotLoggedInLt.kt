package com.famas.tonz.feature_feed.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.famas.tonz.BuildConfig
import com.famas.tonz.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserNotLoggedInLt(
    onLogin: (String) -> Unit,
    onStartLogin: () -> Unit,
    onCancelLogin: (String) -> Unit,
    loading: Boolean = false,
) {
    val pagerState = rememberPagerState {
        3
    }

    val state = rememberOneTapSignInState()
    val coroutineScope = rememberCoroutineScope()
    var oldPagerJob: Job? = remember {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LaunchedEffect(key1 = pagerState.settledPage, block = {
            oldPagerJob?.cancel()
            delay(2500)
            oldPagerJob = coroutineScope.launch {
                pagerState.stopScroll()
                pagerState.animateScrollToPage(
                    when (pagerState.settledPage) {
                        0 -> 1
                        1 -> 2
                        2 -> 0
                        else -> 0
                    }
                )
            }
        })

        Spacer(modifier = Modifier.weight(1f))
        HorizontalPager(state = pagerState) {
            when (it) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ringtones_download),
                            contentDescription = "Download ringtones easily",
                            modifier = Modifier
                                .fillMaxHeight(0.6f)
                                .aspectRatio(1f)
                        )
                        Text(
                            text = "Download and play ringtones shared by others, trim them and set them as ringtones",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.getting_call),
                            contentDescription = "Enjoy the ringtones",
                            modifier = Modifier
                                .fillMaxHeight(0.6f)
                                .aspectRatio(1f)
                        )
                        Text(
                            text = "Enjoy the call from your loved ones with awesome ringtones",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.referral_and_earn),
                            contentDescription = "Refer and earn",
                            modifier = Modifier
                                .fillMaxHeight(0.6f)
                                .aspectRatio(1f)
                        )
                        Text(
                            text = "Refer your friends an earn Ad skip coins that provides you and Ad free app.",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Column {
            Text(
                text = "Login or create account on single click, no extra details needed",
                style = MaterialTheme.typography.labelLarge,
            )

            Button(
                onClick = {
                    if (!loading) {
                        onStartLogin()
                        state.open()
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(text = "Login or Register")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }


        OneTapSignInWithGoogle(
            state = state,
            clientId = remember { BuildConfig.GOOGLE_AUTH_CLIENT_ID },
            onTokenIdReceived = { tokenId ->
                onLogin(tokenId)
            },
            onDialogDismissed = { message ->
                onCancelLogin(message)
            }
        )
    }
}