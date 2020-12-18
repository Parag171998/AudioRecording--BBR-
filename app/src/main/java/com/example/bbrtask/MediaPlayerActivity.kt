package com.example.bbrtask

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_media_player.*
import java.util.*
import kotlin.collections.ArrayList

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false
    private var audioFilePaths: ArrayList<String>?= null
    private var isShuffleOn = false
    private var pos = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        pos = intent.getIntExtra("position", -1)
        textAudioNo.text = "Audio: $pos"
        audioFilePaths = intent.getStringArrayListExtra("audioFilesPaths")

        if(pos == 0){
            previousBtn.isEnabled = false
        }
        if(pos == audioFilePaths?.size!!-1){
            nextBtn.isEnabled = false
        }

        setMediaPlayer()
    }

    @SuppressLint("SetTextI18n")
    private fun setMediaPlayer() {
        mediaPlayer = MediaPlayer()

        playBtn.setOnClickListener{
            playAudio(pos)
        }
        // Pause the media player
        pauseBtn.setOnClickListener {
            if(mediaPlayer.isPlaying){
                mediaPlayer.pause()
                pause = true
                playBtn.isEnabled = true
                pauseBtn.isEnabled = false
                stopBtn.isEnabled = true
            }
        }

        nextBtn.setOnClickListener {
            ++pos
            if(pos < audioFilePaths?.size!!){
                previousBtn.isEnabled = true
                playAudio(pos)
            }
            else{
                --pos
                Toast.makeText(this, "No more audios", Toast.LENGTH_SHORT).show()
            }
        }

        previousBtn.setOnClickListener {
            --pos
            if(pos >= 0){
                nextBtn.isEnabled = true
                playAudio(pos)
            }
            else{
                ++pos
                Toast.makeText(this, "No more audios", Toast.LENGTH_SHORT).show()
            }
        }

        // Stop the media player
        stopBtn.setOnClickListener{
            if(mediaPlayer.isPlaying || pause.equals(true)){
                pause = false
                seek_bar.progress = 0
                mediaPlayer.stop()
                mediaPlayer.reset()
                handler.removeCallbacks(runnable)

                playBtn.isEnabled = true
                pauseBtn.isEnabled = false
                stopBtn.isEnabled = false
                tv_pass.text = ""
                tv_due.text = ""
            }
        }

        shuffleBtn.setOnClickListener {
            isShuffleOn = !isShuffleOn
            if(isShuffleOn){
                shuffleBtn.text = "Shuffle On"
            }
            else{
                shuffleBtn.text = "Shuffle Off"
            }
        }

        // Seek bar change listener
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun playAudio(pos: Int) {
        if(pos >=0 && pos < audioFilePaths?.size!!) {
            textAudioNo.text = "Audio: $pos"

            previousBtn.isEnabled = pos - 1 >= 0
            nextBtn.isEnabled = pos + 1 < audioFilePaths?.size!!

            if(!pause) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(audioFilePaths?.get(pos))
                mediaPlayer.prepare()
                seek_bar.progress = 0
            }

            if (pause) {
                mediaPlayer.seekTo(mediaPlayer.currentPosition)
                mediaPlayer.start()
                pause = false
            } else {
                mediaPlayer.start()

            }
            initializeSeekBar()
            playBtn.isEnabled = false
            pauseBtn.isEnabled = true
            stopBtn.isEnabled = true

            mediaPlayer.setOnCompletionListener {
                if(isShuffleOn){
                    val r = Random()
                    this.pos = r.nextInt(audioFilePaths!!.size)
                    textAudioNo.text = "Audio: ${this.pos}"
                    playAudio(this.pos)
                }
                else {
                    playBtn.isEnabled = true
                    pauseBtn.isEnabled = false
                    stopBtn.isEnabled = false
                }
            }
        }

    }

    // Method to initialize seek bar and audio stats
    @SuppressLint("SetTextI18n")
    private fun initializeSeekBar() {
        seek_bar.max = mediaPlayer.seconds

        runnable = Runnable {
            seek_bar.progress = mediaPlayer.currentSeconds

            tv_pass.text = "${mediaPlayer.currentSeconds} sec"
            val diff = mediaPlayer.seconds - mediaPlayer.currentSeconds
            tv_due.text = "$diff sec"

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }
}
// Creating an extension property to get the media player time duration in seconds
val MediaPlayer.seconds:Int
    get() {
        return this.duration / 1000
    }
// Creating an extension property to get media player current position in seconds
val MediaPlayer.currentSeconds:Int
    get() {
        return this.currentPosition/1000
    }