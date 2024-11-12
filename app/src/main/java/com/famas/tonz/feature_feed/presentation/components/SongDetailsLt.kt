package com.famas.tonz.feature_feed.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.famas.tonz.R
import com.famas.tonz.buildCoilDefaultImageLoader
import com.famas.tonz.core.ad_util.AdmobBanner
import com.famas.tonz.feature_feed.domain.models.RingtonePostModel
import com.famas.tonz.feature_trim_set_ringtone.presentation.DefIconButton

@Composable
fun SongDetailsLt(
    ringtonePostModel: RingtonePostModel,
    modifier: Modifier = Modifier,
    onLike: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15))
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 20.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15))
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ringtonePostModel.thumbnailPictureUrl,
                            buildCoilDefaultImageLoader()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )

                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(
                        topStartPercent = 25,
                        bottomEndPercent = 0,
                        topEndPercent = 0,
                        bottomStartPercent = 0
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = ringtonePostModel.likeCount)
                        IconButton(
                            onClick = { onLike() },
                        ) {
                            Icon(
                                imageVector = if (ringtonePostModel.isPresentUserLiked) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (ringtonePostModel.isPresentUserLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(28.dp)
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = ringtonePostModel.ringtoneName,
                modifier = Modifier
                    .fillMaxWidth(),
                maxLines = 2,
                style = MaterialTheme.typography.headlineSmall
            )
            ringtonePostModel.albumName?.let {
                Text(
                    text = "$it ${ringtonePostModel.artist}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun SongDetailModalFooterContent(
    isPlaying: Boolean,
    isPlayerLoading: Boolean,
    showAds: Boolean,
    progress: Float,
    onTogglePlay: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onClickSetRingtone: () -> Unit,
    onShare: () -> Unit,
    skipPrevious: (() -> Unit)? = null,
    skipNext: (() -> Unit)? = null
) {
//    val animatedProgress = animateFloatAsState(targetValue = progress, label = stringResource(id = R.string.progress_anim), animationSpec = tween(300))
    Column {
        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
//            LinearProgressIndicator(
//                { animatedProgress.value }, modifier = Modifier
//                    .fillMaxWidth()
//                    .background(MaterialTheme.colorScheme.background)
//                    .pointerInput(Unit) {
//                        val screenWidth = size.width
//                        detectTapGestures {
//                            onProgressChange(it.x / screenWidth)
//                        }
//                    }
//            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    skipPrevious?.let {
                        DefIconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = stringResource(
                                    R.string.previous
                                ),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20))
                            .clickable {
                                onTogglePlay()
                            }
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        if (isPlayerLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 24.dp)
                                    .size(32.dp),
                            )
                        } else Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 24.dp)
                                .size(32.dp),
                        )
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    skipNext?.let {
                        DefIconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = stringResource(
                                    R.string.next
                                ),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        onShare()
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        onClickSetRingtone()
                    }) {
                        Text(text = stringResource(id = R.string.set_as_ringtone))
                    }
                }

                if (showAds) {
                    AdmobBanner(
                        modifier = Modifier,
                        width = LocalConfiguration.current.screenWidthDp
                    )
                }
            }
        }
    }
}