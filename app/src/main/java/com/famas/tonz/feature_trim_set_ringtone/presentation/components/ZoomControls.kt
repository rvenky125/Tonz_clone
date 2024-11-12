package com.famas.tonz.feature_trim_set_ringtone.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.famas.tonz.R
import com.famas.tonz.feature_trim_set_ringtone.presentation.TrimRingtoneEvent

@Composable
fun ZoomControls(
    modifier: Modifier = Modifier,
    onEvent: (TrimRingtoneEvent) -> Unit,
    trimDuration: Float?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (trimDuration != null) "${String.format("%.2f", (trimDuration / 1000f))} sec" else "",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                onEvent(TrimRingtoneEvent.OnZoomOut)
            }) {
                Icon(
                    painter = painterResource(R.drawable.zoom_out),
                    contentDescription = stringResource(id = R.string.zoom_in),
                    modifier = Modifier.size(25.dp)
                )
            }
            IconButton(onClick = {
                onEvent(TrimRingtoneEvent.OnZoomIn)
            }, modifier = Modifier.padding(end = 8.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.zoom_in),
                    contentDescription = stringResource(id = R.string.zoom_in),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}