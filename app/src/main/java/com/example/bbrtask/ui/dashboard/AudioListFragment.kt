package com.example.bbrtask.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bbrtask.MediaPlayerActivity
import com.example.bbrtask.R
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.io.File


class AudioListFragment : Fragment() {

    private lateinit var dashboardViewModel: AudioListViewModel
    private var audioFile: File? = null
    private var audioFileList: ArrayList<String>? = null
    private var adapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(AudioListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        dashboardViewModel.getAudioFiles(requireContext()).observe(viewLifecycleOwner, {
            if (it != null) {
                audioFile = it
                initList(audioFile!!)
            }
        })
    }

    private fun initList(audioFile: File) {
        val files = audioFile.listFiles()
        val filePaths = ArrayList<String>()
        audioFileList = ArrayList()

        for(file in files){
            audioFileList!!.add(file.name)
            filePaths.add(file.absolutePath)
        }

        adapter = context?.let { ArrayAdapter<String>(
            it,
            android.R.layout.simple_list_item_1,
            audioFileList!!
        ) }

        audioListView.adapter = adapter

        audioListView.onItemLongClickListener = OnItemLongClickListener { _, _, pos, _ ->
            val file = File(filePaths[pos])
            file.delete()
            filePaths.removeAt(pos)
            audioFileList!!.removeAt(pos)
            adapter?.notifyDataSetChanged()
            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
            true
        }

        audioListView.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(context, MediaPlayerActivity::class.java)
            intent.putExtra("audioPath", files[i].absolutePath.toString())
            intent.putExtra("position", i)
            intent.putExtra("audioFilesPaths", filePaths)
            context?.startActivity(intent)
        }
    }
}