package com.famas.tonz.feature_music.presentation

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.famas.tonz.adblock.AdBlocker
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewNavigator
import com.google.accompanist.web.WebViewState


@Composable
fun WebViewScreen(
    webViewState: WebViewState,
    webViewNavigator: WebViewNavigator,
    onEvent: (MusicScreenEvent) -> Unit,
//    downloadHint: String?,
    enableJavascript: Boolean = false,
    showAds: Boolean,
    adBlocker: AdBlocker,
//    allAcceptedHosts: List<String>
    modifier: Modifier = Modifier
) {
    var webView: WebView? = remember {
        null
    }

    val requestHeaders = remember { mutableMapOf<String, String>() }

    DisposableEffect(key1 = Unit, effect = {
        webView?.onResume()
        onDispose {
            try {
                webView?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    })

    Column {
        if (webViewState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        WebView(
            state = webViewState,
            factory = {
                webView = WebView(it)
                webView!!
            },
            onCreated = { webView ->
                webView.settings.javaScriptEnabled = enableJavascript
                webView.settings.javaScriptCanOpenWindowsAutomatically = true
                webView.settings.domStorageEnabled = true
                webView.settings.allowFileAccess = true
                webView.settings.loadsImagesAutomatically = true

                webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webView.settings.displayZoomControls = false
                webView.settings.builtInZoomControls = true
                webView.settings.setSupportZoom(true)
                webView.settings.domStorageEnabled = true

                webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    if (mimetype != null) {
                        requestHeaders["Content-Type"] = mimetype
                    }

                    url?.let {
                        onEvent(
                            MusicScreenEvent.OnGetDownloadFileUrl(
                                it,
                                requestHeaders,
                                "GET",
                                URLUtil.guessFileName(url, contentDisposition, mimetype)
                            )
                        )
                    }
                }
            },
            modifier = modifier
                .padding(bottom = if (showAds) 50.dp else 0.dp),
            client = object : AccompanistWebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    return if (adBlocker.isAd(request.url.toString())) {
                        adBlocker.createEmptyResource()
                    } else {
                        requestHeaders.clear()
                        request.requestHeaders.forEach { (key, value) ->
                            requestHeaders[key] = value
                        }
                        super.shouldInterceptRequest(view, request)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest
                ): Boolean {
                    return if (adBlocker.isAd(request.url.toString())) {
                        // Block the ad by returning true
                        true
                    } else {
                        // Allow regular URLs to load
                        false
                    }
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    if (url?.contains("google") == true) {
                        val js = """
                            (function() {
                                var header = document.querySelector('header');
                                if (header) {
                                    header.parentNode.removeChild(header);
                                }
                            })();
                        """
                        val js2 = """
                            (function() {
                                var navElements = document.querySelectorAll('[role="navigation"]');
                                    navElements.forEach(function(nav) {
                                    nav.parentNode.removeChild(nav);
                                });
                            })();
                        """
                        view?.evaluateJavascript(js, null)
                        view?.evaluateJavascript(js2, null)
                    }
                }

                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    if (url?.contains("google") == true) {
                        val js = """
                            (function() {
                                var header = document.querySelector('header');
                                if (header) {
                                    header.parentNode.removeChild(header);
                                }
                            })();
                        """
                        val js2 = """
                            (function() {
                                var navElements = document.querySelectorAll('[role="navigation"]');
                                navElements.forEach(function(nav) {
                                    nav.parentNode.removeChild(nav);
                                });
                            })();
                        """
                        view.evaluateJavascript(js2, null)
                        view.evaluateJavascript(js, null)
                    }
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    val js = """
                        (function() {
                            var header = document.querySelector('header');
                            if (header) {
                                header.parentNode.removeChild(header);
                            }
                        })();
                    """
                    val js2 = """
                        (function() {
                            var navElements = document.querySelectorAll('[role="navigation"]');
                            navElements.forEach(function(nav) {
                                nav.parentNode.removeChild(nav);
                            });
                        })();
                    """
                    view.evaluateJavascript(js2, null)
                    view.evaluateJavascript(js, null)
                }
            },
            navigator = webViewNavigator,
            captureBackPresses = false,
        )
    }
}


fun isDownloadHintContainsInTheUrl(
    url: Uri?,
    downloadHint: String
): Boolean {
    val downloadHintParts = downloadHint.split("$")
    return downloadHintParts.any { url.toString().contains(it) }
}