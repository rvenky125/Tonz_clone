package com.famas.tonz.feature_device_audio.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import com.famas.tonz.core.model.LocalAudio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


class LocalMediaDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {

    private val contentResolver: ContentResolver get() = context.contentResolver
    private val audioProjection: Array<String>
        get() = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.DATE_ADDED,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.AudioColumns.SIZE,
        )

    suspend fun loadAudioById(id: String): LocalAudio? = withContext(ioDispatcher) {
        val mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media._ID + "=?"
        val selectionArgs = arrayOf(id)
        val cursor =
            contentResolver.query(mediaContentUri, audioProjection, selection, selectionArgs, null)
        return@withContext cursor?.use {
            if (it.moveToFirst()) it.toLocalAudio() else null
        }
    }

    suspend fun loadAudioFiles(query: String): List<LocalAudio> = withContext(ioDispatcher) {
        val contentUri = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

            else -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
        val content = mutableListOf<LocalAudio?>()
        val cursor = contentResolver.query(
            contentUri,
            audioProjection,
            MediaStore.Audio.Media.DISPLAY_NAME + " LIKE '%$query%'",
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                content.add(it.toLocalAudio())
            }
        }
        return@withContext content.filterNotNull()
    }

    private fun Cursor.toLocalAudio(): LocalAudio? {
        try {
            val contentId = getColumnIndex(MediaStore.Audio.AudioColumns._ID).let(::getLong)
            val uri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentId)
            val data = getColumnIndex(MediaStore.Audio.Media.DATA).let(::getString)
            val date = getColumnIndex(MediaStore.Audio.Media.DATE_ADDED).let(::getLong)

            if (!File(data).exists()) {
                return null
            }

            val duration = getColumnIndex(MediaStore.Audio.AudioColumns.DURATION).let(::getLong)
            val size = getColumnIndex(MediaStore.Audio.AudioColumns.SIZE).let(::getLong)
            return LocalAudio(
                id = contentId.toString(),
                path = data,
                name = getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME).let(::getString),
                duration = if (duration > 4000) duration else return null,
                size = if (size > 100) size else return null,
                uri = uri,
                timestamp = date,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}