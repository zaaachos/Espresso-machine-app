package com.example.espressomachineapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.accept_changes.view.*
import kotlinx.android.synthetic.main.activity_coffee_maker.*
import java.util.*

class CoffeeMakerActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val step = 25
    private val MAX = 75
    var currentML = 25
    var currentDose = 0
    lateinit var messageDose: String
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
        setContentView(R.layout.activity_coffee_maker)

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

        var coffeeSelected = loadDataString()
        SelectedCoffeeText.setText(coffeeSelected)

        val langSelected = loadFlagData()

        currentDose = singleMeasureBtn.id
        handleSelections()

        startBrewBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.accept_changes, null)
            builder.setView(dialogLayout)

            val dialog: AlertDialog = builder.create()
            dialog.show()

            if (currentDose == singleMeasureBtn.id) {
                print("YES")
                if (langSelected != true) {
                    messageDose = "Single"
                } else {
                    messageDose = "Μονή"
                }
            } else if (currentDose == doubleMeasureBtn.id) {
                if (langSelected != true) {
                    messageDose = "Double"
                } else {
                    messageDose = "Διπλή"
                }
            }

            dialogLayout.rofimaText.setText(coffeeSelected)
            dialogLayout.dosiText.setText(messageDose)
            dialogLayout.mlTextId.setText("$currentML")
            dialogLayout.OKBtn.setOnClickListener {
                val waterUsedTillNow = loadWaterUsed()
                val coffeeUsedTillNow = loadCoffeeUsed()
                if (currentML == 25)
                    saveWaterUsed(waterUsedTillNow + 5)
                else if (currentML == 50)
                    saveWaterUsed(waterUsedTillNow + 10)
                else if (currentML == 75)
                    saveWaterUsed(waterUsedTillNow + 15)

                if (currentDose == singleMeasureBtn.id)
                    saveCoffeeUsed(coffeeUsedTillNow + 4)
                else if (currentDose == doubleMeasureBtn.id)
                    saveCoffeeUsed(coffeeUsedTillNow + 8)

                val BrewCoffee = Intent(this, CoffeeLoadingScreen::class.java)
                startActivity(BrewCoffee)
            }
            dialogLayout.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

        }

        mLSeekBar.max = MAX / step
        mLSeekBar.progress = 1
        mLSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                currentML = progress * step
                changeMugPicture(currentML)
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
                        "Δεν μπορείς να βάλεις 0 mL νερό στον καφέ σου!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (currentDose == doubleMeasureBtn.id) {
                    if (currentML < 50) {
                        mLSeekBar.progress = 2
                        Toast.makeText(
                            applicationContext,
                            "Δεν μπορείς να βάλεις 25 mL νερό με ΔΙΠΛΗ ΔΟΣΗ!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
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
            if (howManyShotsTitle.text.toString() != "Δόση"
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
            var title = coffeeNameTranslation(SelectedCoffeeText.text.toString())
            tts!!.speak(
                "$title. Πείτε μου σας παρακαλώ τι θέλετε να αλλάξουμε; Οι επιλογές είναι. Δόσεις. Ποσότητα. Έναρξη.Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        } else {
            var title = SelectedCoffeeText.text
            tts!!.speak(
                "$title. Please tell me what you would like to change? Choices are shots. amount. Start.If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        }
    }

    private fun coffeeNameTranslation(englishName: String): String {
        if (!loadFlagData()!!)
            return englishName
        else if (englishName.toUpperCase().equals("ESPRESSO"))
            return "Εσπρέσσο"
        else if (englishName.toUpperCase().equals("CAPPUCCINO"))
            return "Καπουτσίνο"
        else if (englishName.toUpperCase().equals("MACCHIATO"))
            return "Μακιάτο"
        else if (englishName.toUpperCase().equals("LATTE"))
            return "Λάτε"
        else if (englishName.toUpperCase().equals("AMERICANO"))
            return "Αμερικάνο"
        else
            return "Άκυρο"

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
            } else if (word.toUpperCase() == "ΔΏΣΕΙΣ" || word.toUpperCase() == "SHOTS") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Οι επιλογές για τις δόσεις είναι μονή. διπλή. Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_DOSE
                } else {
                    tts!!.speak(
                        "The choices for shots are. Single. Double. Please, speak after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_DOSE
                }


            } else if (word.toUpperCase() == "ΠΟΣΌΤΗΤΑ" || word.toUpperCase() == "AMOUNT") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                if (currentDose == singleMeasureBtn.id) {
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
                } else if (currentDose == doubleMeasureBtn.id) {
                    if (loadFlagData() == true) {
                        tts!!.speak(
                            "Οι επιλογές για την ποσότητα, στην διπλή δόση, είναι 50 ή 75. Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                        RQ_TEMP_CODE = RQ_SPEECH_AMOUNT
                    } else {
                        tts!!.speak(
                            "The choices for amount in double shot are. 50. 75. Please speak after the tone!", //Hello.
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                        RQ_TEMP_CODE = RQ_SPEECH_AMOUNT
                    }
                }


            } else if (word.toUpperCase() == "ΈΝΑΡΞΗ" || word.toUpperCase() == "START") {
                var coffeeSelected = coffeeNameTranslation(loadDataString().toString())
                val langSelected = loadFlagData()
                if (currentDose == singleMeasureBtn.id) {
                    if (langSelected != true) {
                        messageDose = "Single"
                    } else {
                        messageDose = "Μονή"
                    }
                } else if (currentDose == doubleMeasureBtn.id) {
                    if (langSelected != true) {
                        messageDose = "Double"
                    } else {
                        messageDose = "Διπλή"
                    }
                }
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Οι επιλογές που κάνατε είναι.  $coffeeSelected .  $messageDose Δόση , $currentML εμέλ. Θέλετε να ξεκινήσει η προετοιμασία με αυτές τις επιλογές; Παρακαλώ μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_START
                } else {
                    tts!!.speak(
                        "The choices you made are. $coffeeSelected , $messageDose Shot ,  $currentML ml. Do you want to brew this drink? Speak after the ton!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    RQ_TEMP_CODE = RQ_SPEECH_START
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Δόσεις. Ποσότητα. Έναρξη. Μιλήστε μετά τον ήχο!", //Hello.
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
        if (requestCode == RQ_SPEECH_DOSE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)

            if (word.toUpperCase() == "ΜΌΝΗ" || word.toUpperCase() == "ΜΟΝΉ" || word.toUpperCase() == "SINGLE") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η δόση άλλαξε σε μονή!  Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    selectionsId.check(singleMeasureBtn.id)
                    currentDose = singleMeasureBtn.id
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "The shot has been set to single. What would you like to change? Speak, after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    selectionsId.check(singleMeasureBtn.id)
                    currentDose = singleMeasureBtn.id
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "ΔΙΠΛΉ" || word.toUpperCase() == "ΔΊΠΛΗ" || word.toUpperCase() == "DOUBLE") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η δόση άλλαξε σε διπλή!  Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    selectionsId.check(doubleMeasureBtn.id)
                    currentDose = doubleMeasureBtn.id
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                } else {
                    tts!!.speak(
                        "The shot has been set to double. What would you like to change? Speak, after the tone!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    selectionsId.check(doubleMeasureBtn.id)
                    currentDose = doubleMeasureBtn.id
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές για τις δόσεις είναι μία ή δύο! Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices for shots are one or two.", //Hello.
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


            if (word.toUpperCase() == "25") {
                if (currentDose == singleMeasureBtn.id) {
                    if (loadFlagData() == true) {
                        tts!!.speak(
                            "Η ποσότητα άλλαξε σε 25 εμέλ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                        mLSeekBar.progress = 1
                        currentML = 25
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
                        RQ_TEMP_CODE = RQ_SPEECH_MENU
                    }
                } else if (currentDose == doubleMeasureBtn.id) {
                    if (loadFlagData() == true) {
                        tts!!.speak(
                            "Η ποσότητα στην διπλή δόση δεν μπορεί να είναι 25 εμέλ. Μπορείτε να επιλέξετε ανάμεσα σε 50 και 75. Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                        RQ_TEMP_CODE = RQ_SPEECH_AMOUNT
                    } else {
                        tts!!.speak(
                            "The amount cannot been set to 25. You could choose between 50 and 75. Speak after the tone!", //Hello.
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                        RQ_TEMP_CODE = RQ_SPEECH_AMOUNT
                    }

                }


            } else if (word.toUpperCase() == "50" || word.toUpperCase() == "FIFTY") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η ποσότητα άλλαξε σε 50 εμέλ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 2
                    currentML = 50
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
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "75") {
                if (loadFlagData() == true) {
                    tts!!.speak(
                        "Η ποσότητα άλλαξε σε 75 εμέλ! Τί άλλο θα θέλατε να αλλάξουμε; Παρακαλώ, μιλήστε μετά τον ήχο!", //Hello.
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    mLSeekBar.progress = 3
                    currentML = 75
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
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                }


            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές για την ποσότητα είναι 25. 50. 75. Μιλήστε μετά τον ήχο!", //Hello.
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase().equals("CHOICES")) {
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

            if (word.toUpperCase() == "ΝΑΊ" || word.toUpperCase() == "ΝΑΙ" || word.toUpperCase() == "YES") {    // GOOGLE CANNOT CAPTURE Δόσεις!
                val waterUsedTillNow = loadWaterUsed()
                val coffeeUsedTillNow = loadCoffeeUsed()
                if (currentML == 25)
                    saveWaterUsed(waterUsedTillNow + 5)
                else if (currentML == 50)
                    saveWaterUsed(waterUsedTillNow + 10)
                else if (currentML == 75)
                    saveWaterUsed(waterUsedTillNow + 15)

                if (currentDose == singleMeasureBtn.id)
                    saveCoffeeUsed(coffeeUsedTillNow + 4)
                else if (currentDose == doubleMeasureBtn.id)
                    saveCoffeeUsed(coffeeUsedTillNow + 8)
                val BrewCoffee = Intent(this, CoffeeLoadingScreen::class.java)
                startActivity(BrewCoffee)
            } else if (word.toUpperCase().equals("ΌΧΙ") || word.toUpperCase() == "NO") {
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


    private fun loadDataString(): String? {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedString = sharedPreferences.getString("STRING_KEY", null)
        return savedString

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

    private fun handleSelections() {
        selectionsId.setOnCheckedChangeListener { group, checkedId ->
            val rb: RadioButton = findViewById(checkedId)
            if (rb != null) {
                when (rb.id) {
                    singleMeasureBtn.id -> {
                        currentDose = singleMeasureBtn.id
                        mLSeekBar.progress = 1
                        currentML = mLSeekBar.progress * step
                        changeMugPicture(currentML)
                        mlText.setText(currentML.toString() + " mL")
                    }
                    doubleMeasureBtn.id -> {
                        currentDose = doubleMeasureBtn.id
                        mLSeekBar.progress = 2
                        currentML = mLSeekBar.progress * step
                        changeMugPicture(currentML)
                        mlText.setText(currentML.toString() + " mL")
                    }


                }
            }

        }
    }


    fun changeMugPicture(mL: Int) {
        when (mL) {
            25 -> mugPicture.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.percent25,
                    null
                )
            )
            50 -> mugPicture.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.percent50,
                    null
                )
            )
            75 -> mugPicture.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.percent75,
                    null
                )
            )
        }
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

    override fun onBackPressed() {
        tts?.shutdown()
        val SelectCoffeeIntent = Intent(this, SelectCoffeeActivity::class.java)
        startActivity(SelectCoffeeIntent)
        super.onBackPressed()
    }

    override fun onStop() {
        tts?.shutdown()
        super.onStop()

    }

    override fun onPause() {
        tts?.stop()
        super.onPause()
    }


}




