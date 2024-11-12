package com.famas.tonz.adblock

import java.net.MalformedURLException
import java.net.URL

object UrlUtils {
    @Throws(MalformedURLException::class)
    fun getHost(url: String?): String {
        return URL(url).host
    }
}