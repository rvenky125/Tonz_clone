package com.famas.tonz.core.ui.navigation

import com.famas.tonz.destinations.AudioListScreenDestination
import com.famas.tonz.destinations.HomeScreenDestination
import com.famas.tonz.destinations.MusicScreenDestination
import com.famas.tonz.destinations.TrimRingtoneScreenDestination
import com.ramcosta.composedestinations.spec.Direction

sealed class Screen(val navArgs: UiEventNavArgs? = null) {
    data object Home : Screen()
    data object Music : Screen()
    class TrimRingtone(navArgs: TrimRingtoneNavArgs) : Screen(navArgs)
    class SelectAudio(navArgs: AudioListScreenNavArgs) : Screen()
}

fun Screen.toDirection(): Direction {
    return when (this) {
        Screen.Home -> HomeScreenDestination
        Screen.Music -> MusicScreenDestination()
        is Screen.TrimRingtone -> TrimRingtoneScreenDestination(navArgs as TrimRingtoneNavArgs)
        is Screen.SelectAudio -> AudioListScreenDestination(navArgs as AudioListScreenNavArgs)
        else -> HomeScreenDestination
    }
}