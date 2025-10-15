package com.epicx.apps.rescue1122dashboard

import android.webkit.WebSettings
import android.webkit.WebView

object WebViewUtils {
    /**
     * Desktop mode toggle for WebView.
     * - enabled = true: sets a desktop user-agent and wider viewport
     * - enabled = false: restores default user-agent
     *
     * reload: when true, reloads the page after applying settings.
     */
    fun setDesktopMode(webView: WebView, enabled: Boolean, reload: Boolean = true) {
        val settings = webView.settings
        val defaultUA = WebSettings.getDefaultUserAgent(webView.context)

        if (enabled) {
            // A modern desktop UA string
            val desktopUA =
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
            settings.userAgentString = desktopUA
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        } else {
            settings.userAgentString = defaultUA
            // Keep your existing preferred defaults (you already enable these in fragments)
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        }

        if (reload) {
            webView.reload()
        }
    }
}