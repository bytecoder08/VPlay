package com.bytecoder.vplay.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bytecoder.vplay.R
import com.bytecoder.vplay.activities.GetPermissions

class PermissionsFragment : Fragment() {

    // Mapping to friendly category names
    private val permissionsMap = mapOf(
        android.Manifest.permission.INTERNET to "Network",
        android.Manifest.permission.ACCESS_NETWORK_STATE to "Network",
        android.Manifest.permission.POST_NOTIFICATIONS to "Notifications",
        android.Manifest.permission.READ_EXTERNAL_STORAGE to "Storage",
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE to "Storage",
        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE to "Storage",
        android.Manifest.permission.READ_MEDIA_IMAGES to "Photos",
        android.Manifest.permission.READ_MEDIA_AUDIO to "Audio",
        android.Manifest.permission.READ_MEDIA_VIDEO to "Video"
    )

    private lateinit var containerLayout: LinearLayout

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permissions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        containerLayout = view.findViewById(R.id.permissionsContainer)

        view.findViewById<Button>(R.id.btnAllowPermissions).setOnClickListener {
            startActivity(Intent(requireContext(), GetPermissions::class.java))
        }

        updatePermissionsList()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionsList() // Refresh after returning from GetPermissions
    }

    private fun updatePermissionsList() {
        containerLayout.removeAllViews()

        // Avoid duplicate categories
        val displayedCategories = mutableSetOf<String>()

        permissionsMap.forEach { (perm, friendlyName) ->
            if (displayedCategories.contains(friendlyName)) return@forEach
            displayedCategories.add(friendlyName)

            val granted = isPermissionGranted(requireContext(), perm)

            // Card-like row
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 24, 24, 24)
                setBackgroundResource(R.drawable.permission_card_bg)
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16) // spacing between rows
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    elevation = 4f
                }
            }

            val icon = ImageView(requireContext()).apply {
                setImageResource(
                    if (granted) R.drawable.check_circle_24px else R.drawable.error_circle_24px
                )
                setColorFilter(if (granted) Color.parseColor("#388E3C") else Color.parseColor("#D32F2F"))
                layoutParams = LinearLayout.LayoutParams(64, 64)
            }

            val tv = TextView(requireContext()).apply {
                text = friendlyName
                textSize = 18f
                setPadding(24, 0, 0, 0)
            }

            row.addView(icon)
            row.addView(tv)
            containerLayout.addView(row)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            permission == android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
