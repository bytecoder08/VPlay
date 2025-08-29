package com.bytecoder.vplay.utils

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bytecoder.vplay.activities.MainActivity

class PermissionsHelper(private val activity: Activity) {

    // Collect required permissions depending on Android version
    fun getRequiredPermissions(): List<String> {
        val neededPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // Android 9 and below
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }

        } else {
            // Android 13+ (TIRAMISU and above)
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasAllFilesPermission()) {
            // Android 11+ requires MANAGE_EXTERNAL_STORAGE
            // All files access
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        }

        return neededPermissions
    }
    // Utility checkers & setter
    fun hasAllFilesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }
    // just checks if all permissions are granted
    fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().isEmpty() || hasAllFilesPermission()
    }

    // Start requesting permissions
    fun requestPermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        val neededPermissions = getRequiredPermissions()

        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions.toTypedArray())
            return
        }

        goToMain()
    }

    // Move to main activity
    fun goToMain() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    // Show dialog when permissions are denied
    fun showPermissionDialog(onRetry: () -> Unit) {
        val builder = AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage(
                "This app needs permissions to play your videos and music.\n" +
                        "Please grant them so the app can access files and work properly."
            )
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                // Try requesting again
                onRetry()
            }
            .setNegativeButton("App Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${activity.packageName}")
                activity.startActivity(intent)
            }

        // Only show "All Files Access" on Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setNeutralButton("All Files Access") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivity(intent)
                }
            }
        }

        builder.show()
    }
}
