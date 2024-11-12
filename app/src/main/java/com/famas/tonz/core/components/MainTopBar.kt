package com.famas.tonz.core.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.famas.tonz.R
import com.famas.tonz.destinations.HistoryDownloadsScreenDestination
import com.famas.tonz.destinations.ReferAndEarnScreenDestination
import com.ramcosta.composedestinations.navigation.navigate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    navController: NavController,
    showWorkInfosBadge: Boolean,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
//    onClickSurpriseBox: () -> Unit
) {
    var showVert by rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    TopAppBar(title = {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
    }, actions = {
//        IconButton(onClick = {
//            onClickSurpriseBox()
//        }) {
//            BadgedBox(badge = {
//                if (showWorkInfosBadge) { //TODO
//                    Badge()
//                }
//            }) {
//                Icon(
//                    painter = painterResource(id = R.drawable.surprise_box),
//                    contentDescription = null,
//                    modifier = Modifier.size(22.dp)
//                )
//            }
//        }

        DownloadBadgedBoxButton(
            { navController.navigate(HistoryDownloadsScreenDestination) },
            showWorkInfosBadge,
        )

        Column {
            IconButton(onClick = {
                showVert = !showVert
            }) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = showVert,
                onDismissRequest = { showVert = false },
                modifier = Modifier.clip(RoundedCornerShape(20))
            ) {
                AnimatedVisibility(
                    visible = showVert,
                    enter = expandVertically(tween(delayMillis = 0, durationMillis = 1000)),
                    exit = shrinkVertically(),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Column {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.refer_and_earn)) },
                            onClick = {
                                navController.navigate(ReferAndEarnScreenDestination)
                                showVert = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = stringResource(R.string.refer_and_earn)
                                )
                            }
                        )

//                        if (!isUserLoggedIn) {
//                            DropdownMenuItem(
//                                text = { Text(text = stringResource(R.string.login_or_register)) },
//                                onClick = {
//                                    oneTapSignInState.open()
//                                    homeScreenVM.onEvent(HomeScreenEvent.ToggleMoreVert)
//                                },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.AutoMirrored.Filled.Login,
//                                        contentDescription = stringResource(R.string.login_or_register)
//                                    )
//                                }
//                            )
//                        }

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.contacts_us)) },
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://tonz.co.in")
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ContactSupport,
                                    contentDescription = stringResource(R.string.contacts_us)
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.rate_app)) },
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                        )
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        context,
                                        "Something went wrong",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = stringResource(R.string.rate_app)
                                )
                            }
                        )
                    }
                }
            }
        }
    }, modifier = Modifier.background(backgroundColor))
}

@Composable
fun DownloadBadgedBoxButton(
    onClick: () -> Unit,
    showWorkInfosBadge: Boolean,
    color: Color? = null
) {
    Box(modifier = Modifier
        .padding(end = 10.dp)
        .clickable {
            onClick()
        }
        .padding(5.dp)
    ) {
        BadgedBox(badge = {
            if (showWorkInfosBadge) {
                Badge()
            }
        }) {
            Icon(
                imageVector = Icons.Rounded.Downloading,
                contentDescription = null,
                tint = color ?: LocalContentColor.current
            )
        }
    }
}
