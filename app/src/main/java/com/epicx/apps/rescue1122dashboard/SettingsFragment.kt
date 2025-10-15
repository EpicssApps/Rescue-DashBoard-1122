package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Objects.requireNonNull((requireActivity() as AppCompatActivity).supportActionBar!!).hide()
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    companion object{
        var userName :String? = null
        var password :String? = null
        var location :String? = null
    }
}