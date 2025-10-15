package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.*

class CheckVehicleStatus : Fragment(R.layout.check_vehicle_status) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Objects.requireNonNull((requireActivity() as AppCompatActivity).supportActionBar!!).setTitle(R.string.check_vehicle_status)
        Objects.requireNonNull((requireActivity() as AppCompatActivity).supportActionBar!!).show()

        val allVehicleStatus  = view.findViewById<AppCompatButton>(R.id.allVehicleStatus)
        val idle = view.findViewById<AppCompatButton>(R.id.idle)
        val maintenance = view.findViewById<AppCompatButton>(R.id.Maintenance)
        val onEmergency  = view.findViewById<AppCompatButton>(R.id.OnEmergency)
        val onProtocol    = view.findViewById<AppCompatButton>(R.id.OnProtocol)

        allVehicleStatus.setOnClickListener {
            val directions = CheckVehicleStatusDirections.actionCheckVehicleStatusToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle?station_id=&vehicle_type=&vehicle_status=")
            findNavController().navigate(directions)
        }
        idle.setOnClickListener {
            val directions = CheckVehicleStatusDirections.actionCheckVehicleStatusToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle?station_id=&vehicle_type=&vehicle_status=idle")
            findNavController().navigate(directions)
        }
        maintenance.setOnClickListener {
            val directions = CheckVehicleStatusDirections.actionCheckVehicleStatusToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle?station_id=&vehicle_type=&vehicle_status=maintenance")
            findNavController().navigate(directions)
        }
        onEmergency.setOnClickListener {
            val directions = CheckVehicleStatusDirections.actionCheckVehicleStatusToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle?station_id=&vehicle_type=&vehicle_status=on_emergency")
            findNavController().navigate(directions)
        }
        onProtocol.setOnClickListener {
            val directions = CheckVehicleStatusDirections.actionCheckVehicleStatusToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle?station_id=&vehicle_type=&vehicle_status=on_protocol")
            findNavController().navigate(directions)
        }

    }
}