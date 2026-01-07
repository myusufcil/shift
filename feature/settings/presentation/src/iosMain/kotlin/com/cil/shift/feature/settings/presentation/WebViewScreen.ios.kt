package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(url: String, modifier: Modifier) {
    val nsUrl = remember(url) { NSURL.URLWithString(url) }

    UIKitView(
        factory = {
            val config = WKWebViewConfiguration()
            WKWebView(frame = kotlinx.cinterop.cValue { }, configuration = config).apply {
                nsUrl?.let { loadRequest(NSURLRequest.requestWithURL(it)) }
            }
        },
        update = { webView ->
            nsUrl?.let { webView.loadRequest(NSURLRequest.requestWithURL(it)) }
        },
        modifier = modifier.fillMaxSize()
    )
}
