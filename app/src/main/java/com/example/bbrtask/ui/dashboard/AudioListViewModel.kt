package com.example.bbrtask.ui.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bbrtask.repository.RecordingRepository
import java.io.File

class AudioListViewModel : ViewModel() {

    private var recordingRepository: RecordingRepository? = null

    init {
        recordingRepository = RecordingRepository()
    }

    fun getAudioFiles(context: Context):MutableLiveData<File>{
        return recordingRepository?.getAudioFiles(context)!!
    }
}