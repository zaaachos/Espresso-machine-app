package com.example.espressomachineapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_select_coffee.*
import java.util.*

class SelectCoffeeForFavorites : AppCompatActivity(), View.OnClickListener,
    TextToSpeech.OnInitListener {
    var coffeeSelected: String = ""
    private val RQ_SPEECH_REC = 103
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }

        setContentView(R.layout.activity_select_coffee_for_favorites)
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

        esspressoBtn.setOnClickListener(this)
        capuccinoBtn.setOnClickListener(this)
        macchiatoBtn.setOnClickListener(this)
        latteBtn.setOnClickListener(this)
        americanoBtn.setOnClickListener(this)
        othersBtn.setOnClickListener(this)


    }

    private fun loadFlagData(): Boolean? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("FLAG", true)
        return savedFlag

    }

    override fun onClick(v: View) {
        if (v.id == esspressoBtn.id) {
            coffeeSelected = "ESPRESSO"
            val CoffeeMakerActivityIntent = Intent(this, CoffeeMakerForSavingActivity::class.java)
            saveData(coffeeSelected)
            startActivity(CoffeeMakerActivityIntent)
        } else if (v.id == capuccinoBtn.id) {
            coffeeSelected = "CAPPUCCINO"
            val CoffeeMakerActivityIntent = Intent(this, CoffeeMakerForSavingActivity::class.java)
            saveData(coffeeSelected)
            startActivity(CoffeeMakerActivityIntent)
        } else if (v.id == macchiatoBtn.id) {
            coffeeSelected = "MACCHIATO"
            val CoffeeMakerActivityIntent = Intent(this, CoffeeMakerForSavingActivity::class.java)
            saveData(coffeeSelected)
            startActivity(CoffeeMakerActivityIntent)
        } else if (v.id == latteBtn.id) {
            coffeeSelected = "LATTE"
            val CoffeeMakerActivityIntent = Intent(this, CoffeeMakerForSavingActivity::class.java)
            saveData(coffeeSelected)
            startActivity(CoffeeMakerActivityIntent)
        } else if (v.id == americanoBtn.id) {
            coffeeSelected = "AMERICANO"
            val CoffeeMakerActivityIntent = Intent(this, CoffeeMakerForSavingActivity::class.java)
            saveData(coffeeSelected)
            startActivity(CoffeeMakerActivityIntent)
        } else if (v.id == othersBtn.id) {
            val MilkMakerActivityIntent =
                Intent(this, WaterMilkSaveForFavoritesActivity::class.java)
            startActivity(MilkMakerActivityIntent)
        }

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


    private fun inputSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Not available speech recognition", Toast.LENGTH_SHORT).show()

        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            if (textMenuId.text.toString() != "Τι καφέ θα θέλατε;") {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            } else {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR")
            }

            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say sth")
            startActivityForResult(i, RQ_SPEECH_REC)

        }
    }

    private fun speakOut() {

        if (loadFlagData() == true)
            tts!!.speak(
                "Τι ρόφημα θέλετε να αποθηκεύσετε; Αν θέλετε να ακούσετε τις επιλογές, πείτε επιλογές! Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Please tell me what would you like to save or say choices to hear the possible choices . If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
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
            //title2.text = textView3.text.toString().toUpperCase()
            if (word.toUpperCase() == "ΠΊΣΩ" || word.toUpperCase() == "GO BACK") {
                val back = Intent(this, FavoritesActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ESPRESSO" || word.toUpperCase() == "ΕΣΠΡΈΣΟ" || word.toUpperCase() == "ΕΣΠΡΈΣΣΟ") {
                coffeeSelected = "ESPRESSO"
                val CoffeeMakerActivityIntent =
                    Intent(this, CoffeeMakerForSavingActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "CAPPUCCINO" || word.toUpperCase() == "ΚΑΠΟΥΤΣΊΝΟ"
            ) {
                coffeeSelected = "CAPPUCCINO"
                val CoffeeMakerActivityIntent =
                    Intent(this, CoffeeMakerForSavingActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "MACCHIATO" || word.toUpperCase() == "ΜΑΚΙΆΤΟ"
            ) {
                coffeeSelected = "MACCHIATO"
                val CoffeeMakerActivityIntent =
                    Intent(this, CoffeeMakerForSavingActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "LATTE" || word.toUpperCase() == "ΛΆΤΕ") {
                coffeeSelected = "LATTE"
                val CoffeeMakerActivityIntent =
                    Intent(this, CoffeeMakerForSavingActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "AMERICANO" || word.toUpperCase() == "ΑΜΕΡΙΚΆΝΟ"
            ) {
                coffeeSelected = "AMERICANO"
                val CoffeeMakerActivityIntent =
                    Intent(this, CoffeeMakerForSavingActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "OTHERS" || word.toUpperCase() == "ΆΛΛΑ" || word.toUpperCase() == "ΑΛΛΆ") {
                coffeeSelected = "OTHERS"
                val CoffeeMakerActivityIntent =
                    Intent(this, WaterMilkSaveForFavoritesActivity::class.java)
                saveData(coffeeSelected)
                startActivity(CoffeeMakerActivityIntent)
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Espresso, καπουτσίνο, μακιάτο, λάτε, αμερικάνο, άλλα.", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Espresso, cappuccino, macchiato, latte, americano, others. ", //Hello.
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


    private fun saveData(coffee: String) {
        val TEXT = coffee
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("FAVOR", TEXT)
        }.apply()
        println("I STORE $TEXT")
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
        val FavoritesIntent = Intent(this, FavoritesActivity::class.java)
        startActivity(FavoritesIntent)
    }

    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

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

