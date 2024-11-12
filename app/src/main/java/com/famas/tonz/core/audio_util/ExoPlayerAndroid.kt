package com.famas.tonz.core.audio_util

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.famas.tonz.core.TAG
import com.famas.tonz.core.model.LocalAudio
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

@UnstableApi
class ExoPlayerAndroid(
    private val context: Context,
) : AudioPlayer, Player.Listener {
    private var mediaPlayer: ExoPlayer? = null
    private var ended: Boolean = false

    override val events: MutableSharedFlow<AudioPlayer.Event> = MutableSharedFlow()

    companion object {
        private const val PLAYER_POSITION_UPDATE_TIME = 300L
    }

    private var lastEmittedPosition = 0L
    private var isPlaying = false
    private var handler: Handler? = null

    private val playerPositionRunnable = object : Runnable {
        override fun run() {
            try {
                val playbackPosition = mediaPlayer?.currentPosition ?: 0L
                val playing = mediaPlayer?.isPlaying

                if (playing != isPlaying && playing != null) {
                    sendEvent(AudioPlayer.Event.PlayingChanged(playing))
                    isPlaying = playing
                    if (playing) {
                        ended = false
                    }
                }

                if (playbackPosition != lastEmittedPosition) {
                    sendEvent(
                        AudioPlayer.Event.PositionChanged(
                            playbackPosition, duration = mediaPlayer?.duration
                                ?: 0L
                        )
                    )
                    lastEmittedPosition = playbackPosition
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                handler?.postDelayed(this, PLAYER_POSITION_UPDATE_TIME)
            }
        }
    }


    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)

        if (error != null) {
            Toast.makeText(context, "The file is no longer available to play, please try setting new ringtone", Toast.LENGTH_LONG).show()
        }
    }

    override fun setAudio(localAudio: LocalAudio): Boolean {
        clearAudio()
        return try {
            mediaPlayer?.setMediaItem(MediaItem.fromUri(localAudio.uri))
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Oops!, The file is no longer available to play, please try other one", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            false
        }
    }

    override fun setAudio(uri: Uri): Boolean {
        clearAudio()
        return try {
            mediaPlayer?.setMediaItem(MediaItem.fromUri(uri))
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Oops!, The file is no longer available to play, please try other one", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            false
        }
    }

    override fun streamAudio(url: String): Boolean {
        clearAudio()
        return try {
            val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(url.toUri()))
            mediaPlayer?.setMediaSource(mediaSource)
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to load the audio, please check your internet connection.", Toast.LENGTH_LONG).show()
            false
        }
    }

    override fun clearAudio(): Boolean {
        return try {
            handler?.removeCallbacks(playerPositionRunnable)
            mediaPlayer?.stop()
            mediaPlayer?.clearMediaItems()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun play(): Boolean {
        return try {
            if (ended) {
                mediaPlayer?.seekTo(0L)
            }
            mediaPlayer?.playWhenReady = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun pause(): Boolean {
        return try {
            mediaPlayer?.pause()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun seekTo(position: Long): Boolean {
        return try {
            mediaPlayer?.seekTo(position)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun initialize() {
        mediaPlayer = ExoPlayer.Builder(context).build()
        mediaPlayer?.addListener(this)
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        sendEvent(AudioPlayer.Event.LoadingChanged(isLoading))
    }

    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                handler?.postDelayed(playerPositionRunnable, PLAYER_POSITION_UPDATE_TIME)
            }

            Player.STATE_BUFFERING -> {
            }

            Player.STATE_ENDED -> {
                Log.d(TAG, "STATE_ENDED")
                ended = true
            }

            Player.STATE_IDLE -> {
                Log.d(TAG, "STATE_IDLE")
            }
        }
    }

    init {
        handler = Handler(Looper.getMainLooper())
    }

    override fun release() {
        handler?.removeCallbacks(playerPositionRunnable)
        handler = null
        mediaPlayer?.release()
    }

    private fun sendEvent(event: AudioPlayer.Event) {
        runBlocking { events.emit(event) }
    }
}