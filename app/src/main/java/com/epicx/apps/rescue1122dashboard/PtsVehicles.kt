package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.epicx.apps.rescue1122dashboard.databinding.PtsLayoutBinding
import java.util.*

class PtsVehicles : DialogFragment() {
    private var _binding: PtsLayoutBinding? = null
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PtsLayoutBinding.inflate(inflater, container, false)
        return binding!!.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!){
            bna7.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1338/edit")
                findNavController().navigate(direction)
            }
            bna9.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1310/edit")
                findNavController().navigate(direction)
            }
            bna11.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1202/edit")
                findNavController().navigate(direction)
            }
            bna10.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1324/edit")
                findNavController().navigate(direction)
            }
            bna21.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1311/edit")
                findNavController().navigate(direction)
            }
            bna22.setOnClickListener {
                val direction = com.epicx.apps.rescue1122dashboard.PtsVehiclesDirections.actionPtsVehiclesToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1301/edit")
                findNavController().navigate(direction)
            }

        }


    }
    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}