package com.example.espressomachineapp

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_welcome_screen_for_blind_people.*
import java.util.*


class WelcomeScreenForBlindPeople : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var timer: CountDownTimer
    private var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen_for_blind_people)

        tts = TextToSpeech(this, this)
        tts?.setSpeechRate(0.9f)
        tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

            override fun onDone(utteranceId: String?) {

            }

            override fun onError(utteranceId: String?) {
                //do whatever you want if TTS makes an error.
            }

            override fun onStart(utteranceId: String?) {
                //do whatever you want when TTS start speaking.
            }
        })

        timer = object : CountDownTimer(30000, 1000) {


            override fun onFinish() {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }

            override fun onTick(p0: Long) {
                println("${p0 / 1000}")
            }
        }.start()
        firstBtn.setOnClickListener {
            saveSwitchState(true)
            val CoffeeIntent = Intent(this, MainActivity::class.java)
            startActivity(CoffeeIntent)
            finish()
        }
    }


    override fun onStop() {
        timer.cancel()
        tts?.shutdown()
        super.onStop()

    }

    override fun onPause() {
        timer.cancel()
        tts?.stop()
        super.onPause()
    }

    override fun onBackPressed() {
        timer.cancel()
        tts?.shutdown()
        this.finishAffinity()           // handle back history, and close entire app

    }

    private fun saveSwitchState(flag: Boolean) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("SWITCH", flag)
        editor.apply()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            print("Begin First Launch!")
            var result = tts!!.setLanguage(Locale("el_GR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
                Toast.makeText(
                    this,
                    "Το κινητό είτε δεν υποστηρίζει ελληνικά στο TextToSpeech είτε πρέπει να επιλέξετε από τις ρυθμίσεις TTS της Google!",
                    Toast.LENGTH_LONG
                ).show()

            } else {
                println("Everything is fine!")
                speakOut()
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }


    private fun speakOut() {

        tts!!.speak(
            "Καλώς ήρθατε. Αν αντιμετωπίζετε δυσκολία στην όραση σας, και θέλετε να λειτουργήσει η εφαρμογή με βοήθεια απο φωνητικές εντολές, πατήστε μία φορά στην οθόνη της συσκευής." +
                    "Διαφορετικά περιμένετε 10 δευτερόλεπτα, για να μεταβείτε στο αρχικό μενού!", //Hello. How could I serve you? Speak after the tone.
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )


    }
}