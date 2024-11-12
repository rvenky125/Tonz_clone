package com.famas.tonz.core.audio_util

import android.net.Uri
import com.famas.tonz.core.model.LocalAudio
import kotlinx.coroutines.flow.MutableSharedFlow

interface AudioPlayer {
    val events: MutableSharedFlow<Event>

    fun setAudio(localAudio: LocalAudio): Boolean

    fun streamAudio(url: String): Boolean

    fun setAudio(uri: Uri): Boolean

    fun clearAudio(): Boolean

    fun play(): Boolean

    fun pause(): Boolean

    fun seekTo(position: Long): Boolean

    fun initialize()

    fun release()

    sealed interface Event {
        data class PositionChanged(val position: Long, val duration: Long) : Event
        data class PlayingChanged(val isPlaying: Boolean) : Event
        data class LoadingChanged(val isLoading: Boolean) : Event
    }
}