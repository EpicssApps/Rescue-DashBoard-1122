package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.epicx.apps.rescue1122dashboard.databinding.MotorBikesBinding
import com.epicx.apps.rescue1122dashboard.viewModel.WebViewModel

class MotorBikes : DialogFragment() {

    private lateinit var pageViewModel: WebViewModel
    private var _binding: MotorBikesBinding? = null
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MotorBikesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!){
            BNB01.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1686/edit")
                findNavController().navigate(directions)
            }
            BNB02.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1675/edit")
                findNavController().navigate(directions)
            }
            BNB03.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1676/edit")
                findNavController().navigate(directions)
            }
            BNB04.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1690/edit")
                findNavController().navigate(directions)
            }
            BNB05.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1692/edit")
                findNavController().navigate(directions)
            }
            BNB06.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1693/edit")
                findNavController().navigate(directions)
            }
            BNB07.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1701/edit")
                findNavController().navigate(directions)
            }
            BNB08.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2422/edit")
                findNavController().navigate(directions)
            }
            BNB09.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2112/edit")
                findNavController().navigate(directions)
            }
            BNB10.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2506/edit")
                findNavController().navigate(directions)
            }
            BNB11.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2383/edit")
                findNavController().navigate(directions)
            }
            BNB12.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2043/edit")
                findNavController().navigate(directions)
            }
            BNB13.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2266/edit")
                findNavController().navigate(directions)
            }
            BNB14.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1691/edit")
                findNavController().navigate(directions)
            }
            BNB15.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/2035/edit")
                findNavController().navigate(directions)
            }
            BNB16.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3563/edit")
                findNavController().navigate(directions)
            }
            BNB17.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3564/edit")
                findNavController().navigate(directions)
            }
            BNB18.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3565/edit")
                findNavController().navigate(directions)
            }
            BNB19.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3566/edit")
                findNavController().navigate(directions)
            }
            BNB20.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3567/edit")
                findNavController().navigate(directions)
            }
            BNB21.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3568/edit")
                findNavController().navigate(directions)
            }
            BNB22.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3569/edit")
                findNavController().navigate(directions)
            }
            BNB23.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3570/edit")
                findNavController().navigate(directions)
            }
            BNB24.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3618/edit")
                findNavController().navigate(directions)
            }
            BNB25.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3572/edit")
                findNavController().navigate(directions)
            }
            BNB26.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3573/edit")
                findNavController().navigate(directions)
            }
            BNB27.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3574/edit")
                findNavController().navigate(directions)
            }
            BNB28.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3575/edit")
                findNavController().navigate(directions)
            }
            BNB29.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3576/edit")
                findNavController().navigate(directions)
            }
            BNB30.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3577/edit")
                findNavController().navigate(directions)
            }
            BNB31.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3578/edit")
                findNavController().navigate(directions)
            }
            BNB32.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3579/edit")
                findNavController().navigate(directions)
            }
            BNB33.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3580/edit")
                findNavController().navigate(directions)
            }
            BNB34.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3581/edit")
                findNavController().navigate(directions)
            }
            BNB35.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3582/edit")
                findNavController().navigate(directions)
            }
            BNB36.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3619/edit")
                findNavController().navigate(directions)
            }
            BNB37.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3620/edit")
                findNavController().navigate(directions)
            }
            BNB38.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3621/edit")
                findNavController().navigate(directions)
            }
            BNB39.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3622/edit")
                findNavController().navigate(directions)
            }
            BNB40.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3623/edit")
                findNavController().navigate(directions)
            }
            BNB41.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3624/edit")
                findNavController().navigate(directions)
            }
            BNB42.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3625/edit")
                findNavController().navigate(directions)
            }
            BNB43.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3626/edit")
                findNavController().navigate(directions)
            }
            BNB44.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3627/edit")
                findNavController().navigate(directions)
            }
            BNB45.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3628/edit")
                findNavController().navigate(directions)
            }
            BNB46.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3629/edit")
                findNavController().navigate(directions)
            }
            BNB47.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3583/edit")
                findNavController().navigate(directions)
            }
            BNB48.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3571/edit")
                findNavController().navigate(directions)
            }
            BNB49.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3584/edit")
                findNavController().navigate(directions)
            }
            BNB50.setOnClickListener {
                val directions = MotorBikesDirections.actionMotorBikeToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/3630/edit")
                findNavController().navigate(directions)
            }
            /*  BNB51.setOnClickListener {
                  val directions = MotorBikesDirections.actionMotorBikeToShowWebView("")
                  findNavController().navigate(directions)
              }*/
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