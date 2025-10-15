package com.epicx.apps.rescue1122dashboard.viewModel

import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class WebViewModel : ViewModel() {
    private val liveData: MutableLiveData<WebView> = MutableLiveData<WebView>()
    fun setWebView(webView: WebView) {
        liveData.value = webView
    }

    fun getWebView(): LiveData<WebView> {
        return liveData
    }
}