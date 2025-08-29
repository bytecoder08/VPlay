package com.bytecoder.vplay.activities

import android.animation.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bytecoder.vplay.databinding.ActivitySplashBinding
import com.bytecoder.vplay.utils.PermissionsHelper

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permissionsHelper = PermissionsHelper(this)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch", true)
        val anyGranted = permissionsHelper.getRequiredPermissions().any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (isFirstLaunch || !anyGranted) {
            prefs.edit().putBoolean("first_launch", false).apply()
            startActivity(Intent(this, GetPermissionsActivity::class.java))
            finish()
            return
        }

        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        val appName = binding.appName

        // 1. Fade in text
        val fadeIn = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f).apply {
            duration = 1000
        }

        // 2. Move text straight up + shrink + fade out
        val moveUp = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, -600f)
        val shrinkX = ObjectAnimator.ofFloat(appName, View.SCALE_X, 1f, 0.5f)
        val shrinkY = ObjectAnimator.ofFloat(appName, View.SCALE_Y, 1f, 0.5f)
        val fadeOut = ObjectAnimator.ofFloat(appName, View.ALPHA, 1f, 0f)

        val textAnim = AnimatorSet().apply {
            playTogether(moveUp, shrinkX, shrinkY, fadeOut)
            interpolator = AccelerateDecelerateInterpolator()
            duration = 1200
        }

        // End animation → directly go to Homepage
        textAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        })

        // Play sequence
        AnimatorSet().apply {
            playSequentially(fadeIn, textAnim)
            start()
        }
    }
}
