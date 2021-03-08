package com.example.espressomachineapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val RQ_SPEECH_REC = 102
    private var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }

        setContentView(R.layout.activity_main)

        if (loadSwitch()) {

            tts = TextToSpeech(this, this)
            tts?.setSpeechRate(0.85f)
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                override fun onDone(utteranceId: String?) {
                    //do whatever you want when TTS finish speaking.
                    println("DONE SPEAKING!")
                    inputSpeech()
                }

                override fun onError(utteranceId: String?) {
                    //do whatever you want if TTS makes an error.
                }

                override fun onStart(utteranceId: String?) {
                    //do whatever you want when TTS start speaking.
                }
            })
        }


        // Settings Button Clicked
        settingsBtn.setOnClickListener {
            println("You clicked Settings Button!")
            val SettingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(SettingsIntent)

        }
        // MyProducts Button Clicked
        myProductsBtn.setOnClickListener {
            println("You clicked My Products Button!")
            val myProductsIntent = Intent(this, StockProductsActivity::class.java)
            startActivity(myProductsIntent)

        }
        // Coffee Button Clicked
        selectCoffeeBtn.setOnClickListener {
            println("You clicked Coffee Button!")
            val CoffeeIntent = Intent(this, SelectCoffeeActivity::class.java)
            startActivity(CoffeeIntent)

        }

        favouritesBtn.setOnClickListener {
            println("You clicked Favorites Button!")
            val FavoritesIntent = Intent(this, FavoritesActivity::class.java)
            startActivity(FavoritesIntent)

        }


    }

    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

    }

    private fun loadFlagData(): Boolean? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("FLAG", true)
        return savedFlag

    }

    override fun onStop() {
        tts?.shutdown()
        super.onStop()

    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }

    override fun onBackPressed() {
        tts?.shutdown()
        this.finishAffinity()           // handle back history, and close entire app

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
                "Γειά σας. Πείτε μου τι θα θέλατε ή πείτε επιλογές για να ακούσετε τις επιλογές. Μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Hello. Tell me what you would like, or say choices to hear the possible choices. Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )

    }

    var word = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)

            if (word.equals(textView3.text.toString(), ignoreCase = true)) {
                val CoffeeIntent = Intent(this, SelectCoffeeActivity::class.java)
                startActivity(CoffeeIntent)
            } else if (word.equals(textView4.text.toString(), ignoreCase = true)) {
                val FavoritesIntent = Intent(this, FavoritesActivity::class.java)
                startActivity(FavoritesIntent)
            } else if (word.equals(textView5.text.toString(), ignoreCase = true)) {
                val myProductsIntent = Intent(this, StockProductsActivity::class.java)
                startActivity(myProductsIntent)
            } else if (word.equals(textView6.text.toString(), ignoreCase = true)) {
                val SettingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(SettingsIntent)
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Καφές. Αγαπημένα. Ρυθμίσεις. Η μηχανή μου. ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Coffee , favorites , settings , my machine. ",
                    TextToSpeech.QUEUE_FLUSH,

                    null,
                    ""
                )
            } else if (loadFlagData() != true) {
                tts!!.speak(
                    "I couldn't understand, please repeat. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            } else {
                tts!!.speak(
                    "Δεν κατάλαβα, παρακαλώ επαναλάβετε. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            }

        }
    }


    private fun inputSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Not available speech recognition", Toast.LENGTH_SHORT).show()

        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            if (loadFlagData() != true) {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")
            } else {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR")
            }

            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say sth")
            startActivityForResult(i, RQ_SPEECH_REC)

        }
    }


    fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

    }


}