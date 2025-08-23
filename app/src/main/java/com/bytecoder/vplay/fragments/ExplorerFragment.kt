package com.bytecoder.vplay.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytecoder.vplay.R
import com.bytecoder.vplay.adapters.ExplorerAdapter
import com.bytecoder.vplay.player.PlayerLauncher
import java.io.File

class ExplorerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExplorerAdapter
    private var currentDir: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explorer, container, false)

        recyclerView = view.findViewById(R.id.recyclerExplorer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // --- CHANGED: Added PlayerLauncher inside adapter click ---
        adapter = ExplorerAdapter(emptyList()) { file ->
            if (file.isDirectory) {
                openFolder(file)
            } else {
                val extension = file.extension.lowercase()
                if (extension in listOf("mp3", "aac", "wav", "mp4", "mkv", "3gp")) {
                    PlayerLauncher.play(requireContext(), file.path, file.name)
                } else {
                    Toast.makeText(requireContext(), "File: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.adapter = adapter

        val rootDir = Environment.getExternalStorageDirectory()
        openFolder(rootDir)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!goBack()) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    private fun openFolder(folder: File) {
        currentDir = folder
        val files = folder.listFiles()?.sortedBy { it.name.lowercase() }?.toList() ?: emptyList()
        adapter.updateData(files)
    }

    private fun goBack(): Boolean {
        currentDir?.parentFile?.let { parent ->
            if (parent.canRead() && parent != currentDir) {
                openFolder(parent)
                return true
            }
        }
        return false
    }
}
