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

        val bna25 = view.findViewById<AppCompatButton>(R.id.btn25)
        val bna1 = view.findViewById<AppCompatButton>(R.id.btn26)
        val bna2 = view.findViewById<AppCompatButton>(R.id.bna29)
        val bna3 = view.findViewById<AppCompatButton>(R.id.btn30)
        val bna4 = view.findViewById<AppCompatButton>(R.id.btn33)
        val bnf1 = view.findViewById<AppCompatButton>(R.id.Bnf1)
        val bnf3 = view.findViewById<AppCompatButton>(R.id.Bnf3)
        val bnr1 = view.findViewById<AppCompatButton>(R.id.Bnr1)
        val bng1 = view.findViewById<AppCompatButton>(R.id.bng1)

        bna25.setOnClickListener{
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1467/edit")
            findNavController().navigate(directions)
        }

        bna1.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1423/edit")
            findNavController().navigate(directions)
        }
        bna2.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1489/edit")
            findNavController().navigate(directions)
        }
        bna3.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1570/edit")
            findNavController().navigate(directions)
        }
        bna4.setOnClickListener {
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
        bng1.setOnClickListener {
            val directions = ShowOperationDialogDirections.actionShowOperationDialogToShowWebView("https://punjab.rescue1122.org/dashboard/vehicle/1217/edit")
            findNavController().navigate(directions)
        }

    }


    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}