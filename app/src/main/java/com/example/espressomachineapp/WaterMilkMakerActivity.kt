package com.example.espressomachineapp

import android.app.Activity
import android.app.AlertDialog
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
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.accept_changes.view.*
import kotlinx.android.synthetic.main.activity_coffee_maker.*
import kotlinx.android.synthetic.main.activity_coffee_maker.mLSeekBar
import kotlinx.android.synthetic.main.activity_coffee_maker.mlText
import kotlinx.android.synthetic.main.activity_coffee_maker.mugPicture
import kotlinx.android.synthetic.main.activity_water_milk_maker.*
import java.util.*

class WaterMilkMakerActivity : AppCompatActivity(), View.OnClickListener,
    TextToSpeech.OnInitListener {
    private val step = 25
    private val MAX = 75
    var currentML = 25
    var currentDrink = 0

    private var RQ_TEMP_CODE = 106
    private val RQ_SPEECH_MENU = 106
    private val RQ_SPEECH_DOSE = 107
    private val RQ_SPEECH_AMOUNT = 108
    private val RQ_SPEECH_START = 109
    private var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_water_milk_maker)

        if (loadSwitch()) {
            tts = TextToSpeech(this, this)
            tts?.setSpeechRate(0.85f)
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                override fun onDone(utteranceId: String?) {
                    //do whatever you want when TTS finish speaking.
                    println("DONE SPEAKING!")
                    inputSpeech(RQ_TEMP_CODE)
                }

                override fun onError(utteranceId: String?) {
                    //do whatever you want if TTS makes an error.
                }

                override fun onStart(utteranceId: String?) {
                    //do whatever you want when TTS start speaking.
                }
            })
        }

        water_select.setOnClickListener(this)
        milk_select.setOnClickListener(this)

        milkwaterBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.accept_changes, null)
            builder.setView(dialogLayout)

            val dialog: AlertDialog = builder.create()
            dialog.show()
            var messageDose = R.string.water
            if (currentDrink == milk_select.id) {
                messageDose = R.string.milk
            }

            dialogLayout.rofimaText.setText(messageDose)
            dialogLayout.dosiText.setText("-")
            dialogLayout.mlTextId.setText("$currentML")
            dialogLayout.OKBtn.setOnClickListener {
                if (currentDrink == water_select.id) {
                    val waterUsedTillNow = loadWaterUsed()
                    if (currentML == 25)
                        saveWaterUsed(waterUsedTillNow + 5)
                    else if (currentML == 50)
                        saveWaterUsed(waterUsedTillNow + 10)
                    else if (currentML == 75)
                        saveWaterUsed(waterUsedTillNow + 15)
                }
                val BrewCoffee = Intent(this, CoffeeLoadingScreen::class.java)
                startActivity(BrewCoffee)
            }
            dialogLayout.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }


        }

        currentDrink = water_select.id

        mLSeekBar.max = MAX / step
        mLSeekBar.progress = 1
        mLSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                currentML = progress * step
                changeMugPicture(currentML, currentDrink)
                mlText.setText(currentML.toString() + " mL")


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (currentML < 25) {                        // handle zero mL
                    mLSeekBar.progress = 1
                    Toast.makeText(
                        applicationContext,
                        "Δεν μπορείς να βάλεις 0 mL νερό/γάλα στο ποτήρι!",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        })

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


    private fun inputSpeech(CODE: Int) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Not available speech recognition", Toast.LENGTH_SHORT).show()

        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            if (selectDrinkTitle.text.toString() != "Επιλογή ροφήματος"
            ) {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            } else {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR")
            }

            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say sth")
            startActivityForResult(i, CODE)

        }
    }

    private fun speakOut() {
        if (loadFlagData() == true) {
            tts!!.speak(
                "Άλλα. Πείτε μου σας παρακαλώ τι θέλετε να αλλάξουμε; Οι επιλογές είναι. Επιλογή ροφήματος. Ποσότητα. Έναρξη. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        } else {
            tts!!.speak(
                "Others. Please tell me what you would like to change? Choices are. Choose Drink. amount. Start. If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }
    }


    var word = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_MENU && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΠΊΣΩ" || word.toUpperCase() == "GO BACK") {
                val back = Intent(this, SelectCoffeeActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΉ ΡΟΦΉΜΑΤΟΣ" || word.toUpperCase() == "CHOOSE DRINK") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Οι επιλογές για τσ ροφήματα είναι γάλα. νερό. Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_DOSE
                } else {
                    tts!!.speak(
                        "The choices for drink are. Milk. Water. Please speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_DOSE
                }


            } else if (word.toUpperCase() == "ΠΟΣΌΤΗΤΑ" || word.toUpperCase() == "AMOUNT") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Οι επιλογές για την ποσότητα είναι 25. 50. 75. Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_AMOUNT
                } else {
                    tts!!.speak(
                        "The choices for amount are. 25. 50. 75. Please speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_AMOUNT

                }


            } else if (word.toUpperCase() == "ΈΝΑΡΞΗ" || word.toUpperCase() == "START") {
                if (loadFlagData() == true) {
                    var coffeeSelected = if (currentDrink == milk_select.id) "γάλα" else "νερό"
                    tts!!.speak(
                        "Οι επιλογές που κάνατε είναι. Ρόφημα. $coffeeSelected . εμέλ. $currentML . Θέλετε να ξεκινήσει η προετοιμασία με αυτές τις επιλογές; Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_START
                } else {
                    var coffeeSelected = if (currentDrink == milk_select.id) "milk" else "water"
                    tts!!.speak(
                        "The choices you have made are. Drink. $coffeeSelected . ml. $currentML . Do you want to start with these choices? Please, speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_START
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Επιλογή ροφήματος. Ποσότητα. Έναρξη. Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Choose Drink. Amount. Start. Please speak after the tone!", //Hello.
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
        if (requestCode == RQ_SPEECH_DOSE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΓΆΛΑ" || word.toUpperCase() == "MILK") {
                milk_select.setBackgroundResource(R.drawable.linearborder)
                water_select.setBackgroundResource(android.R.color.transparent)
                currentDrink = milk_select.id
                changeMugPicture(currentML, currentDrink)
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Το ρόφημα άλλαξε σε γάλα! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "The drink has been set to milk! What else would you like to change? Please, speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                    RQ_TEMP_CODE = RQ_SPEECH_MENU

                }


            } else if (word.toUpperCase() == "ΝΕΡΌ" || word.toUpperCase() == "WATER") {
                water_select.setBackgroundResource(R.drawable.linearborder)
                milk_select.setBackgroundResource(android.R.color.transparent)
                currentDrink = water_select.id
                changeMugPicture(currentML, currentDrink)
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Το ρόφημα άλλαξε σε νερό! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "The drink has been set to water! What else would you like to change? Please, speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                    RQ_TEMP_CODE = RQ_SPEECH_MENU

                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές για τα ροφήματα είναι γάλα και νερό! Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices for drinks are milk and water. Please speak after the tone!", //Hello.
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
        if (requestCode == RQ_SPEECH_AMOUNT && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "25") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η ποσότητα άλλαξε σε 25 εμέλ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 1
                    currentML = 25
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "Amount has been set to 25! What would you like to change? Speak, after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 1
                    currentML = 25
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "50" || word.toUpperCase() == "FIFTY") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η ποσότητα άλλαξε σε 50 εμέλ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 2
                    currentML = 50
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "Amount has been set to 50! What would you like to change? Speak, after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 2
                    currentML = 50
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU

                }


            } else if (word.toUpperCase() == "75") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η ποσότητα άλλαξε σε 75 εμ ελ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 3
                    currentML = 75
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "Amount has been set to 75! What would you like to change? Speak, after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 3
                    currentML = 75
                    changeMugPicture(currentML, currentDrink)
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές για την ποσότητα είναι 25. 50. 75. Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Shots. Amount. Start. Please speak after the tone!", //Hello.
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
        if (requestCode == RQ_SPEECH_START && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΝΑΙ" || word.toUpperCase() == "ΝΑΊ" || word.toUpperCase() == "YES") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                if (currentDrink == water_select.id) {
                    val waterUsedTillNow = loadWaterUsed()
                    if (currentML == 25)
                        saveWaterUsed(waterUsedTillNow + 5)
                    else if (currentML == 50)
                        saveWaterUsed(waterUsedTillNow + 10)
                    else if (currentML == 75)
                        saveWaterUsed(waterUsedTillNow + 15)
                }
                val BrewCoffee = Intent(this, CoffeeLoadingScreen::class.java)
                startActivity(BrewCoffee)
            } else if (word.toUpperCase() == "ΌΧΙ" || word.toUpperCase() == "NO") {
                RQ_TEMP_CODE = RQ_SPEECH_MENU
                speakOut()

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

    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

    }

    fun changeMugPicture(mL: Int, id: Int) {
        if (id == milk_select.id) {
            when (mL) {
                25 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.milk25,
                        null
                    )
                )
                50 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.milk50,
                        null
                    )
                )
                75 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.milk75,
                        null
                    )
                )
            }
        } else if (id == water_select.id) {
            when (mL) {
                25 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.water25,
                        null
                    )
                )
                50 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.water50,
                        null
                    )
                )
                75 -> mugPicture.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.water75,
                        null
                    )
                )
            }
        }
    }

    override fun onBackPressed() {
        tts?.shutdown()
        val SelectCoffeeIntent = Intent(this, SelectCoffeeActivity::class.java)
        startActivity(SelectCoffeeIntent)
    }

    override fun onStop() {
        tts?.shutdown()
        super.onStop()

    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
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

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == water_select.id) {
                water_select.setBackgroundResource(R.drawable.linearborder)
                milk_select.setBackgroundResource(android.R.color.transparent)
                currentDrink = water_select.id
                changeMugPicture(currentML, currentDrink)

            } else if (v.id == milk_select.id) {
                milk_select.setBackgroundResource(R.drawable.linearborder)
                water_select.setBackgroundResource(android.R.color.transparent)
                currentDrink = milk_select.id
                changeMugPicture(currentML, currentDrink)

            }
        }

    }
}