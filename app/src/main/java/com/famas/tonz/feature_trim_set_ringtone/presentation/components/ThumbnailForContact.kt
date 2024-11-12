package com.famas.tonz.feature_trim_set_ringtone.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.famas.tonz.core.util.rememberBitmapForContact
import com.famas.tonz.core.util.toOneOrTwoLetters

@Composable
fun ThumbnailForContact(
    modifier: Modifier = Modifier,
    size: Float = 80F,
    contactId: Long,
    name: String,
    useBorder: Boolean = false
) {
    val profileBitmap = rememberBitmapForContact(contactId = contactId)

    Box(modifier = modifier) {
        profileBitmap?.asImageBitmap()?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier
                    .size(Dp(size))
                    .border(if (useBorder) 0.5.dp else 0.dp, color = MaterialTheme.colorScheme.onBackground, CircleShape)
                    .clip(
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier
                .size(Dp(size))
                .border(if (useBorder) 0.5.dp else 0.dp, color = MaterialTheme.colorScheme.onBackground, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.toOneOrTwoLetters(),
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = size.div(2).sp
                )
            )
        }
    }
}

@Composable
fun ThumbnailForContact(
    modifier: Modifier = Modifier,
    size: Float = 80F,
    photoUri: String?,
    name: String,
    selected: Boolean = false
) {
    Box(modifier = modifier) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(Dp(size))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Done, contentDescription = "Toggle Select", tint = MaterialTheme.colorScheme.onPrimary)
            }
        } else {
            photoUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(Dp(size))
                        .clip(
                            CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(Dp(size))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.toOneOrTwoLetters(),
                    color = MaterialTheme.colorScheme.onTertiary,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = size.div(2).sp
                    )
                )
            }
        }
    }
}