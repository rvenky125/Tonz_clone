package com.famas.tonz.feature_feed.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.famas.tonz.R
import com.famas.tonz.buildCoilDefaultImageLoader
import com.famas.tonz.feature_feed.domain.models.RingtonePostModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridItem(
    ringtonePostModel: RingtonePostModel,
    onLike: () -> Unit,
    onOptionsClick: () -> Unit,
    onClick: () -> Unit,
    onShare: () -> Unit
) {
    Card(modifier = Modifier.padding(horizontal = 5.dp), onClick = onClick) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    model = ringtonePostModel.thumbnailPictureUrl,
                    imageLoader = buildCoilDefaultImageLoader()
                ),
                contentDescription = ringtonePostModel.albumName,
                modifier = Modifier
                    .matchParentSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(top = 110.dp)
                    .background(
                        Brush.verticalGradient(
                            0.01f to MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            0.5f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            1f to MaterialTheme.colorScheme.surface,
                        )
                    )
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = ringtonePostModel.ringtoneName ?: "",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = ringtonePostModel.albumName?.trim() ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row {
                    Text(
                        text = "Uploaded by: ${ringtonePostModel.uploadedByName}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ringtonePostModel.likeCount,
                            fontSize = 10.sp,
                        )
                        Icon(
                            imageVector = if (ringtonePostModel.isPresentUserLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = stringResource(
                                R.string.like
                            ),
                            modifier = Modifier
                                .padding(6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onLike()
                                },
                            tint = animateColorAsState(
                                if (ringtonePostModel.isPresentUserLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                                tween(), label = "present_user_like_color_anim"
                            ).value,
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(
                            R.string.share_ringtone_to_other_app
                        ),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                onShare()
                            }
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(
                            R.string.more_options
                        ),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                onOptionsClick()
                            }

                    )
                }
            }
        }
    }
}