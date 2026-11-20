package com.epicx.apps.rescue1122dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.AlarmManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.epicx.apps.rescue1122dashboard.viewModel.WebViewModel
import android.content.Intent
import android.os.Build
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

        myWebView?.addJavascriptInterface(WebAppInterface(requireContext(), myWebView!!), "AndroidBridge")

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

    class WebAppInterface(
        private val context: Context,
        private val webView: WebView
    ) {
        @JavascriptInterface
        fun logMessage(msg: String) {
            Log.d("WebClientDebug", msg)
        }

        @JavascriptInterface
        fun onVehicleMadeActive(vehicleName: String) {
            Log.d("WebClientDebug", "Vehicle made active/idle, cancelling timer for: $vehicleName")
            Handler(Looper.getMainLooper()).post {
                VehicleTimerManager.cancelTimer(context, vehicleName)
                Toast.makeText(context, "Timer cancelled for $vehicleName (Vehicle is Active)", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun onMinorMaintenanceSubmitted(vehicleName: String, reason: String) {
            Log.d("WebClientDebug", "Minor maintenance submitted intercepted for: $vehicleName, reason: $reason")

            // Check and request notification permission on Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    if (context is android.app.Activity) {
                        context.requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
                    }
                }
            }

            // Check exact alarm permission on Android 12+ (API 31+) for Vivo / Android 14+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
            }

            Handler(Looper.getMainLooper()).post {
                showTimeSelectionDialog(context, vehicleName, reason) { selectedMinutes ->
                    webView.post {
                        val js = """
                            (function() {
                                window.allowVehicleSubmit = true;
                                var btn = document.getElementById('submit_btn');
                                if (btn) {
                                    btn.click();
                                } else {
                                    var form = document.getElementById('vehicle_form') || document.querySelector('form');
                                    if (form) form.submit();
                                }
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(js, null)
                    }
                    VehicleTimerManager.startTimer(context, vehicleName, reason, selectedMinutes)
                }
            }
        }

        private fun showTimeSelectionDialog(context: Context, vehicleName: String, reason: String, onTimeSelected: (Int) -> Unit) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_select_time, null)
            val tvVehicleInfo = dialogView.findViewById<TextView>(R.id.tvVehicleInfo)
            val spinnerTime = dialogView.findViewById<Spinner>(R.id.spinnerTime)

            tvVehicleInfo.text = "Vehicle: $vehicleName\nReason: $reason"

            val items = arrayOf(
                "-- Select Timer Duration --",
                "1 Minute (Testing)",
                "10 Minutes", "15 Minutes", "20 Minutes", "25 Minutes",
                "30 Minutes", "35 Minutes", "40 Minutes", "45 Minutes",
                "50 Minutes", "55 Minutes", "60 Minutes"
            )
            val timeValues = intArrayOf(0, 1, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTime.adapter = adapter

            val dialog = AlertDialog.Builder(context)
                .setTitle("Minor Maintenance Timer")
                .setView(dialogView)
                .setPositiveButton("Submit & Start Timer", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.setOnShowListener {
                val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    val selectedPosition = spinnerTime.selectedItemPosition
                    if (selectedPosition == 0) {
                        Toast.makeText(context, "Please select a valid timer duration!", Toast.LENGTH_SHORT).show()
                    } else {
                        val selectedMinutes = timeValues[selectedPosition]
                        dialog.dismiss()
                        onTimeSelected(selectedMinutes)
                    }
                }
            }
            dialog.show()
        }
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

            // Intercept submit_btn click for Minor Maintenance
            if (url?.contains("/vehicle/") == true) {
                val interceptJs = """
                    (function() {
                        if (window.vehicleInterceptorInitialized) return;
                        window.vehicleInterceptorInitialized = true;
                        
                        document.addEventListener('click', function(e) {
                            if (window.allowVehicleSubmit) {
                                return;
                            }
                            var btn = e.target.closest('#submit_btn, button[type=submit], input[type=submit]');
                            if (btn) {
                                var statusSel = document.getElementById('status');
                                var typeSel = document.getElementById('maintenanceType');
                                var reasonSel = document.getElementById('reason_maintenance_id');
                                
                                var regNo = document.getElementById('registration_no') ? document.getElementById('registration_no').value.trim() : '';
                                var licenseNo = document.getElementById('license_no') ? document.getElementById('license_no').value.trim() : '';
                                var vehicleName = (regNo || licenseNo) ? (regNo + (licenseNo ? ' (' + licenseNo + ')' : '')) : ('Vehicle_' + Date.now());
                                
                                if (statusSel && statusSel.value === 'idle') {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.logMessage("Vehicle made active/idle: " + vehicleName);
                                        window.AndroidBridge.onVehicleMadeActive(vehicleName);
                                    }
                                } else if (statusSel && statusSel.value === 'maintenance' && typeSel && typeSel.value === 'minor') {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    e.stopImmediatePropagation();
                                    
                                    var reasonText = 'Minor Maintenance';
                                    if (reasonSel && reasonSel.selectedIndex >= 0) {
                                        var opt = reasonSel.options[reasonSel.selectedIndex];
                                        reasonText = opt.getAttribute('data-title') || opt.text || 'Minor Maintenance';
                                    }
                                    
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.logMessage("Intercepted submit click for minor maintenance vehicle: " + vehicleName);
                                        window.AndroidBridge.onMinorMaintenanceSubmitted(vehicleName, reasonText);
                                    }
                                }
                            }
                        }, true);
                    })();
                """.trimIndent()
                view?.evaluateJavascript(interceptJs, null)
            }

            // Force desktop viewport when desktop mode is ON
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (prefs.getBoolean("desktop_mode", false)) {
                val js = """
                    (function(){
                        try {
                            var target = 1280;
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

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url
            if (uri != null) {
                view?.loadUrl(uri.toString())
                return true
            }
            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                view?.loadUrl(url)
                return true
            }
            return false
        }
    }
}
