package com.bytecoder.vplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import androidx.appcompat.widget.Toolbar
import com.bytecoder.vplay.adapters.SimpleAdapter

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Videos Section
        val videoRecycler = view.findViewById<RecyclerView>(R.id.recyclerVideos)
        videoRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        videoRecycler.adapter = SimpleAdapter(getDummyList("Video"))

        // Music Section
        val musicRecycler = view.findViewById<RecyclerView>(R.id.recyclerMusic)
        musicRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        musicRecycler.adapter = SimpleAdapter(getDummyList("Music"))

        // Playlist Section
        val playlistRecycler = view.findViewById<RecyclerView>(R.id.recyclerPlaylists)
        playlistRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        playlistRecycler.adapter = SimpleAdapter(getDummyList("Playlist"))

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        // Setup search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Videos"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // TODO: filter adapter
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                // TODO: live filter adapter
                return false
            }
        })
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                // TODO: sorting logic
                true
            }
            R.id.action_filter -> {
                // TODO: filtering logic
                true
            }
            R.id.action_more -> {
                Toast.makeText(requireContext(), "More options clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun getDummyList(prefix: String): List<String> {
        return List(10) { "$prefix Item ${it+1}" }
    }
}
