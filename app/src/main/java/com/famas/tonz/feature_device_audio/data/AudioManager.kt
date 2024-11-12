package com.famas.tonz.feature_device_audio.data

import android.util.Log
import com.famas.tonz.core.TAG
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import linc.com.amplituda.Amplituda
import linc.com.amplituda.Cache
import linc.com.amplituda.callback.AmplitudaErrorListener
import javax.inject.Inject

class AudioManager @Inject constructor(
    private val amplituda: Amplituda,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun getAmplitudes(path: String): List<Int> = withContext(ioDispatcher) {
        Log.d(TAG, "file path for amplitudes:$path")
        return@withContext amplituda.processAudio(path, Cache.withParams(Cache.REUSE))
            .get(AmplitudaErrorListener {
                it.printStackTrace()
            })
            .amplitudesAsList()
    }
}