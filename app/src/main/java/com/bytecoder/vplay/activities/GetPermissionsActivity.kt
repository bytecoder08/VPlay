package com.bytecoder.vplay.activities

import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bytecoder.vplay.R
import com.bytecoder.vplay.utils.PermissionsHelper

class GetPermissionsActivity : AppCompatActivity() {

    private lateinit var permissionsHelper: PermissionsHelper
    private lateinit var tvPermissionInfo: TextView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_permissions)

        permissionsHelper = PermissionsHelper(this)
        tvPermissionInfo = findViewById(R.id.tvPermissionInfo)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                if (allGranted || permissionsHelper.hasAllFilesPermission()) {
                    showSuccessMessage()
                    permissionsHelper.goToMain()
                } else {
                    permissionsHelper.showPermissionDialog {
                        permissionsHelper.requestPermissions(requestPermissionLauncher)
                    }
                }
            }

        findViewById<Button>(R.id.btnGrantPermissions).setOnClickListener {
            if (permissionsHelper.hasAllPermissions()) {
                // Already granted → just show styled success message
                showSuccessMessage()
            } else {
                // Request permissions
                permissionsHelper.requestPermissions(requestPermissionLauncher)
            }
        }

        findViewById<Button>(R.id.btnSkipPermissions).setOnClickListener {
            permissionsHelper.goToMain()
        }
    }
    private fun showSuccessMessage() {
        tvPermissionInfo.text = "All required permissions granted! \uD83C\uDF89"
        tvPermissionInfo.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        tvPermissionInfo.setTypeface(null, Typeface.BOLD)
        tvPermissionInfo.textSize = 18f
        tvPermissionInfo.setBackgroundResource(R.drawable.permission_success_bg)

        // Animate for visibility
        tvPermissionInfo.alpha = 0f
        tvPermissionInfo.scaleX = 0.8f
        tvPermissionInfo.scaleY = 0.8f
        tvPermissionInfo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start()
    }
}
