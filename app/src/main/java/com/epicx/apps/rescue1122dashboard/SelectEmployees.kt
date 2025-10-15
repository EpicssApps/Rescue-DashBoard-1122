package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SelectEmployees : Fragment(R.layout.select_employees) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val drivers = view.findViewById<AppCompatButton>(R.id.Drivers)
        val responders = view.findViewById<AppCompatButton>(R.id.responders)
        val bikeEmt = view.findViewById<AppCompatButton>(R.id.bikeEMT)

        drivers.setOnClickListener {
            val directions = SelectEmployeesDirections.actionSelectEmployeesToShowWebView("https://punjab.rescue1122.org/dashboard/employee/view/driver")
            findNavController().navigate(directions)
        }
        responders.setOnClickListener {
            val directions = SelectEmployeesDirections.actionSelectEmployeesToShowWebView("https://punjab.rescue1122.org/dashboard/employee/view/responder")
            findNavController().navigate(directions)
        }
        bikeEmt.setOnClickListener {
            val directions = SelectEmployeesDirections.actionSelectEmployeesToShowWebView("https://punjab.rescue1122.org/dashboard/employee/view/bike-emt")
            findNavController().navigate(directions)
        }

    }
}