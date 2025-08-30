package com.bytecoder.vplay.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bytecoder.vplay.fragments.OnlineFragment
import com.bytecoder.vplay.R
import com.bytecoder.vplay.fragments.OptionsFragment
import com.bytecoder.vplay.fragments.MusicFragment
import com.bytecoder.vplay.fragments.PlaylistFragment
import com.bytecoder.vplay.fragments.VideoFragment
import com.bytecoder.vplay.utils.StorePreference
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.bytecoder.vplay.player.MiniPlayerView
import com.bytecoder.vplay.player.music.MusicPlayerManager
import com.bytecoder.vplay.player.video.VideoPlayerManager
import com.bytecoder.vplay.utils.TabType

class MainActivity : AppCompatActivity() {

    private lateinit var lastTabStore: StorePreference
    private lateinit var bottomNav: BottomNavigationView

//    private lateinit var miniPlayerView: MiniPlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastTabStore = StorePreference(this)

        bottomNav = findViewById(R.id.bottomNav)

//        miniPlayerView = findViewById(R.id.mini_player_view)
//        if (VideoPlayerManager.isPlaybackActive()) {
//            miniPlayerView?.attach(VideoPlayerManager, this)
//        } else {
//            miniPlayerView?.attach(MusicPlayerManager, this)
//        }
//        MusicPlayerManager.subscribePlaybackUpdates {
//            if (MusicPlayerManager.isPlaybackActive()) {
//                runOnUiThread { miniPlayerView.attach(MusicPlayerManager, this) }
//            } else {
//                runOnUiThread { if (!VideoPlayerManager.isPlaybackActive()) miniPlayerView.detach() }
//            }
//        }
//        VideoPlayerManager.subscribePlaybackUpdates {
//            if (VideoPlayerManager.isPlaybackActive()) {
//                runOnUiThread { miniPlayerView.attach(VideoPlayerManager, this) }
//            } else {
//                runOnUiThread { if (!MusicPlayerManager.isPlaybackActive()) miniPlayerView.detach() }
//            }
//        }
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_video -> {
                    loadFragment(VideoFragment())
                    lastTabStore.saveLastTab(TabType.VIDEO)
                }
                R.id.nav_music -> {
                    loadFragment(MusicFragment())
                    lastTabStore.saveLastTab(TabType.MUSIC)
                }
                R.id.nav_playlist -> {
                    loadFragment(PlaylistFragment())
                    lastTabStore.saveLastTab(TabType.PLAYLIST)
                }
                R.id.nav_online -> {
                    loadFragment(OnlineFragment())
                    lastTabStore.saveLastTab(TabType.ONLINE)
                }
                R.id.nav_options -> {
                    loadFragment(OptionsFragment())
                    lastTabStore.saveLastTab(TabType.OPTIONS)
                }
            }
            true
        }

        // Restore last selected tab
        when (lastTabStore.getLastTab()) {
            TabType.VIDEO -> bottomNav.selectedItemId = R.id.nav_video
            TabType.MUSIC -> bottomNav.selectedItemId = R.id.nav_music
            TabType.PLAYLIST -> bottomNav.selectedItemId = R.id.nav_playlist
            TabType.ONLINE -> bottomNav.selectedItemId = R.id.nav_online
            TabType.OPTIONS -> bottomNav.selectedItemId = R.id.nav_options
            else -> bottomNav.selectedItemId = R.id.nav_video // default fallback
        }
    }

    // Added: handle back navigation to pop fragments and move app to background at root
//    override fun onBackPressed() {
//        try {
//            val navFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
//            if (navFragment != null) {
//                val navController = try { androidx.navigation.fragment.NavHostFragment.findNavController(navFragment) } catch (e: Exception) { null }
//                if (navController != null && navController.popBackStack()) { return }
//            }
//            if (supportFragmentManager.backStackEntryCount > 0) { supportFragmentManager.popBackStack(); return }
//            moveTaskToBack(true)
//        } catch (e: Exception) { super.onBackPressed() }
//    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
