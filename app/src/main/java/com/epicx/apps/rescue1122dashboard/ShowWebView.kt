package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.epicx.apps.rescue1122dashboard.viewModel.WebViewModel
import android.content.Intent
import androidx.preference.PreferenceManager

class ShowWebView : Fragment(R.layout.show_web_view){
    private lateinit var pageViewModel: WebViewModel
    var myWebView: WebView? = null

    private var isDesktopMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        myWebView = view.findViewById(R.id.showBillWebView)
        pageViewModel = ViewModelProvider(this).get(WebViewModel::class.java)
        pageViewModel.setWebView(myWebView!!)

        val settingPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        SettingsFragment.userName = settingPreferences.getString("userName","")!!
        SettingsFragment.password = settingPreferences.getString("password","")!!
        isDesktopMode = settingPreferences.getBoolean("desktop_mode", false)

        val args = ShowWebViewArgs.fromBundle(requireArguments())
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_circular)

        myWebView?.webViewClient = MyWebViewClient(
            context = requireContext(),
            progressBar = progressBar,
            userName = SettingsFragment.userName!!,
            password = SettingsFragment.password!!
        )

        val myWebSettings = myWebView?.settings
        myWebSettings?.javaScriptEnabled = true
        myWebSettings?.loadWithOverviewMode = true
        myWebSettings?.useWideViewPort = true
        myWebSettings?.builtInZoomControls = true
        myWebSettings?.displayZoomControls = false

        // Apply desktop mode (without reloading initially)
        myWebView?.let { WebViewUtils.setDesktopMode(it, isDesktopMode, reload = false) }

        myWebView?.loadUrl(args.url!!)

        myWebView?.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && myWebView?.canGoBack() == true) {
                myWebView?.goBack()
                return@OnKeyListener true
            }
            false
        })

        myWebView?.setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        pageViewModel.getWebView().observe(requireActivity(), { customWebView ->
            if (customWebView != null) {
                myWebView = customWebView
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_desktop_toggle, menu)
        val item = menu.findItem(R.id.action_desktop_site)
        item?.isChecked = isDesktopMode
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_desktop_site) {
            isDesktopMode = !item.isChecked
            item.isChecked = isDesktopMode
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit()
                .putBoolean("desktop_mode", isDesktopMode)
                .apply()
            myWebView?.let { WebViewUtils.setDesktopMode(it, isDesktopMode, reload = true) }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class MyWebViewClient(
        private val context: Context,
        progressBar: ProgressBar,
        userName: String,
        password: String
    ) : WebViewClient(){
        private val myProgressBar = progressBar
        private val userNameOf = userName
        private val passwordOf = password

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            myProgressBar.visibility = View.VISIBLE
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            myProgressBar.visibility = View.GONE

            // Auto-fill login
            if (url?.contains("/login") == true){
                view?.loadUrl("javascript:(function(){  document.getElementsByTagName('input')[1].value ='$userNameOf';})();")
                view?.loadUrl("javascript:(function(){ document.getElementsByTagName('input')[2].value ='$passwordOf';})();")
                view?.evaluateJavascript("document.getElementById('captcha') && (document.getElementById('captcha').inputMode = 'numeric');", null)
            }

            // Force desktop viewport when desktop mode is ON
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val desktopMode = prefs.getBoolean("desktop_mode", false)
            if (desktopMode) {
                // Set a wider viewport and scale it to fit the device width
                val js = """
                    (function(){
                        try {
                            var target = 1280; // target desktop width in CSS px
                            var scale = (window.innerWidth || document.documentElement.clientWidth || 360) / target;
                            if (!isFinite(scale) || scale <= 0) { scale = 0.5; }
                            var content = 'width=' + target + ', initial-scale=' + scale + ', minimum-scale=0.1, maximum-scale=5, user-scalable=1';
                            var meta = document.querySelector('meta[name=viewport]');
                            if (meta) {
                                meta.setAttribute('content', content);
                            } else {
                                meta = document.createElement('meta');
                                meta.name = 'viewport';
                                meta.content = content;
                                document.head.appendChild(meta);
                            }
                        } catch(e) {}
                    })();
                """.trimIndent()
                view?.evaluateJavascript(js, null)
            }

            super.onPageFinished(view, url)
        }

        // For modern Android (Lollipop+)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url
            if (uri != null) {
                view?.loadUrl(uri.toString())
                return true
            }
            return false
        }

        // For older Android
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                view?.loadUrl(url)
                return true
            }
            return false
        }
    }
}