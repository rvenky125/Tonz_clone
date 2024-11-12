package com.famas.tonz.feature_device_audio.data

import android.content.Context
import androidx.core.net.toUri
import com.famas.tonz.core.model.LocalAudio
import com.famas.tonz.core.util.getRealPathFromURI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AudioRepository @Inject constructor(
    private val localMediaDataSource: LocalMediaDataSource,
    private val audioManager: AudioManager,
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) {

    suspend fun loadAudioFiles(query: String): List<LocalAudio> = withContext(ioDispatcher) {
        return@withContext localMediaDataSource.loadAudioFiles(query)
    }

    suspend fun loadAudioByContentId(id: String): LocalAudio? = withContext(ioDispatcher) {
        return@withContext localMediaDataSource.loadAudioById(id)
    }

    suspend fun loadAudioAmplitudes(
        filePath: String
    ): List<Int> = withContext(ioDispatcher) {
        return@withContext audioManager.getAmplitudes(filePath)
    }

    suspend fun loadAudioByUri(uri: String): LocalAudio? = withContext(ioDispatcher) {
        return@withContext try {
            val file = File(uri)
            LocalAudio(
                id = null,
                uri = uri.toUri(),
                path = getRealPathFromURI(context, uri.toUri()) ?: file.path,
                name = file.name,
                duration = 0L,
                size = file.length(),
                timestamp = 0,
            )
        } catch (e: Exception) {
            null
        }
    }
}