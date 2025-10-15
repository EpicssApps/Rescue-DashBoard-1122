package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.epicx.apps.rescue1122dashboard.databinding.ShowReportsBinding
import com.epicx.apps.rescue1122dashboard.viewModel.WebViewModel

class ShowEmergencyReports : Fragment() {

    private lateinit var pageViewModel: WebViewModel
    private var _binding: ShowReportsBinding? = null
    private val binding get() = _binding

    private var isDesktopMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ShowReportsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageViewModel = ViewModelProvider(this).get(WebViewModel::class.java)
        pageViewModel.setWebView(binding?.showBillWebView!!)

        val settingPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        SettingsFragment.userName = settingPreferences.getString("userName","")!!
        SettingsFragment.password = settingPreferences.getString("password","")!!
        isDesktopMode = settingPreferences.getBoolean("desktop_mode", false)

        binding?.showBillWebView!!.webViewClient = ShowWebView.MyWebViewClient(
            context = requireContext(),
            progressBar = binding?.progressCircular!!,
            userName = SettingsFragment.userName!!,
            password = SettingsFragment.password!!
        )

        val myWebSettings = binding?.showBillWebView!!.settings
        myWebSettings.javaScriptEnabled = true
        myWebSettings.loadWithOverviewMode = true
        myWebSettings.useWideViewPort = true
        myWebSettings.builtInZoomControls = true
        myWebSettings.displayZoomControls = false

        // Apply Desktop mode before first load
        WebViewUtils.setDesktopMode(binding?.showBillWebView!!, isDesktopMode, reload = false)

        binding?.showBillWebView!!.loadUrl("https://punjab.rescue1122.org/dashboard/report/index")

        binding?.showBillWebView!!.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && binding?.showBillWebView!!.canGoBack()) {
                binding?.showBillWebView!!.goBack()
                return@OnKeyListener true
            }
            false
        })

        binding?.showBillWebView!!.setDownloadListener { url, _, _, _, _ ->
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
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
            binding?.showBillWebView?.let { WebViewUtils.setDesktopMode(it, isDesktopMode, reload = true) }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}