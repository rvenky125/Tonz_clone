package com.famas.tonz.feature_profile.presentation.screen_refer_earn

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.famas.tonz.R
import com.famas.tonz.core.MainActivityVM
import com.famas.tonz.core.ui.theme.ManropeFontFamily
import com.famas.tonz.core.ui.theme.md_theme_light_primary
import com.famas.tonz.core.util.toDp
import com.famas.tonz.destinations.ReferAndEarnScreenDestination
import com.famas.tonz.feature_feed.presentation.components.UserNotLoggedInLt
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ReferAndEarnScreen(
    referEarnVM: ReferEarnVM = hiltViewModel(),
    mainVm: MainActivityVM,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    val state = referEarnVM.state

    val systemUiController = rememberSystemUiController()

    val currentUser = mainVm.userData.value

    val density = LocalDensity.current
    val clipBoardManger = LocalClipboardManager.current

    val faqSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coinsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var screenSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val context = LocalContext.current
    val background = MaterialTheme.colorScheme.background
    val isSystemInDark = isSystemInDarkTheme()

    DisposableEffect(key1 = Unit, effect = {
        systemUiController.setStatusBarColor(md_theme_light_primary, darkIcons = false)

        onDispose {
            systemUiController.setStatusBarColor(background, darkIcons = !isSystemInDark)
        }
    })

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    coroutineScope.launch {
                        faqSheetState.show()
                    }
                }) {
                    Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(text = "FAQ?")
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    screenSize = it.size
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_music),
                    contentDescription = stringResource(
                        id = R.string.app_name
                    ),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .align(Alignment.End)
                            .padding(end = 16.dp)
                            .clickable {
                                coroutineScope.launch {
                                    coinsSheetState.show()
                                }
                            },
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ads_coin),
                            contentDescription = stringResource(
                                id = R.string.ads_coin
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${currentUser?.adsCoins ?: 0} ADS",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = ManropeFontFamily,
                                color = Color.White,
                            ),
                            modifier = Modifier.padding(start = 5.dp, bottom = 2.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .defaultMinSize(screenSize.width.dp, screenSize.height.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(
                            modifier = Modifier.height(height = (screenSize.height * 0.1f).toDp())
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Refer your friends and Earn",
                                style = TextStyle(
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ManropeFontFamily
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .align(Alignment.CenterHorizontally),
                                textAlign = TextAlign.Center,
                                color = Color.White,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Image(
                                painter = painterResource(id = R.drawable.giftbox),
                                contentDescription = stringResource(
                                    R.string.gift
                                ),
                                modifier = Modifier
                                    .height(height = (screenSize.height * 0.20f).toDp())
                                    .aspectRatio(1f)
                                    .padding(top = 10.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 12.dp),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ads_coin),
                                    contentDescription = stringResource(
                                        id = R.string.ads_coin
                                    ),
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "100",
                                    style = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = ManropeFontFamily,
                                        color = Color.White,
                                        textDecoration = TextDecoration.LineThrough
                                    ),
                                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                )
                                Text(
                                    text = "300",
                                    style = TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = ManropeFontFamily,
                                        color = Color.White,
                                    ),
                                    modifier = Modifier.padding(start = 5.dp, bottom = 5.dp)
                                )
                            }
                            Text(text = "Ad Skip Coins", color = Color.White)

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your friend gets 100 Ad Skip coins and you'll get 300 Ad Skip coins after successful signup",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(0.8f),
                                fontSize = 12.sp,
                                lineHeight = 13.sp
                            )

                            Spacer(modifier = Modifier.height(22.dp))
                            Row(
                                modifier = Modifier
                                    .drawBehind {
                                        drawRoundRect(
                                            style = Stroke(
                                                3f, pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(20f, 10f), 0f
                                                ),
                                                join = StrokeJoin.Round
                                            ), color = Color.White,
                                            cornerRadius = CornerRadius(25f, 25f)
                                        )
                                    }
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0x804B91FE),
                                                Color(0x99304FFE)
                                            )
                                        ), shape = RoundedCornerShape(20)
                                    )
                                    .padding(5.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(end = 5.dp)
                                ) {
                                    Text(
                                        text = "Your referral code",
                                        modifier = Modifier,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp
                                    )
                                    Text(
                                        text = currentUser?.referralCode ?: "N/A",
                                        color = Color.White,
                                        style = TextStyle(
                                            lineHeight = 22.sp,
                                            fontFamily = ManropeFontFamily,
                                            fontSize = 22.sp,
                                            letterSpacing = 3.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .width(1.dp)
                                        .height(35.dp)
                                        .background(Color.White)
                                )
                                Box(modifier = Modifier
                                    .clickable {
                                        clipBoardManger.setText(buildAnnotatedString {
                                            append(currentUser?.referralCode)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Copied code: ${currentUser?.referralCode}"
                                                        ?: "Error occurred"
                                                )
                                            }
                                        })
                                    }
                                    .padding(horizontal = 10.dp)
                                    .clip(RoundedCornerShape(20))
                                ) {
                                    Text(
                                        text = "Copy\nCode",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(
                                    text = "Share Your Referral Code Now",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                )
                                IconButton(onClick = {
                                    val intent = Intent().apply {
                                        type = "text/plain"
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Hey! 😃 Join me on Tonz, a cool Ringtones app. Use my code ${currentUser?.referralCode} to get 100 bonus coins. Download Tonz here: https://play.google.com/store/apps/details?id=${context.packageName}&referrer=${currentUser?.referralCode}. Check it out at [tonz.co.in]. Let's enjoy ringtones together and earn rewards! 🎵💰"
                                        )
                                    }
                                    val shareIntent = Intent.createChooser(
                                        intent,
                                        "Share redeem code through"
                                    )
                                    context.startActivity(shareIntent)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = stringResource(
                                            id = R.string.share_referral_code
                                        ),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(screenSize.height.times(0.3f).toDp()))
                    }
                }
            }
        }

        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                    .pointerInput(Unit) {},
                contentAlignment = Alignment.Center
            ) {
                UserNotLoggedInLt(
                    { referEarnVM.onEvent(ReferEarnEvent.OnLogin(it)) },
                    onStartLogin = { referEarnVM.onEvent(ReferEarnEvent.SetLoginLoading(true)) },
                    loading = state.loginLoading,
                    onCancelLogin = {
                        referEarnVM.onEvent(ReferEarnEvent.SetLoginLoading(false))
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    },
                )
            }
        }
    }

    if (coinsSheetState.isVisible && currentUser != null) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    coinsSheetState.hide()
                }
            },
            sheetState = coinsSheetState
        ) {
            UserCoinsSheetContent(adsCoins = currentUser.adsCoins)
            Spacer(modifier = Modifier.height(if (systemUiController.isNavigationBarVisible) 58.dp else 16.dp))
        }
    }


    if (faqSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    faqSheetState.hide()
                }
            },
            sheetState = faqSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Frequently asked questions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = "FAQ",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                FaqItem(
                    head = "How it works?",
                    content = "When you join Tonz, you get a unique 6-character referral code. Share it with friends via \"Refer and Earn.\" Both you and your friend earn coins - 300 for you, 100 for them. Ad Skip Coins automatically Skip Ads daily by deducting 10 coins each day."
                )
                Spacer(modifier = Modifier.height(10.dp))
                FaqItem(
                    head = "How can I use the Ad Skip Coins?",
                    content = "Tonz won't show you ads if you've Ad Skip coins. Tonz will automatically debit 10 coins every day for not showing ads. Check your balance in the app, If it's low, refer your friend now. Enjoy Tonz without Ad interruptions."
                )
                Spacer(modifier = Modifier.height(if (systemUiController.isNavigationBarVisible) 58.dp else 10.dp))
            }
        }
    }

}


