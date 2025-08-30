package com.bytecoder.vplay.fragments

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.adapters.VideoAdapter
import com.bytecoder.vplay.model.MediaItem
import com.bytecoder.vplay.player.PlayerLauncher
//import com.bytecoder.vplay.player.video.VideoPlayerManager
//import com.bytecoder.vplay.player.video.VideoQueueActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var toggleButton: ImageButton
    private var isGrid = false
    private lateinit var adapter: VideoAdapter
    private val data = mutableListOf<MediaItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        recyclerView = view.findViewById(R.id.recyclerVideo)
        toggleButton = view.findViewById(R.id.btnToggleView)

        adapter = VideoAdapter(
            items = emptyList(),
            isGrid = isGrid,
            contentResolver = requireContext().contentResolver
        ) { item ->
        }

        setupRecycler()
        toggleButton.setOnClickListener { toggleLayout() }
        loadVideo()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    private fun setupRecycler() {
        recyclerView.layoutManager = if (isGrid) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
        recyclerView.adapter = adapter

        toggleButton.setImageResource(
            if (isGrid) R.drawable.list_24px else R.drawable.grid_view_24px
        )
    }

    private fun toggleLayout() {
        isGrid = !isGrid
        setupRecycler()
        adapter.setGrid(isGrid)
    }

    private fun loadVideo() {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                queryVideo()
            }
            data.clear()
            data.addAll(items)
            adapter.submit(items)
        }
    }

    private fun queryVideo(): List<MediaItem> {
        val list = mutableListOf<MediaItem>()

        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DURATION
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        requireContext().contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)/////////////////////////
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)//////////////
            val bucketCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)/////
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)///////////////////

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val name = cursor.getString(nameCol) ?: "Video"
                val folder = cursor.getString(bucketCol) ?: "Unknown"
                val dur = cursor.getLong(durCol)
                list.add(MediaItem(id, uri, name, folder, dur))
            }
        }
        return list
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        // --- Added: Queue button to open VideoQueueActivity ---
        val queueItem = menu.findItem(R.id.action_queue)
        queueItem?.setOnMenuItemClickListener {
//            val currentIndex = VideoPlayerManager.getCurrentIndex()
            val currentIndex = PlayerLauncher.getCurrentVideoIndex()
//            val intent = Intent(requireContext(), VideoQueueActivity::class.java)
//            intent.putExtra("currentIndex", currentIndex)
//            startActivity(intent)
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }
}
