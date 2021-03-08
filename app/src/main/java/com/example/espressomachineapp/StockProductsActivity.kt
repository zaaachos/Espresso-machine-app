package com.example.espressomachineapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_stock_products.*
import me.itangqi.waveloadingview.WaveLoadingView
import java.util.*

class StockProductsActivity : AppCompatActivity(), View.OnClickListener,
    TextToSpeech.OnInitListener {

    private val RQ_SPEECH_REC = 104
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_stock_products)

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

        goMonthlyConsumptionBtn.setOnClickListener(this)
        buyCoffeeBtn.setOnClickListener(this)

        val WATER_USED = loadWaterUsed()
        val COFFEE_USED = loadCoffeeUsed()
        println(loadCurrentWater())

        waveLoadingView1.progressValue = loadCurrentWater()
        waveLoadingView2.progressValue = loadCurrentCoffee()

        if (waveLoadingView1.progressValue - WATER_USED < 0) {
            waveLoadingView1.progressValue = 0
            saveCurrentWater(waveLoadingView1.progressValue)
            saveWaterUsed(0)
        } else {
            waveLoadingView1.progressValue -= WATER_USED
            saveCurrentWater(waveLoadingView1.progressValue)
            saveWaterUsed(0)
        }

        if (waveLoadingView2.progressValue - COFFEE_USED < 0) {
            waveLoadingView2.progressValue = 0
            saveCurrentCoffee(waveLoadingView2.progressValue)
            saveCoffeeUsed(0)
        } else {
            waveLoadingView2.progressValue -= COFFEE_USED
            saveCurrentCoffee(waveLoadingView2.progressValue)
            saveCoffeeUsed(0)
        }

        makeWaves(waveLoadingView1)
        val myBlue = Color.argb(255, 2, 190, 196)
        val myKafe = Color.argb(255, 82, 34, 0)
        waveLoadingView1.waveColor = myBlue
        waveLoadingView2.waveColor = myKafe
        handleTitleWaves(waveLoadingView1)
        handleTitleWaves(waveLoadingView2)

    }

    private fun saveWaterUsed(water: Int) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("WATER_USED", water)
        editor.apply()
    }

    private fun loadWaterUsed(): Int {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedWaterUsed = sharedPreferences.getInt("WATER_USED", 0)
        return savedWaterUsed

    }

    private fun saveCoffeeUsed(coffee: Int) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("COFFEE_USED", coffee)
        editor.apply()
    }

    private fun loadCoffeeUsed(): Int {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedWaterUsed = sharedPreferences.getInt("COFFEE_USED", 0)
        return savedWaterUsed

    }

    private fun saveCurrentWater(water: Int) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("CURRENT_WATER", water)
        editor.apply()
    }

    private fun loadCurrentWater(): Int {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedWaterUsed = sharedPreferences.getInt("CURRENT_WATER", 75)
        println(savedWaterUsed)
        return savedWaterUsed

    }

    private fun saveCurrentCoffee(coffee: Int) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("CURRENT_COFFEE", coffee)
        editor.apply()
    }

    private fun loadCurrentCoffee(): Int {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedWaterUsed = sharedPreferences.getInt("CURRENT_COFFEE", 60)
        return savedWaterUsed

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
                "Η μηχανή μου! Πείτε μου τι θα θέλατε ή πείτε επιλογές για να ακούσετε τις επιλογές. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Mιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "My machine! Tell me what you would like, or say choices to hear the possible choices . If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
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
                val back = Intent(this, MainActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ΣΤΆΘΜΕΣ" || word.toUpperCase() == "LEVELS" || word.toUpperCase() == "ΣΤΑΘΜΕΣ") {
                if (loadFlagData() == true) {
                    var speaker = "Οι στάθμες αυτή τη στιγμή. "
                    var water_percentage = waveLoadingView1.progressValue
                    print(water_percentage)
                    var coffee_percentage = waveLoadingView2.progressValue
                    var water = "Νερό. $water_percentage % ."
                    var coffee = "Καφές. $coffee_percentage % ."
                    var result = speaker + water + coffee
                    tts!!.speak(
                        result, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                } else {
                    var speaker = "Levels right now. "
                    var water_percentage = waveLoadingView1.progressValue
                    print(water_percentage)
                    var coffee_percentage = waveLoadingView2.progressValue
                    var water = "Water. $water_percentage % ."
                    var coffee = "Coffee. $coffee_percentage % ."
                    var result = speaker + water + coffee
                    tts!!.speak(
                        result, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                }

            } else if (word.toUpperCase() == "ΑΓΟΡΆ ΚΑΦΈ" || word.toUpperCase() == "BUY COFFEE") {
                val StoresIntent = Intent(this, StoresActivity::class.java)
                startActivity(StoresIntent)
            } else if (word.toUpperCase() == "ΚΑΤΑΝΆΛΩΣΗ" || word.toUpperCase() == "CONSUMPTION") {
                val MonthlyConsumptionPage =
                    Intent(this, CoffeeMonthlyConsumptionActivity::class.java)
                startActivity(MonthlyConsumptionPage)
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Στάθμες. Αγορά Καφέ. Κατανάλωση. ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Levels. Buy Coffee. Consumption. ",
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
        val MainIntent = Intent(this, MainActivity::class.java)
        startActivity(MainIntent)
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

    override fun onClick(v: View) {
        if (v.id == goMonthlyConsumptionBtn.id) {
            val MonthlyConsumptionPage = Intent(this, CoffeeMonthlyConsumptionActivity::class.java)
            startActivity(MonthlyConsumptionPage)
        } else if (v.id == buyCoffeeBtn.id) {
            val StoresIntent = Intent(this, StoresActivity::class.java)
            startActivity(StoresIntent)
        }

    }

    fun makeWaves(wave: WaveLoadingView) {
        wave.setAnimDuration(4000);
        wave.pauseAnimation();
        wave.resumeAnimation();
        wave.cancelAnimation();
        wave.startAnimation();

    }

    fun handleTitleWaves(wave: WaveLoadingView) {
        var capacity = wave.progressValue
        if (capacity >= 50) {
            wave.setTopTitle("")
            wave.setBottomTitle("$capacity%")
            wave.setBottomTitleSize(35F)
            wave.bottomTitleColor = Color.argb(255, 255, 255, 255)
        } else {
            wave.setTopTitle("$capacity%")
            wave.setBottomTitle("")
            wave.setTopTitleSize(35F)
            wave.topTitleColor = Color.argb(255, 0, 0, 0)


        }

    }
}