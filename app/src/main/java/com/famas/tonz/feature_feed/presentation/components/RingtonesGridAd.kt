package com.famas.tonz.feature_feed.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.famas.tonz.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RingtonesGridAd(
    ad: NativeAd?,
    modifier: Modifier = Modifier
) {
    if (ad == null) {
        Card(modifier = Modifier.padding(horizontal = 5.dp), onClick = {

        }) {
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Ad", modifier = Modifier.align(Alignment.Center))
            }
        }
        return
    }

//    val pagerState = rememberPagerState()

    var currentImageIndex by remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(key1 = currentImageIndex, block = {
        if (ad.images.size == 1) return@LaunchedEffect
        delay(1000)
        currentImageIndex = if (currentImageIndex < ad.images.size - 1) currentImageIndex + 1 else 0
    })

    AndroidView(factory = { NativeAdView(it) }, update = { nativeAdView ->
        val composeView = ComposeView(nativeAdView.context)
        nativeAdView.setNativeAd(ad)
        nativeAdView.removeAllViews()
        nativeAdView.addView(composeView)

        composeView.setContent {
            Card(
                modifier = Modifier.padding(horizontal = 5.dp),
                onClick = { nativeAdView.performClick() }) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ad.images[currentImageIndex].uri
                        ),
                        contentDescription = ad.headline,
                        modifier = Modifier
                            .height(180.dp)
                            .align(Alignment.TopCenter),
                        contentScale = ContentScale.Crop,
                    )

                    Column(
                        modifier = Modifier
                            .padding(top = 110.dp)
                            .background(
                                Brush.verticalGradient(
                                    0.01f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    0.5f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    1f to MaterialTheme.colorScheme.surface,
                                )
                            )
                            .fillMaxWidth()
                            .padding(10.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = ad.headline ?: "Ad",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = ad.body ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row {
                            Text(
                                text = "${ad.advertiser}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = ad.icon?.uri,
                                contentDescription = ad.advertiser,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(
                                        CircleShape
                                    )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = ad.starRating?.toString() ?: "",
                                    fontSize = 10.sp,
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = stringResource(
                                        R.string.like
                                    ),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
//                            Spacer(modifier = Modifier.width(5.dp))
//                            Icon(
//                                imageVector = Icons.Rounded.MoreVert,
//                                contentDescription = stringResource(
//                                    R.string.more_options
//                                ),
//                                modifier = Modifier
//                                    .padding(6.dp)
//                                    .size(18.dp)
//                                    .clip(CircleShape)
//                                    .clickable {
//                                    }
//
//                            )
                        }
                    }
                }
            }
        }
    })
}