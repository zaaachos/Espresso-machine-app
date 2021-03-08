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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_coffee_monthly_consumption.*
import kotlinx.android.synthetic.main.activity_stock_products.*
import java.util.*
import kotlin.collections.ArrayList

class CoffeeMonthlyConsumptionActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var allMonths: ArrayList<Months> = ArrayList(12)
    private lateinit var monthAdapter: MonthsListView
    private val RQ_SPEECH_MENU = 104
    private val RQ_SPEECH_MONTH = 105
    private var RQ_TEMP_CODE = 104
    private var tts: TextToSpeech? = null
    private val HashMonths: HashMap<String, Int> = HashMap<String, Int>()
    private val HashMonthsEnglish: HashMap<String, Int> = HashMap<String, Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_coffee_monthly_consumption)

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


        HashMonths.put("Ιανουάριος", 11)
        HashMonths.put("Φεβρουάριος", 23)
        HashMonths.put("Μάρτιος", 6)
        HashMonths.put("Απρίλιος", 17)
        HashMonths.put("Μάιος", 32)
        HashMonths.put("Ιούνιος", 1)
        HashMonths.put("Ιούλιος", 9)
        HashMonths.put("Αύγουστος", 22)
        HashMonths.put("Σεπτέμβριος", 25)
        HashMonths.put("Οκτώβριος", 6)
        HashMonths.put("Νοέμβριος", 7)
        HashMonths.put("Δεκέμβριος", 28)

        HashMonthsEnglish.put("January", 11)
        HashMonthsEnglish.put("February", 23)
        HashMonthsEnglish.put("March", 6)
        HashMonthsEnglish.put("April", 17)
        HashMonthsEnglish.put("May", 32)
        HashMonthsEnglish.put("June", 1)
        HashMonthsEnglish.put("July", 9)
        HashMonthsEnglish.put("August", 22)
        HashMonthsEnglish.put("September", 25)
        HashMonthsEnglish.put("October", 6)
        HashMonthsEnglish.put("November", 7)
        HashMonthsEnglish.put("December", 28)

        allMonths.add(Months(R.string.january, 11, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.february, 23, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.march, 6, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.april, 17, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.may, 32, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.june, 1, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.july, 9, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.august, 22, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.september, 25, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.october, 6, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.november, 7, R.drawable.coffee_mug))
        allMonths.add(Months(R.string.december, 28, R.drawable.coffee_mug))

        monthAdapter = MonthsListView(this, allMonths)

        allMonthsListViewId.adapter = monthAdapter


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

    fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = activity.resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

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
                "Μηνιαία Κατανάλωση! Πείτε μου τι θα θέλατε ή πείτε επιλογές για να ακούσετε τις επιλογές. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω! Mιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Monthly Consumption! Tell me what you would like, or say choices to hear the possible choices . If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )

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
                val back = Intent(this, StockProductsActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() == "ΛΊΣΤΑ" || word.toUpperCase() == "LIST") {
                if (loadFlagData() == true) {
                    var speaker = "Η κατανάλωση για τον κάθε μήνα είναι. "
                    var text = ""
                    for (key in HashMonths.keys) {
                        text += "$key . ${HashMonths[key]} ποτήρια καφέ. "
                    }
                    var res =
                        "$speaker . $text . Πείτε μου τι άλλο θα θέλατε να ακούσετε; Οι επιλογές είναι λίστα. Επιλογή Μήνα. Παρακαλώ μιλήστε μετά τον ήχο!"
                    tts!!.speak(
                        res, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                } else {
                    var speaker = "The consumption for each month is. "
                    var text = ""
                    for (key in HashMonths.keys) {
                        text += "$key . ${HashMonths[key]} glass of coffee. "
                    }
                    var res =
                        "$speaker . $text . Tell what would you like to hear; Choices are. List. Choose Month. Please speak after the tone!"
                    tts!!.speak(
                        res, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                }

            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΉ ΜΉΝΑ" || word.toUpperCase() == "CHOOSE MONTH") {
                if (loadFlagData() == true) {
                    RQ_TEMP_CODE = RQ_SPEECH_MONTH
                    tts!!.speak(
                        "Πείτε μου παρακαλώ, για ποιό μήνα θέλετε να ακούσετε; ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                } else {
                    RQ_TEMP_CODE = RQ_SPEECH_MONTH
                    tts!!.speak(
                        "Please tell me, which month do you want to hear about? ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                }
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. Λίστα. Επιλογή μήνα. ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. List. Choose Month. ",
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
        if (requestCode == RQ_SPEECH_MONTH && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (loadFlagData() == true) {

                if (HashMonths.containsKey(word)) {
                    var amount = HashMonths[word]

                    var speaker =
                        "Τον μήνα $word καταναλώσατε $amount ποτήρια καφέ. Πείτε μου τι άλλο θα θέλατε να ακούσετε; Οι επιλογές είναι λίστα. Επιλογή Μήνα. Παρακαλώ μιλήστε μετά τον ήχο!"
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                    tts!!.speak(
                        speaker, TextToSpeech.QUEUE_FLUSH, null, ""
                    )

                } else {
                    tts!!.speak(
                        "Δεν κατάλαβα, παρακαλώ επαναλάβετε. ", TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                }
            } else {
                if (HashMonthsEnglish.containsKey(word)) {
                    var amount = HashMonthsEnglish[word]
                    var speaker =
                        "On $word you consumed $amount glass of coffee. Tell me what else you would like to hear? Choices are. List. Choose month. Please speak after the tone!"
                    RQ_TEMP_CODE = RQ_SPEECH_MENU
                    tts!!.speak(
                        speaker, TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                } else {
                    tts!!.speak(
                        "I couldn't understand, please repeat. ", TextToSpeech.QUEUE_FLUSH, null, ""
                    )
                }
            }

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
            if (loadFlagData() != true) {

                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            } else {
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el_GR")
            }

            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say sth")
            startActivityForResult(i, CODE)

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
        val Intent = Intent(this, StockProductsActivity::class.java)
        startActivity(Intent)
    }


}