@Composable
fun UserCoinsSheetContent(
    adsCoins: Int?,
    navController: NavController? = null,
    onClose: () -> Unit = {}
) {
    if (adsCoins == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.no_ads),
                contentDescription = "No ads",
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f),
            )

            Text(
                text = "Don't want to see Ads? Login now and get 100 coins in bonus to skip ads for 10 days. Refer your friends and earn more",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)
            )

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onClose() }, modifier = Modifier.padding(end = 16.dp)) {
                    Text(text = "Cancel")
                }
                Button(onClick = {
                    onClose()
                    navController?.navigate(ReferAndEarnScreenDestination)
                }) {
                    Text(text = "Refer Now")
                }
            }
        }

    } else {
        val coinComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.shimmer_ads_coin))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = coinComposition,
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .aspectRatio(1f)
                        .padding(top = 10.dp),
                    iterations = LottieConstants.IterateForever
                )
//            Image(
//                painter = painterResource(id = R.drawable.ads_coin),
//                contentDescription = stringResource(
//                    R.string.ads_coin
//                ),
//                modifier = Modifier
//                    .fillMaxWidth(0.4f)
//                    .aspectRatio(1f)
//                    .padding(top = 10.dp)
//            )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (adsCoins < 10) "Ads are showing because you've $adsCoins ads coins." else "You've $adsCoins ADS Coins!",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = ManropeFontFamily,
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(top = 10.dp),
                    textAlign = TextAlign.Center,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ads_coin),
                        contentDescription = stringResource(
                            R.string.ads_coin
                        ),
                        modifier = Modifier
                            .width(24.dp)
                            .aspectRatio(1f)
                    )
                    Text(text = "10 = No Ads for 1 day")
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (adsCoins < 10 && navController?.currentDestinationAsState()?.value != ReferAndEarnScreenDestination) {
                    Button(onClick = {
                        navController?.navigate(ReferAndEarnScreenDestination)
                    }) {
                        Text(text = "Refer and Earn coins")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ADS coins are Ad Skip coins. Tonz will automatically deduct 10 coins per day from your account for not showing Ads",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 12.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}


@Composable
fun FaqItem(head: String, content: String, modifier: Modifier = Modifier) {
    var isContentVisible by remember {
        mutableStateOf(false)
    }

    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isContentVisible = !isContentVisible },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = head, style = MaterialTheme.typography.labelLarge)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand Faq",
                modifier = Modifier.graphicsLayer(
                    rotationZ = animateFloatAsState(
                        targetValue = if (isContentVisible) 180f else 0f,
                        label = "faq_expand_animation"
                    ).value
                )
            )
        }
        AnimatedVisibility(visible = isContentVisible) {
            Text(text = content, style = MaterialTheme.typography.labelMedium)
        }
    }
}