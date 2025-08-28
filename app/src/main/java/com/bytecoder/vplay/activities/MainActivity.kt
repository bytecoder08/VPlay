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
import com.bytecoder.vplay.utils.LastTabStore
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var lastTabStore: LastTabStore
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var actionSearch: ImageButton
    private lateinit var actionFilter: ImageButton
    private lateinit var actionSort: ImageButton
    private lateinit var actionMore: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lastTabStore = LastTabStore(this)
        try {
            val lastTab= lastTabStore.getLastTab()
            binding.bottomNav.selectedItemId = if (lastTab == 0) R.id.nav_music else R.id.nav_videos
            viewPager.currentItem = lastTab
        }catch (t: Throwable){

        }

        bottomNav = findViewById(R.id.bottomNav)
        actionSearch = findViewById(R.id.actionSearch)
        actionFilter = findViewById(R.id.actionFilter)
        actionSort = findViewById(R.id.actionSort)
        actionMore = findViewById(R.id.actionMore)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_videos    -> loadFragment(VideoFragment())
                R.id.nav_music     -> loadFragment(MusicFragment())
                R.id.nav_playlists -> loadFragment(PlaylistFragment())
                R.id.nav_online    -> loadFragment(OnlineFragment())
                R.id.nav_options   -> loadFragment(OptionsFragment())
            }
            true
        }

        // default tab
        bottomNav.selectedItemId = R.id.nav_videos

        // Actions row (for now, simple toasts; fragments can override later)
        actionSearch.setOnClickListener { currentFragment()?.onSearchClicked() ?: toast("Search") }
        actionFilter.setOnClickListener { currentFragment()?.onFilterClicked() ?: toast("Filter") }
        actionSort.setOnClickListener   { currentFragment()?.onSortClicked()   ?: toast("Sort") }
        actionMore.setOnClickListener   { currentFragment()?.onMoreClicked()   ?: toast("More") }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun currentFragment(): ActionBarActions? {
        val f = supportFragmentManager.findFragmentById(R.id.fragment_container)
        return if (f is ActionBarActions) f else null
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

interface ActionBarActions {
    fun onSearchClicked() {}
    fun onFilterClicked() {}
    fun onSortClicked() {}
    fun onMoreClicked() {}


    lastTabStore.saveLastTab(0) // 0 = Music, 1 = Video
    binding.bottomNav.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_music -> lastTabStore.saveLastTab(0)
            R.id.menu_video -> lastTabStore.saveLastTab(1)
        }
        false
    }
}
