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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*


class SettingsActivity : AppCompatActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    private val RQ_SPEECH_REC = 105
    private var RQ_TEMP_CODE = 105
    private val RQ_SPEECH_LANGUAGE = 106
    private val RQ_SPEECH_SAVE = 107
    private var speech_language_selected = ""
    private var tts: TextToSpeech? = null
    private var FLAG: Boolean = false
    private var locale: Locale? = null


    // Data to use
    var STH_CHANGE = false
    var GREEK = false
    var ENGLISH = false


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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

        if (loadFlagData() == true) {         // Greek

            setLocale(this, "el_GR")
            greekBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gr_selected,
                    null
                )
            )
            englishBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gb,
                    null
                )
            )
        } else {                // English

            setLocale(this, "en")
            greekBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gr,
                    null
                )
            )
            englishBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gb_selected,
                    null
                )
            )

        }
        greekBtn.setOnClickListener(this)
        englishBtn.setOnClickListener(this)
        switchConfirm.isChecked = loadSwitch()
        switchConfirm.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveSwitchState(true)
                Toast.makeText(this, "Ενεργοποιήσατε τις φωνητικές εντολές!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                saveSwitchState(false)
                Toast.makeText(this, "Απενεργοποιήσατε τις φωνητικές εντολές!", Toast.LENGTH_SHORT)
                    .show()
            }
        }


    }

    private fun saveSwitchState(flag: Boolean) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("SWITCH", flag)
        editor.apply()
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

    private fun loadFlagData(): Boolean? {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("FLAG", true)
        return savedFlag

    }

    private fun saveData(flag: Boolean) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("FLAG", flag)
        editor.apply()
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
                "Ρυθμίσεις. Για να αλλάξετε την γλώσσα πείτε αλλαγή γλώσσας. Διαφορετικά πείτε πίσω για να μεταβείτε στο αρχικό μενού. Μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Settings. To change app language, say change language. If you want to go back, say go back, in order to redirect to home menu. Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
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
            } else if (word.toUpperCase() == "ΑΛΛΑΓΉ ΓΛΏΣΣΑΣ" || word.toUpperCase() == "CHANGE LANGUAGE"
            ) {
                if (loadFlagData() == true) {
                    RQ_TEMP_CODE = RQ_SPEECH_LANGUAGE
                    tts!!.speak(
                        "Οι επιλογές για την γλώσσα είναι. ελληνικά. αγγλικά. Πείτε μου σας παρακαλώ τι γλώσσα θέλετε να βάλουμε; Παρακαλώ μιλήστε μετά τον ήχο! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                } else {
                    RQ_TEMP_CODE = RQ_SPEECH_LANGUAGE
                    tts!!.speak(
                        "The choices for language are. greek. english. Please tell, which language you want? Speak after the tone! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. change language. ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. αλλαγή γλώσσας. ",
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
        if (requestCode == RQ_SPEECH_LANGUAGE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΑΓΓΛΙΚΆ" || word.toUpperCase() == "ENGLISH") {
                if (loadFlagData() == true) {
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                    speech_language_selected = "EN"
                    tts!!.speak(
                        "Η γλώσσα άλλαξε σε αγγλικά. Θέλετε να αποθηκεύσουμε τις αλλαγές; Παρακαλώ μιλήστε μετά τον ήχο! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                } else {
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                    speech_language_selected = "EN"
                    tts!!.speak(
                        "Language has been set to english. Do you want to save these changes; Speak after the tone! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
            } else if (word.toUpperCase() == "ΕΛΛΗΝΙΚΆ" || word.toUpperCase() == "GREEK"
            ) {
                if (loadFlagData() == true) {
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                    speech_language_selected = "GR"
                    tts!!.speak(
                        "Η γλώσσα άλλαξε σε ελληνικά. Θέλετε να αποθηκεύσουμε τις αλλαγές; Παρακαλώ μιλήστε μετά τον ήχο! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                } else {
                    RQ_TEMP_CODE = RQ_SPEECH_SAVE
                    speech_language_selected = "GR"
                    tts!!.speak(
                        "Language has been set to greek. Do you want to save these changes; Speak after the tone! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                }
            } else if (word.toUpperCase() == "CHOICES") {
                tts!!.speak(
                    "The choices are. greek. english ",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
            } else if (word.toUpperCase() == "ΕΠΙΛΟΓΈΣ") {
                tts!!.speak(
                    "Οι επιλογές είναι. ελληνικά. αγγλικά. ",
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
        if (requestCode == RQ_SPEECH_SAVE && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΝΑΙ" || word.toUpperCase() == "ΝΑΊ" || word.toUpperCase() == "YES") {
                if (speech_language_selected == "EN") {
                    englishBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.gb_selected,
                            null
                        )
                    )
                    greekBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.gr,
                            null
                        )
                    )
                    // change text to preferred language

                    saveData(false)
                    RQ_TEMP_CODE = 0
                    tts!!.setLanguage(Locale("en"))
                    tts!!.speak(
                        "Your language has been set to english. Now you will be directed to home menu! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )

                    val go_back = Intent(this, MainActivity::class.java)
                    startActivity(go_back)


                } else if (speech_language_selected == "GR") {
                    englishBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.gb,
                            null
                        )
                    )
                    greekBtn.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.gr_selected,
                            null
                        )
                    )
                    // change text to preferred language
                    saveData(true)
                    RQ_TEMP_CODE = 0
                    tts!!.setLanguage(Locale("el_GR"))
                    tts!!.speak(
                        "Η γλώσσα άλλαξε σε ελληνικά. Τώρα θα μεταβείτε στο αρχικό μενού! ",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    val go_back = Intent(this, MainActivity::class.java)
                    startActivity(go_back)

                }

            } else if (word.toUpperCase() == "ΌΧΙ" || word.toUpperCase() == "ΝΟ") {
                RQ_TEMP_CODE = RQ_SPEECH_REC
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


    private fun inputSpeech(CODE: Int) {
        if (CODE == 0) {
            return
        }
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
        if (STH_CHANGE) {
            println("Back Pressed")
            if (ENGLISH && !GREEK) {                                 // case ENGLISH
                println("ENGLISH")

                englishBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.gb_selected,
                        null
                    )
                )
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.accept_changes)
                builder.setMessage("Η γλώσσα έχει οριστεί αγγλικά. Θα θέλατε να εφαρμόσουμε αυτή την αλλαγή;")
                builder.setPositiveButton("NAI") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Ok, we changed the app language.",
                        Toast.LENGTH_SHORT
                    ).show()
                    println("YES PRESSED")
                    // change text to preferred language
                    saveData(false)
                    setLocale(this, "en")
                    println("Lang changed!")
                    val go_back = Intent(this, MainActivity::class.java)
                    recreate()
                    startActivity(go_back)
                }
                builder.setNegativeButton("OXI") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Οι αλλαγές δεν αποθηκεύτηκαν.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val go_back = Intent(this, MainActivity::class.java)
                    recreate()
                    startActivity(go_back)
                }
                builder.show()


            } else if (!ENGLISH && GREEK) {                 // case GREEK

                greekBtn.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.gr_selected,
                        null
                    )
                )
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.accept_changes)
                builder.setMessage("Language has been set to greek. Do you want to accept these changes?")
                builder.setPositiveButton("YES") { dialog, which ->
                    Toast.makeText(
                        applicationContext,
                        "Οκ, αλλάξαμε την γλώσσα της διεπαφής.",
                        Toast.LENGTH_SHORT
                    ).show()
                    setLocale(this, "el_GR")
                    saveData(true)
                    val go_back = Intent(this, MainActivity::class.java)
                    recreate()
                    startActivity(go_back)
                }
                builder.setNegativeButton("NO") { dialog, which ->
                    Toast.makeText(applicationContext, "Changes cancelled.", Toast.LENGTH_SHORT)
                        .show()
                    val go_back = Intent(this, MainActivity::class.java)
                    recreate()
                    startActivity(go_back)
                }
                builder.show()


            } else {
                println("I DON'T SEE ANY CHANGES HERE!")
            }
        } else {
            val go_back = Intent(this, MainActivity::class.java)
            recreate()
            startActivity(go_back)
        }
    }


    override fun onClick(v: View) {
        if (v.id == greekBtn.id) {

            // check if current image is already selected.
            if (greekBtn.drawable.constantState == resources.getDrawable(R.drawable.gr_selected).constantState) {
                println("Is already selected!")
                return
            }

            // CHANGE VALUES
            STH_CHANGE = true
            GREEK = true
            ENGLISH = false

            greekBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gr_touch,
                    null
                )
            )
            englishBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gb,
                    null
                )
            )

        } else if (v.id == englishBtn.id) {
            // check if current image is already selected.
            if (englishBtn.drawable.constantState == resources.getDrawable(R.drawable.gb_selected).constantState) {
                println("Is already selected!")
                return
            }
            // CHANGE VALUES
            STH_CHANGE = true
            ENGLISH = true
            GREEK = false

            englishBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gb_touch,
                    null
                )
            )
            greekBtn.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.gr,
                    null
                )
            )

        }

    }
}





