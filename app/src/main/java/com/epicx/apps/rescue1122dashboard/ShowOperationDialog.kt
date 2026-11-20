package com.epicx.apps.rescue1122dashboard

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController

class ShowOperationDialog : DialogFragment(R.layout.show_operation_dialog) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn04 = view.findViewById<AppCompatButton>(R.id.btn04)
        val bna25 = view.findViewById<AppCompatButton>(R.id.btn25)
        val btn09 = view.findViewById<AppCompatButton>(R.id.btn09)
        val btn17 = view.findViewById<AppCompatButton>(R.id.btn17)
        val bnf1 = view.findViewById<AppCompatButton>(R.id.Bnf1)
        val bnf3 = view.findViewById<AppCompatButton>(R.id.Bnf3)
        val bnr1 = view.findViewById<AppCompatButton>(R.id.Bnr1)
        val bnd1 = view.findViewById<AppCompatButton>(R.id.bnd1)


        btn04.setOnClickListener{
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/6213/edit")
            findNavController().navigate(directions)
        }

        bna25.setOnClickListener{
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1467/edit")
            findNavController().navigate(directions)
        }

        btn09.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/6227/edit")
            findNavController().navigate(directions)
        }
        btn17.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1425/edit")
            findNavController().navigate(directions)
        }
        bnf1.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1211/edit")
            findNavController().navigate(directions)
        }
        bnf3.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1214/edit")
            findNavController().navigate(directions)
        }
        bnr1.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1186/edit")
            findNavController().navigate(directions)
        }
        bnd1.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/6660/edit")
            findNavController().navigate(directions)
        }

    }


    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}