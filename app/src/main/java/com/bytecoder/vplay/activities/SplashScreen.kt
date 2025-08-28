package com.bytecoder.vplay.activities

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.bytecoder.vplay.databinding.SplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            val perms = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val missing = perms.any { androidx.core.content.ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (missing) {
                val i = android.content.Intent(this, GetPermissions::class.java)
                startActivity(i)
                finish()
                return
            }
        } catch (e: Exception) { }

        super.onCreate(savedInstanceState)
        binding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
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
