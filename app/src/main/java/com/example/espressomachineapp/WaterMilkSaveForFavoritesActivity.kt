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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.accept_changes.view.*
import kotlinx.android.synthetic.main.accept_changes.view.cancelBtn
import kotlinx.android.synthetic.main.activity_coffee_maker.*
import kotlinx.android.synthetic.main.activity_water_milk_maker.*
import kotlinx.android.synthetic.main.activity_water_milk_maker.mLSeekBar
import kotlinx.android.synthetic.main.activity_water_milk_maker.milk_select
import kotlinx.android.synthetic.main.activity_water_milk_maker.mlText
import kotlinx.android.synthetic.main.activity_water_milk_maker.mugPicture
import kotlinx.android.synthetic.main.activity_water_milk_maker.water_select
import kotlinx.android.synthetic.main.activity_water_milk_save_for_favorites.*
import kotlinx.android.synthetic.main.alert_box.view.*
import java.util.*

class WaterMilkSaveForFavoritesActivity : AppCompatActivity(), View.OnClickListener,
    TextToSpeech.OnInitListener {
    private val step = 25
    private val MAX = 75
    var currentML = 25
    var currentDrink = 0

    private var RQ_TEMP_CODE = 106
    private val RQ_SPEECH_MENU = 106
    private val RQ_SPEECH_DOSE = 107
    private val RQ_SPEECH_AMOUNT = 108
    private val RQ_SPEECH_SAVE = 109
    private val RQ_SPEECH_NAME_INPUT = 110
    private var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_water_milk_save_for_favorites)

        if(loadSwitch()) {
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

        saveWaterOrMilkBtn.setOnClickListener {
            var OK: Boolean = true
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_box, null)
            builder.setView(dialogLayout)
            val dialog: AlertDialog = builder.create()
            dialog.show()
            dialogLayout.okNameInputBtn.setOnClickListener {
                var text = dialogLayout.coffeeNameInput.text.toString()
                val flag = loadFlagData()
                var drink = ""
                if (flag != true) {
                    if (currentDrink == milk_select.id) {
                        drink = "Milk"
                    } else if (currentDrink == water_select.id) {
                        drink = "Water"
                    }
                } else {
                    if (currentDrink == milk_select.id) {
                        drink = "Γάλα"
                    } else if (currentDrink == water_select.id) {
                        drink = "Νερό"
                    }

                }

                if (text.isEmpty()) {
                    OK = false
                    println("YES")

                    if (flag == true) {
                        Toast.makeText(
                            applicationContext,
                            "You have to give a name!",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Πρέπει να βάλεις κάποιο όνομα!",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }

                }
                if (text.length > 15) {
                    text = text.substring(0, 14)
                }
                var currentPersons = FavoritesActivity.mExampleList?.toArray()
                if (currentPersons != null) {
                    for (person in currentPersons) {
                        if (person.toString()
                                .equals(text + " " + drink + " " + "-" + " " + currentML)
                        ) {
                            val flag2 = loadFlagData()
                            if (flag2 != true) {
                                Toast.makeText(
                                    applicationContext,
                                    "This favorite already exists!",
                                    Toast.LENGTH_LONG
                                ).show()
                                dialog.dismiss()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Αυτό το αγαπημένο υπάρχει ήδη!",
                                    Toast.LENGTH_LONG
                                ).show()
                                dialog.dismiss()
                            }
                            dialog.dismiss()
                            OK = false
                            break
                        }
                    }
                }
                if (OK) {
                    insertItem(text, drink, "-", "$currentML")
                    val go_back = Intent(this, FavoritesActivity::class.java)
                    startActivity(go_back)
                }


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
            if (selectDrinkFavoritesTitle.text.toString() != "Επιλογή ροφήματος"
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
        if (loadFlagData()==true) {
            tts!!.speak(
                "Άλλα. Πείτε μου σας παρακαλώ τι θέλετε να αλλάξουμε; Οι επιλογές είναι. Επιλογή ροφήματος. Ποσότητα. Αποθήκευση. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        } else {
            tts!!.speak(
                "Others. Please tell me what you would like to change? Choices are. Choose Drink. amount. Save. If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
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
                val back = Intent(this, SelectCoffeeForFavorites::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΉ ΡΟΦΉΜΑΤΟΣ" || word.toUpperCase() == "CHOOSE DRINK") {    // GOOGLE CANNOT CAPTURE Δόσεις!
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


            } else if (word.toUpperCase() == "ΠΟΣΌΤΗΤΑ" || word.toUpperCase() == "AMOUNT") {    // GOOGLE CANNOT CAPTURE Δόσεις!
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


            } else if (word.toUpperCase() == "ΑΠΟΘΉΚΕΥΣΗ" || word.toUpperCase() == "SAVE") {
                if (loadFlagData() == true) {
                    var coffeeSelected = if (currentDrink == milk_select.id) "γάλα" else "νερό"
                    tts!!.speak(
                        "Οι επιλογές που κάνατε είναι. Ρόφημα. $coffeeSelected . εμέλ. $currentML . Θέλετε να αποθηκεύσετε αυτό το ρόφημα με αυτές τις επιλογές; Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                } else {
                    var coffeeSelected = if (currentDrink == milk_select.id) "milk" else "water"
                    tts!!.speak(
                        "The choices you have made are. Drink. $coffeeSelected . ml. $currentML . Do you want save this drink with these choices? Please, speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Επιλογή ροφήματος. Ποσότητα. Αποθήκευση. Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. Choose Drink. Amount. Save. Please speak after the tone!", //Hello.
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
            } else if (word.toUpperCase().equals("CHOICES")) {
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
            } else if (loadFlagData()!=true) {
                tts!!.speak(
                    "I couldn't understand, please repeat. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            } else {
                tts!!.speak(
                    "Δεν κατάλαβα, παρακαλώ επαναλάβετε. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            }

        }
        if (requestCode == RQ_SPEECH_SAVE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΝΑΙ" || word.toUpperCase() == "ΝΑΊ" || word.toUpperCase() == "YES") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                RQ_TEMP_CODE = RQ_SPEECH_NAME_INPUT
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Παρακαλώ πείτε μου ένα όνομα για το αγαπημένο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }else{
                    tts!!.speak(
                        "Please, tell me a name for your favorite!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
            } else if (word.toUpperCase() == "ΌΧΙ" || word.toUpperCase() == "NO") {
                RQ_TEMP_CODE = RQ_SPEECH_MENU
                speakOut()

            } else if (loadFlagData()!=true) {
                tts!!.speak(
                    "I couldn't understand, please repeat. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            } else {
                tts!!.speak(
                    "Δεν κατάλαβα, παρακαλώ επαναλάβετε. ", TextToSpeech.QUEUE_FLUSH, null, ""
                )
            }

        }
        if (requestCode == RQ_SPEECH_NAME_INPUT && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            var OK: Boolean = true
            var coffee = if (currentDrink == milk_select.id) "γάλα" else "νερό"
            if (word.length > 15) {
                word = word.substring(0, 14)
            }
            var currentPersons = FavoritesActivity.mExampleList?.toArray()
            if (currentPersons != null) {
                println(word)
                for (person in currentPersons) {
                    if (person.toString()
                            .equals(word + " " + coffee + " " + "-" + " " + currentML)
                    ) {
                        OK = false
                        break
                    }
                }
            }
            if (OK) {
                RQ_TEMP_CODE = 0
                tts!!.speak(
                    "Το ρόφημα αποθηκεύτηκε με όνομα $word !", TextToSpeech.QUEUE_FLUSH, null, ""
                )
                insertItem(word, coffee, "-", "$currentML")
                val go_back = Intent(this, FavoritesActivity::class.java)
                startActivity(go_back)
            } else if (loadFlagData()!=true) {
                RQ_TEMP_CODE = RQ_SPEECH_NAME_INPUT
                tts!!.speak(
                    "Τhis name already exists. Please tell me a new one! ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else {
                RQ_TEMP_CODE = RQ_SPEECH_NAME_INPUT
                tts!!.speak(
                    "Αυτό το όνομα υπάρχει ήδη. Παρακαλώ, πείτε μου ένα νέο όνομα!",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            }

        }
    }
    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(FavoritesActivity.mExampleList)
        editor.putString("favor list", json)
        editor.apply()
    }

    private fun insertItem(value1: String, value2: String, value3: String, value4: String) {
        var newPerson = Person(value1, value2, value3, value4)
        println(newPerson == null)
        if (FavoritesActivity.mExampleList != null && newPerson != null)
            FavoritesActivity.mExampleList?.add(newPerson)
        saveData()
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
        val SelectCoffeeIntent = Intent(this, SelectCoffeeForFavorites::class.java)
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