package com.example.espressomachineapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stock_products.*
import kotlinx.android.synthetic.main.activity_stores.*
import java.util.*

class StoresActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val RQ_SPEECH_REC = 104
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_stores)

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

        mapsBtn.setOnClickListener {
            var query = if (loadFlagData() == true) "καφεκοπτείο" else "coffee shop"
            val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            mapIntent.resolveActivity(packageManager)?.let {
                startActivity(mapIntent)
            }
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

    fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
//
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

        if (loadFlagData() == true) {
            tts!!.speak(
                "Πείτε καταστήματα για να ακούσετε τα πιο δημοφιλή καφεκοπτεία ή πείτε χάρτες για να εμφανίσετε τα κοντινότερα, στο Google maps. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        } else {
            tts!!.speak(
                "Say stores to hear the most famous coffee brewers or say maps to be directed to google maps. If you want to go back, say go back! Speak after the tone!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }

    }

    var word = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΠΊΣΩ" || word.toUpperCase() == "GO BACK") {
                val back = Intent(this, StockProductsActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ΚΑΤΑΣΤΉΜΑΤΑ" || word.toUpperCase() == "STORES") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Τα πιο δημοφιλή καφεκοπτεία είναι. Λουμίδης. Αιόλου 106 Αθήνα .Τηλέφωνο: 2103214426 . Coffee Island. Πανεπιστημίου 39 Αθήνα. Τηλέφωνο: 2112147559. " +
                                "Κάγια . Βουλής 7 Αθήνα. Τηλέφωνο: 2130284305", //Hello. How could I serve you? Speak after the tone.
                        TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                } else {
                    tts!!.speak(
                        "The most famous coffee brewers are.Loumidi . Eolou 106 Athens. Coffee Island. Panepistimiou 39 Athens." +
                                "Kaya.Voulis 7 Athens.", //Hello. How could I serve you? Speak after the tone.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
            } else if (word.toUpperCase() == "ΧΆΡΤΕΣ" || word.toUpperCase() == "MAPS") {
                var query = if (loadFlagData() == true) "καφεκοπτείο" else "coffee shop"
                val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                mapIntent.resolveActivity(packageManager)?.let {
                    startActivity(mapIntent)
                }

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

                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            } else {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR")
            }

            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say sth")
            startActivityForResult(i, RQ_SPEECH_REC)

        }
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
        val StockProductsIntent = Intent(this, StockProductsActivity::class.java)
        startActivity(StockProductsIntent)
    }
}