package com.example.bbrtask.repository

import android.content.Context
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import java.io.File

class RecordingRepository {

    fun getAudioFiles(context: Context):MutableLiveData<File> {

        val audioLiveDataList :MutableLiveData<File> = MutableLiveData()

        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        audioLiveDataList.postValue(storageDir)

        return audioLiveDataList
    }
}