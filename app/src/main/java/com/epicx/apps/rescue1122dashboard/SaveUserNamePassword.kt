package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import com.epicx.apps.rescue1122dashboard.MainDashBoard.Companion.reloadWeb
import com.epicx.apps.rescue1122dashboard.databinding.SaveUsernamePasswordBinding

class SaveUserNamePassword : DialogFragment() {
    private var location :String? = null
    private var _binding: SaveUsernamePasswordBinding? = null
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SaveUsernamePasswordBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCancelable = false

        val autoCT = view.findViewById<AutoCompleteTextView>(R.id.locationTextView)
        val reSources = resources.getStringArray(R.array.locationItems)
        val resourcesArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, reSources)
        autoCT.setAdapter(resourcesArrayAdapter)
        autoCT.setOnItemClickListener { _, _, position, _ ->
            location = resourcesArrayAdapter.getItem(position)
        }

        with(binding!!) {
            save.setOnClickListener {
                if (location.isNullOrEmpty()) {
                    locationTextView.error = "Set Location"
                }else if (userName.text.toString().isEmpty()) {
                    userName.error = "enter username"
                } else if (password.text.toString().isEmpty()) {
                    password.error = "enter password"
                } else {
                    when(location){
                        "RS-01 Bahawalnagar" -> {
                            location = "1"
                        }

                        "RS-02 Chistian" -> {
                            location = "2"
                        }

                        "RS-03 Haroonabad" -> {
                            location = "3"
                        }

                        "RS-04 Minchanabad" -> {
                            location = "4"
                        }

                        "RS-05 FortAbbas" -> {
                            location = "5"
                        }
                    }
                    val settingPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val editor = settingPreferences.edit()
                    editor.putString("userName", userName.text.toString().trimEnd())
                    editor.putString("password", password.text.toString().trimEnd())
                    editor.putString("location", location)
                    editor.apply()
                    Toast.makeText(activity, "username and password Saved", Toast.LENGTH_LONG).show()
                    reloadWeb()
                    dialog?.dismiss()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}