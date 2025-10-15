package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.epicx.apps.rescue1122dashboard.databinding.MainDashboardBinding
import com.epicx.apps.rescue1122dashboard.viewModel.WebViewModel

class MainDashBoard :Fragment() {

    private lateinit var pageViewModel: WebViewModel
    private var _binding: MainDashboardBinding? = null
    private val binding get() = _binding

    private var isDesktopMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MainDashboardBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = binding?.showBillWebView!!
        progressBar = binding?.progressCircular!!
        mContext = requireContext()

        pageViewModel = ViewModelProvider(this).get(WebViewModel::class.java)
        pageViewModel.setWebView(webView!!)

        val settingPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        SettingsFragment.userName = settingPreferences.getString("userName","")!!
        SettingsFragment.password = settingPreferences.getString("password","")!!
        isDesktopMode = settingPreferences.getBoolean("desktop_mode", false)

        if (SettingsFragment.userName!!.isEmpty()){
            val direction = MainDashBoardDirections.actionMainDashBoardToSaveUserNamePassword()
            findNavController().navigate(direction)
        }

        webView!!.webViewClient = ShowWebView.MyWebViewClient(
            context = requireContext(),
            progressBar = binding?.progressCircular!!,
            userName = SettingsFragment.userName!!,
            password = SettingsFragment.password!!
        )

        val myWebSettings = webView!!.settings
        myWebSettings.javaScriptEnabled = true
        myWebSettings.loadWithOverviewMode = true
        myWebSettings.useWideViewPort = true
        myWebSettings.builtInZoomControls = true
        myWebSettings.displayZoomControls = false

        // Apply Desktop mode before loading
        WebViewUtils.setDesktopMode(webView!!, isDesktopMode, reload = false)

        webView!!.loadUrl("https://punjab.rescue1122.org/dashboard")

        webView!!.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
                webView!!.goBack()
                return@OnKeyListener true
            }
            false
        })

        webView!!.setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_desktop_toggle, menu)
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        isDesktopMode = prefs.getBoolean("desktop_mode", false)
        menu.findItem(R.id.action_desktop_site)?.isChecked = isDesktopMode
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
            webView?.let { WebViewUtils.setDesktopMode(it, isDesktopMode, reload = true) }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object{
        @SuppressLint("StaticFieldLeak")
        var webView:WebView? = null
        @SuppressLint("StaticFieldLeak")
        var progressBar:ProgressBar? =  null
        @SuppressLint("StaticFieldLeak")
        var mContext:Context? =  null

        fun reloadWeb(){
            val settingPreferences = PreferenceManager.getDefaultSharedPreferences(mContext!!)
            SettingsFragment.userName = settingPreferences.getString("userName","")!!
            SettingsFragment.password = settingPreferences.getString("password","")!!
            val isDesktop = settingPreferences.getBoolean("desktop_mode", false)

            webView!!.webViewClient = ShowWebView.MyWebViewClient(
                context = mContext!!,
                progressBar = progressBar!!,
                userName = SettingsFragment.userName!!,
                password = SettingsFragment.password!!
            )

            // Apply Desktop mode before reload
            WebViewUtils.setDesktopMode(webView!!, isDesktop, reload = false)

            webView?.reload()
        }
    }

}