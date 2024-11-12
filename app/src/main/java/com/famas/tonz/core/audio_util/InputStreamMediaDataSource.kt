package com.famas.tonz.core.audio_util

import android.media.MediaDataSource
import java.io.InputStream

class InputStreamMediaDataSource(
    private val inputStream: InputStream
): MediaDataSource() {

    override fun close() {
        inputStream.close()
    }

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        inputStream.skip(position)
        return inputStream.read(buffer, offset, size)
    }

    override fun getSize(): Long {
        return inputStream.available().toLong()
    }
}