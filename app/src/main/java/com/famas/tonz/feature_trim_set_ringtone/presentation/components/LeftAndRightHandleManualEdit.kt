package com.famas.tonz.feature_trim_set_ringtone.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.famas.tonz.R
import com.famas.tonz.feature_trim_set_ringtone.presentation.HandleEvent
import com.famas.tonz.feature_trim_set_ringtone.presentation.TrimRingtoneEvent

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LeftAndRightHandleManualEdit(
    onEvent: (TrimRingtoneEvent) -> Unit,
    leftHandleTimeToShow: Float,
    rightHandleTimeToShow: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(TrimRingtoneEvent.OnLeftHandleEvent(HandleEvent.Decrease)) }) {
                Icon(
                    imageVector = Icons.Rounded.RemoveCircleOutline,
                    contentDescription = stringResource(
                        id = R.string.decrease_start
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedContent(
                targetState = leftHandleTimeToShow,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically(tween(durationMillis = 800)) { -it } with slideOutVertically(
                            tween(durationMillis = 800)
                        ) { it }
                    } else {
                        slideInVertically(tween(durationMillis = 800)) { it } with slideOutVertically(
                            tween(durationMillis = 800)
                        ) { -it }
                    }
                }, label = "left_handle_progress_text"
            ) {
                Text(
                    String.format(
                        "%.2f",
                        it
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(onClick = { onEvent(TrimRingtoneEvent.OnLeftHandleEvent(HandleEvent.Increase)) }) {
                Icon(
                    imageVector = Icons.Rounded.AddCircleOutline,
                    contentDescription = stringResource(
                        id = R.string.increase_start
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant

                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(TrimRingtoneEvent.OnRightHandleEvent(HandleEvent.Decrease)) }) {
                Icon(
                    imageVector = Icons.Rounded.RemoveCircleOutline,
                    contentDescription = stringResource(
                        id = R.string.decrease_start
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant

                )
            }
            AnimatedContent(
                targetState = rightHandleTimeToShow,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically(tween(durationMillis = 800)) { -it } with slideOutVertically(
                            tween(durationMillis = 800)
                        ) { it }
                    } else {
                        slideInVertically(tween(durationMillis = 800)) { it } with slideOutVertically(
                            tween(durationMillis = 800)
                        ) { -it }
                    }
                },
                label = "right_handle_progress_text"
            ) {
                Text(
                    String.format(
                        "%.2f", it
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            IconButton(onClick = { onEvent(TrimRingtoneEvent.OnRightHandleEvent(HandleEvent.Increase)) }) {
                Icon(
                    imageVector = Icons.Rounded.AddCircleOutline,
                    contentDescription = stringResource(
                        id = R.string.increase_start
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant

                )
            }
        }
    }
}