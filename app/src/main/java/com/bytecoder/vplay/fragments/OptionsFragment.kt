package com.bytecoder.vplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bytecoder.vplay.activities.MainActivity
import com.bytecoder.vplay.R

class OptionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity

        view.findViewById<LinearLayout>(R.id.btnExplorer).setOnClickListener {
            activity.loadFragment(ExplorerFragment())
        }
        view.findViewById<LinearLayout>(R.id.btnSettings).setOnClickListener {
            activity.loadFragment(SettingsFragment())
        }
        view.findViewById<LinearLayout>(R.id.btnPermissions).setOnClickListener {
            activity.loadFragment(PermissionsFragment())
        }
        view.findViewById<LinearLayout>(R.id.btnFeedback).setOnClickListener {
            activity.loadFragment(FeedbackFragment())
        }
        view.findViewById<LinearLayout>(R.id.btnAbout).setOnClickListener {
            activity.loadFragment(AboutFragment())
        }
    }

}
