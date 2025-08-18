package com.bytecoder.vplay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import androidx.appcompat.widget.Toolbar
import com.bytecoder.vplay.adapters.MusicAdapter

class MusicFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toggleButton: ImageButton
    private var isGrid = false
    private val items = List(20) { "Music Track ${it+1}" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)
        recyclerView = view.findViewById(R.id.recyclerMusic)
        toggleButton = view.findViewById(R.id.btnToggleView)

        setupRecycler()
        toggleButton.setOnClickListener { toggleLayout() }

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


    private fun setupRecycler() {
        recyclerView.layoutManager = if (isGrid) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        recyclerView.adapter = MusicAdapter(items, isGrid)

        toggleButton.setImageResource(
            if (isGrid) R.drawable.list_24px else R.drawable.grid_view_24px
        )
    }

    private fun toggleLayout() {
        isGrid = !isGrid
        setupRecycler()
    }
}
