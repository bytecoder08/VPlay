package com.bytecoder.vplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bytecoder.vplay.activities.ActionBarActions
import com.bytecoder.vplay.R

class OnlineFragment : Fragment(), ActionBarActions {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_online, container, false)
    }
}
