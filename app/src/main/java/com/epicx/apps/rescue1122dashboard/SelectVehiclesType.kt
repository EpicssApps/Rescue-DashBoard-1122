package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.epicx.apps.rescue1122dashboard.databinding.SelectTypeVehiclesBinding

class SelectVehiclesType : Fragment() {

    private var _binding: SelectTypeVehiclesBinding? = null
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SelectTypeVehiclesBinding.inflate(inflater, container, false)
        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settingPreferences = PreferenceManager.getDefaultSharedPreferences(MainDashBoard.mContext!!)
        SettingsFragment.location = settingPreferences.getString("location","")!!

        with(binding!!){

            when (SettingsFragment.location) {

                "1" -> {
                    bahawalnagar.visibility = View.VISIBLE
                }

                "2" -> {
                    chistian.visibility = View.VISIBLE
                }

                "3" -> {
                    Haroonabad.visibility = View.VISIBLE
                }

                "4" -> {
                    Minchanabad.visibility = View.VISIBLE
                }

                "5" -> {
                    Fortabbas.visibility = View.VISIBLE
                }
            }


            BNF05.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1196/edit")
                findNavController().navigate(directions)
            }
            BNA16.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1219/edit")
                findNavController().navigate(directions)
            }
            BNA2.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1312/edit")
                findNavController().navigate(directions)
            }
            BNA18.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1329/edit")
                findNavController().navigate(directions)
            }
            BNA28.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1575/edit")
                findNavController().navigate(directions)
            }
            BNA15.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1634/edit")
                findNavController().navigate(directions)
            }
            BNA13.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1201/edit")
                findNavController().navigate(directions)
            }
            BNF02.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1203/edit")
                findNavController().navigate(directions)
            }
            BNA14.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1220/edit")
                findNavController().navigate(directions)
            }
            BNA8.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1305/edit")
                findNavController().navigate(directions)
            }
            BNA12.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1314/edit")
                findNavController().navigate(directions)
            }
            BNA27.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1421/edit")
                findNavController().navigate(directions)
            }
            BNF06.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1185/edit")
                findNavController().navigate(directions)
            }
            BNA20.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1213/edit")
                findNavController().navigate(directions)
            }
            BNA19.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1215/edit")
                findNavController().navigate(directions)
            }
            BNA6.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1336/edit")
                findNavController().navigate(directions)
            }
            BNA32.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1493/edit")
                findNavController().navigate(directions)
            }
            // Fortabbas
            BNF04.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1182/edit")
                findNavController().navigate(directions)
            }
            BNA23.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1188/edit")
                findNavController().navigate(directions)
            }
            BNA24.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1326/edit")
                findNavController().navigate(directions)
            }
            BNA4.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1416/edit")
                findNavController().navigate(directions)
            }
            BNA31.setOnClickListener {
                val directions = SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1467/edit")
                findNavController().navigate(directions)
            }

            Operational.setOnClickListener {
                val directions =  SelectVehiclesTypeDirections.actionSelectVehiclesTypeToShowOperationDialog()
                findNavController().navigate(directions)
            }
            ptsVehicles.setOnClickListener {
                val directions =  SelectVehiclesTypeDirections.actionSelectVehiclesTypeToPtsVehicles()
                findNavController().navigate(directions)
            }
            MotorBike.setOnClickListener {
                val directions =  SelectVehiclesTypeDirections.actionSelectVehiclesTypeToMotorBike()
                findNavController().navigate(directions)
            }
        }


    }

}