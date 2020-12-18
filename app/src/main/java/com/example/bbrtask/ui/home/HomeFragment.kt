package com.example.bbrtask.ui.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bbrtask.R
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.File
import java.io.IOException
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    var audioSavePathInDevice: String? = null
    var mediaRecorder: MediaRecorder? = null
    var mediaPlayer: MediaPlayer? = null
    private var timer: Timer? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecodingPopPup()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }

    private fun checkPermission(): Boolean {
        val result = context?.let {
            ContextCompat.checkSelfPermission(
                it.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val result1 = context?.let {
            ContextCompat.checkSelfPermission(
                it.applicationContext,
                Manifest.permission.RECORD_AUDIO
            )
        }
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO),
            101
        )
    }

    private fun initRecodingPopPup() {
        stop_recording.isEnabled = false
        popup_play_recording.isEnabled = false
        stop_playing.isEnabled = false

        start_recording.setOnClickListener {
            if(checkPermission()) {
                val storageDir: File = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                audioSavePathInDevice = File.createTempFile(
                    "AudioRecording", ".3gp", storageDir
                ).apply {
                    // Save a file: path for use with ACTION_VIEW intents
                    audioSavePathInDevice
                }.absolutePath
                mediaRecorderReady()
                try {
                    mediaRecorder!!.prepare()
                    mediaRecorder!!.start()
                    recording_timer.base = SystemClock.elapsedRealtime()
                    recording_timer.start()
                    popup_report.isEnabled = false
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        val currentMaxAmplitude = mediaRecorder?.maxAmplitude
                        currentMaxAmplitude?.let { it1 -> audioRecordView.update(it1) }; //redraw view
                    }
                }, 0, 100)

                start_recording.isEnabled = false
                stop_recording.isEnabled = true
                popup_play_recording.isEnabled = false
                stop_playing.isEnabled = false

                Toast.makeText(
                    context, "Recording started",
                    Toast.LENGTH_LONG
                ).show()
            }
            else{
                requestPermission()
            }
        }

        stop_recording.setOnClickListener {
            try {
                mediaRecorder!!.stop()
            } catch (stopException: RuntimeException) {
                stopException.message
            }
            stop_recording.isEnabled = false
            popup_play_recording.isEnabled = true
            start_recording.isEnabled = true
            stop_playing.isEnabled = false
            popup_report.isEnabled = true
            timer?.cancel()
            recording_timer.stop()
            Toast.makeText(
                context, "Recording Completed",
                Toast.LENGTH_LONG
            ).show();
        }

        popup_play_recording.setOnClickListener {
            stop_recording.isEnabled = false
            start_recording.isEnabled = false
            popup_play_recording.isEnabled = false
            popup_play_recording.visibility = View.INVISIBLE
            txt_play_recording.visibility = View.INVISIBLE
            stop_playing.isEnabled = true
            stop_playing.visibility = View.VISIBLE
            popup_txt_stop_playing.visibility = View.VISIBLE

            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer?.setDataSource(audioSavePathInDevice)
                mediaPlayer?.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
               start_recording.isEnabled = true
                popup_play_recording.isEnabled = true
                popup_play_recording.visibility = View.VISIBLE
               txt_play_recording.visibility = View.VISIBLE
                stop_playing.isEnabled = false
               stop_playing.visibility = View.INVISIBLE
                popup_txt_stop_playing.visibility = View.INVISIBLE
            }
            Toast.makeText(
                context, "Recording Playing",
                Toast.LENGTH_LONG
            ).show()
        }

        stop_playing.setOnClickListener {
            stop_recording.isEnabled = false
           start_recording.isEnabled = true

            popup_play_recording.isEnabled = true
            popup_play_recording.visibility = View.VISIBLE
            txt_play_recording.visibility = View.VISIBLE
            stop_playing.isEnabled = false
            stop_playing.visibility = View.INVISIBLE
            popup_txt_stop_playing.visibility = View.INVISIBLE

            if(mediaPlayer != null){
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaRecorderReady()
            }
        }

        popup_report.setOnClickListener {
            start_recording.isEnabled = true
            stop_recording.isEnabled = false
            popup_play_recording.isEnabled = false
            stop_playing.isEnabled = false
            popup_report.isEnabled = false
            audioRecordView.recreate()
            timer?.cancel()
            recording_timer.stop()

            if(mediaPlayer != null){
                try {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                }catch (e: Exception)
                {
                    e.message
                }
            }
        }

    }

    private fun mediaRecorderReady() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder!!.setOutputFile(audioSavePathInDevice)
    }

}