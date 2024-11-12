package com.famas.tonz.core.audio_util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.famas.tonz.core.model.LocalAudio
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking

class AndroidAudioPlayer(
    private val context: Context
) : AudioPlayer, MediaPlayer.OnPreparedListener {
    private var mediaPlayer: MediaPlayer? = null

    override val events: MutableSharedFlow<AudioPlayer.Event> = MutableSharedFlow()

    companion object {
        private const val PLAYER_POSITION_UPDATE_TIME = 400L
    }

    private var lastEmittedPosition = 0
    private var isPlaying = false
    private var handler: Handler? = null
    private val playerPositionRunnable = object : Runnable {
        override fun run() {
            try {
                val playbackPosition = mediaPlayer?.currentPosition ?: 0
                val playing = mediaPlayer?.isPlaying

                if (playing != isPlaying && playing != null) {
                    sendEvent(AudioPlayer.Event.PlayingChanged(playing))
                    isPlaying = playing
                }

                if (playbackPosition != lastEmittedPosition) {
                    sendEvent(
                        AudioPlayer.Event.PositionChanged(
                            playbackPosition.toLong(),
                            duration = mediaPlayer?.duration?.toLong() ?: -1
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

    override fun setAudio(localAudio: LocalAudio): Boolean {
        clearAudio()
        return try {
            mediaPlayer?.setDataSource(context, localAudio.uri)
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun setAudio(uri: Uri): Boolean {
        clearAudio()
        return try {
            mediaPlayer?.setDataSource(context, uri)
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun streamAudio(url: String): Boolean {
        return try {
            clearAudio()
            mediaPlayer?.setDataSource(context, url.toUri())
            mediaPlayer?.prepare()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun clearAudio(): Boolean {
        return try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun play(): Boolean {
        return try {
            mediaPlayer?.start()
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
            mediaPlayer?.seekTo(position.toInt())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun initialize() {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setScreenOnWhilePlaying(true)
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(playerPositionRunnable, PLAYER_POSITION_UPDATE_TIME)
    }

    override fun release() {
        handler?.removeCallbacks(playerPositionRunnable)
        handler = null
        mediaPlayer?.release()
    }

    override fun onPrepared(mp: MediaPlayer?) {

    }

    private fun sendEvent(event: AudioPlayer.Event) {
        runBlocking { events.emit(event) }
    }
}