package com.famas.tonz.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector
import com.famas.tonz.R
import com.famas.tonz.destinations.Destination
import com.famas.tonz.destinations.FeedScreenDestination
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.destinations.MusicScreenDestination
import com.famas.tonz.destinations.TypedDestination

enum class BottomBarDestination(
    val direction: TypedDestination<*>,
    val icon: (Boolean) -> ImageVector,
    @StringRes val label: Int
) {
    Home(
        direction = HomeScreenDestination,
        { if (it) Icons.Filled.Home else Icons.Outlined.Home },
        R.string.home_screen
    ),
    Feed(
        direction = FeedScreenDestination,
        { if (it) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic },
        R.string.ringtones
    ),
    Music(
        direction = MusicScreenDestination,
        { if (it) Icons.Filled.TravelExplore else Icons.Outlined.TravelExplore },
        R.string.music_screen
    ),
//    Profile(
//        direction = ProfileScreenDestination,
//        { if (it) Icons.Filled.Person else Icons.Outlined.Person },
//        R.string.profile
//    )
}