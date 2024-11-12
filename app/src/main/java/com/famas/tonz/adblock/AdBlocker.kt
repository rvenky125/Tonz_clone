package com.famas.tonz.adblock

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.net.MalformedURLException

class AdBlocker(private val httpClient: HttpClient, private val context: Context) {
    private var addHosts = emptyList<String>()
    private val alreadyLoadedAdHosts = listOf<String>()
    val loading = mutableStateOf(false)

    init {
        loadFromAssets()
    }

    fun loadFromAssets() {
        CoroutineScope(Dispatchers.IO).launch {
            println("Load assets called")
            if (addHosts.isNotEmpty()) {
                return@launch
            }

            try {
                loading.value = true
                val hostsFile = File(context.cacheDir, "hosts.txt")

                if (hostsFile.exists()) {
                    addHosts = hostsFile.readLines()
                    println("Loaded from cache")
                    loading.value = false
                }
                if (hostsFile.lastModified() < (System.currentTimeMillis() - 1000 * 60 * 60 * 48)) {
                    val addHostsText =
                            httpClient.get("https://tonz.co.in/hosts")
                            .bodyAsText()
                    Log.d("myTag", "response from server: $addHostsText")
                    addHosts = addHostsText.lines()
                    hostsFile.writeText(addHostsText)
                }
                loading.value = false
            } catch (e: Exception) {
                loading.value = false
                e.printStackTrace()
            }
        }
    }

    fun isAd(url: String?): Boolean {
        return try {
            if (alreadyLoadedAdHosts.contains(url)) {
                return true
            }
            val isAdHost = isAdHost(UrlUtils.getHost(url))
            if (isAdHost) {
                alreadyLoadedAdHosts.plus(url)
            }
            isAdHost
        } catch (e: MalformedURLException) {
            Log.d("myTag", e.toString())
            false
        }
    }

    private fun isAdHost(host: String): Boolean {
        if (addHosts.isEmpty()) {
            loadFromAssets()
            return false
        }

        if (TextUtils.isEmpty(host)) {
            return false
        }

        val index = host.indexOf(".")
        val isAd = index >= 0 && (addHosts.contains("||$host^") ||
                index + 1 < host.length && isAdHost(host.substring(index + 1)))
        return isAd
    }

    fun createEmptyResource(): WebResourceResponse {
        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
    }

    fun isAdHostsEmpty(): Boolean {
        return addHosts.isEmpty()
    }
}