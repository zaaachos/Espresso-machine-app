package com.example.espressomachineapp

import android.R.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_coffee_loading_screen.*
import kotlinx.android.synthetic.main.activity_coffee_maker.*
import kotlinx.android.synthetic.main.alert_box.view.*
import kotlinx.android.synthetic.main.coffee_done.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class CoffeeLoadingScreen : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    private lateinit var audio_clip: MediaPlayer
    private var tts: TextToSpeech? = null
    private var audioMinutes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coffee_loading_screen)
        progress_bar.progress = 0
        playerBtn.setOnClickListener(this)
        updateProgressResult()


    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {


            val flagData = loadFlagData()
            var result = tts!!.setLanguage(Locale("el_GR"))
            if (flagData == false) {
                result = tts!!.setLanguage(Locale("en"))
            }

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
                Toast.makeText(
                    this,
                    "Το κινητό είτε δεν υποστηρίζει ελληνικά στο TextToSpeech είτε πρέπει να επιλέξετε από τις ρυθμίσεις TTS της Google!",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                speakOut()
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private fun speakOut() {

        if (loadFlagData() == true)
            tts!!.speak(
                "Το ρόφημα σας είναι έτοιμο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Your drink is ready!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )

    }

    private fun loadFlagData(): Boolean? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("FLAG", true)
        return savedFlag

    }

    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

    }

    override fun onBackPressed() {
        if (progress_bar.progress == 100) {
            audio_clip.stop()
            val go_back = Intent(this, MainActivity::class.java)
            startActivity(go_back)
            super.onBackPressed()
        }
    }


    override fun onResume() {
        audio_clip = MediaPlayer.create(this, R.raw.audio)
        audio_clip.setOnPreparedListener(MediaPlayer.OnPreparedListener { player -> player.start() })
        audio_clip.setVolume(0F,0F)
        audio_clip.seekTo(audioMinutes)
        super.onResume()
    }

    override fun onPause() {

        audio_clip.pause()
        audioMinutes = audio_clip.currentPosition
        super.onPause()
    }

    override fun onStop() {

        audio_clip.stop()
        super.onStop()

    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == playerBtn.id) {
                if (playerBtn.drawable.constantState == resources.getDrawable(R.drawable.sound).constantState) {
                    playerBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.no_sound,
                            null
                        )
                    )
                    audio_clip.setVolume(1F, 1F)
                } else {
                    playerBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.sound,
                            null
                        )
                    )
                    audio_clip.setVolume(0F, 0F)

                }

            }

        }
    }

    fun doneSound() {
        audio_clip = MediaPlayer.create(this, R.raw.done)
        audio_clip.setOnPreparedListener(MediaPlayer.OnPreparedListener { player -> player.start() })
        audio_clip.setVolume(1F, 1F)
        audio_clip.start()

        if (loadSwitch()) {
            tts = TextToSpeech(this, this)
            tts?.setSpeechRate(0.85f)
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                override fun onDone(utteranceId: String?) {
                    val go_back = Intent(applicationContext, MainActivity::class.java)
                    startActivity(go_back)
                    finish()
                }

                override fun onError(utteranceId: String?) {
                    //do whatever you want if TTS makes an error.
                }

                override fun onStart(utteranceId: String?) {
                    //do whatever you want when TTS start speaking.
                }
            })

        } else {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.coffee_done, null)
            builder.setView(dialogLayout)
            val dialog: AlertDialog = builder.create()
            dialog.setOnCancelListener {
                val go_back = Intent(applicationContext, MainActivity::class.java)
                startActivity(go_back)
                finish()
            }
            dialogLayout.OKBtn.setOnClickListener {
                audio_clip.stop()
                val go_back = Intent(this, MainActivity::class.java)
                startActivity(go_back)
                finish()
            }
            dialog.show()


        }


    }

    private fun updateProgressResult() {

        GlobalScope.launch(context = Dispatchers.Main) {
            while (true) {
                if (progress_bar.progress >= 100) {
                    break
                }
                delay(300)
                var prog = 1
                progress_bar.progress += prog
                percentageId.setText(progress_bar.progress.toString() + " %")


            }
            if (progress_bar.progress == 100) {
                audio_clip.stop()
                audio_clip.reset()
                doneSound()
            }
        }

    }

}


