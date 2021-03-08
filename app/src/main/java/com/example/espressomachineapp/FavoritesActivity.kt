package com.example.espressomachineapp

import android.R.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_coffee_maker.*
import kotlinx.android.synthetic.main.activity_favorites.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_stores.view.*
import kotlinx.android.synthetic.main.alert_box.*
import kotlinx.android.synthetic.main.alert_box.view.*
import java.util.*
import kotlin.collections.ArrayList


class FavoritesActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    companion object {
        var mExampleList: ArrayList<Person>? = null
        var currentViewId = 1
    }

    private val RQ_SPEECH_REC = 103
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (loadFlagData() == true) {
            setLocale(this, "el_GR")
        } else {
            setLocale(this, "en")
        }
        setContentView(R.layout.activity_favorites)

        loadData()
        setInsertButton()

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
                "Πείτε μου ποιό αγαπημένο ρόφημα θα θέλατε ,ή πειτε λιστα για να ακούσετε τα αγαπημένα. Πείτε προσθήκη για να προσθέσετε νέο. Αν θέλετε να πάτε στην προηγούμενη σελίδα, πείτε πίσω!  Μιλήστε μετά τον ήχο!", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
        else
            tts!!.speak(
                "Tell me which favorite drink you would like, or say list to hear all the favorites. Say add to add a new one. If you want to go back, say go back! Speak after the tone.", //Hello. How could I serve you? Speak after the tone.
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )

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

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            val profiles = Array(mExampleList!!.size) { i ->
                (mExampleList!!.get(i).userName).toString().toUpperCase()
            }

            for (profile in profiles) {
                println(profile)
            }


            if (result != null && result.size > 0)
                word = result.get(0)
            //title2.text = textView3.text.toString().toUpperCase()

            if (word.toUpperCase() == "ΠΊΣΩ" || word.toUpperCase() == "GO BACK") {
                val back = Intent(this, MainActivity::class.java)
                startActivity(back)
                finish()
            } else if (word.toUpperCase() in profiles) {
                val MAKE_FAVORITE = Intent(this, CoffeeLoadingScreen::class.java)
                startActivity(MAKE_FAVORITE)
            } else if (word.toUpperCase() == "ΛΊΣΤΑ" || word.toUpperCase() == "LIST") {

                if (mExampleList?.isEmpty() == true) {
                    if (loadFlagData() == true) {
                        tts!!.speak(
                            "Δεν έχετε αποθηκεύσει αγαπημένα ακόμα.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    } else {
                        tts!!.speak(
                            "You have not saved any favorite drink yet.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }
                } else {
                    var names = ""
                    var index = 1
                    for (person in mExampleList!!) {
                        var coff = coffeeNameTranslation(person.getUserCoffee())
                        names += "$index. " + person.getUserName() +" " + coff + " " + person.getUserDose() + " " + person.getMl() + ". "
                        index++
                    }
                    if (loadFlagData() == true) {
                        tts!!.speak(
                            "Τα αγαπημένα σας είναι. $names",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    } else {
                        tts!!.speak(
                            "Your favorites are. $names",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }
                }


            } else if (word.toUpperCase() == "ΠΡΟΣΘΉΚΗ" || word.toUpperCase() == "ADD") {

                val newFavor = Intent(this, SelectCoffeeForFavorites::class.java)
                startActivity(newFavor)


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


    override fun onPause() {
        tts?.stop()
        super.onPause()
    }


    override fun onBackPressed() {
        tts?.shutdown()
        saveData()
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

    private fun loadSwitch(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("SWITCH", false)
        return savedFlag

    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(mExampleList)
        editor.putString("favor list", json)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("favor list", null)
        val type: java.lang.reflect.Type? = object : TypeToken<ArrayList<Person?>?>() {}.type
        mExampleList = gson.fromJson(json, type)

        if (mExampleList == null) {
            mExampleList = ArrayList()
        }
        mExampleList?.forEach {
            addNewItem(it.userName, it.userCoffee, it.userDose, it.ml)
        }
    }


    private fun setInsertButton() {

        addBtn.setOnClickListener(View.OnClickListener {
            val goSecondSelect = Intent(this, SelectCoffeeForFavorites::class.java)
            startActivity(goSecondSelect)
        }
        )

    }

    private fun insertItem(line1: String, line2: String, line3: String, line4: String) {
        mExampleList?.add(Person(line1, line2, line3, line4))
        saveData()
        recreate()
    }

    //
    override fun onStop() {
        tts?.shutdown()
        saveData()
        super.onStop()
    }

    private fun addNewItem(username: String, coffee: String, dose: String, ml: String) {


        val px_eight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8F,
            resources.getDisplayMetrics()
        ).toInt()

        val px_hundred = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100F,
            resources.getDisplayMetrics()
        ).toInt()


        val px_twelve = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            12F,
            resources.getDisplayMetrics()
        ).toInt()


        // Parent Linear Layout Parameters
        var new_item2 = LinearLayout(this)
        val paramas = LinearLayout.LayoutParams(-1, -2)
        paramas.setMargins(px_eight, px_eight, px_eight, px_eight)
        paramas.gravity = Gravity.CENTER;
        // Parent set Attributes
        new_item2.orientation = LinearLayout.HORIZONTAL
        new_item2.setPadding(px_twelve, px_twelve, px_twelve, px_twelve)
        new_item2.gravity = Gravity.CENTER
        new_item2.setBackgroundResource(R.drawable.linearborderdark)
        //Display parent
        new_item2.layoutParams = paramas

        // ImageView for Coffee
        val iv = ImageView(this)
        val parms = LinearLayout.LayoutParams(px_hundred, px_hundred)
        iv.setBackgroundResource(R.color.colorAccent)

        when (coffee) {

            "ESPRESSO" -> {
                iv.setImageResource(R.drawable.espresso)
            }
            "CAPPUCCINO" -> {
                iv.setImageResource(R.drawable.cappuccino)
            }
            "MACCHIATO" -> {
                iv.setImageResource(R.drawable.macchiato)
            }
            "LATTE" -> {
                iv.setImageResource(R.drawable.latte)
            }
            "AMERICANO" -> {
                iv.setImageResource(R.drawable.americano)
            }
            "Γάλα" -> {
                iv.setImageResource(R.drawable.milk)
            }
            "Νερό" -> {
                iv.setImageResource(R.drawable.water)
            }
            "Milk" -> {
                iv.setImageResource(R.drawable.milk)
            }
            "Water" -> {
                iv.setImageResource(R.drawable.water)
            }
            "γάλα" -> {
                iv.setImageResource(R.drawable.milk)
            }
            "νερό" -> {
                iv.setImageResource(R.drawable.water)
            }
            "milk" -> {
                iv.setImageResource(R.drawable.milk)
            }
            "water" -> {
                iv.setImageResource(R.drawable.water)
            }

        }
        //Display ImageView
        iv.layoutParams = parms

        // Child LinearLayout for TextViews.
        var new_item3 = LinearLayout(this)
        //Parameters for Child LinearLayout.
        val paramas2 = LinearLayout.LayoutParams(-1, -2)
        paramas2.setMargins(px_eight, px_eight, px_eight, px_eight)
        //Attributes for Child LinearLayout.
        new_item3.orientation = LinearLayout.VERTICAL
        new_item3.setPadding(px_eight, px_eight, px_eight, px_eight)
        new_item3.gravity = Gravity.CENTER

        // Text View for Profile
        val text1 = TextView(this)
        // Parameters
        val text_param = LinearLayout.LayoutParams(-1, -2)
        paramas2.setMargins(px_eight, px_eight, px_eight, px_eight)
        // Attributes
        text1.text = "$username\n$coffee"
        val face = ResourcesCompat.getFont(this, R.font.muli_bold)
        text1.setTypeface(face, Typeface.BOLD)
        text1.textAlignment = View.TEXT_ALIGNMENT_CENTER
        text1.setTextColor(Color.rgb(0, 0, 0))
        text1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22F)
        // Display TextView
        text1.layoutParams = text_param

        // Text View for Profile
        val text2 = TextView(this)
        paramas2.setMargins(px_eight, px_eight, px_eight, px_eight)
        var check_dose = ""
        if (loadFlagData() == true) {
            if (dose == "Μονός" || dose == "Single")
                check_dose = "Μονός"
            if (dose == "Διπλός" || dose == "Double")
                check_dose = "Διπλός"
        } else {

            if (dose == "Μονός" || dose == "Single")
                check_dose = "Single"
            if (dose == "Διπλός" || dose == "Double")
                check_dose = "Double"
        }

        // Attributes
        text2.text = "$check_dose ($ml ml)"
        text2.setTypeface(face, Typeface.BOLD)
        text2.textAlignment = View.TEXT_ALIGNMENT_CENTER
        text2.setTextColor(Color.rgb(0, 0, 0))
        text2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
        // Display TextView
        text2.layoutParams = text_param

        // Add recursive the Items as Nodes
        new_item3.addView(text1)
        new_item3.addView(text2)
        new_item2.addView(iv)
        new_item2.addView(new_item3)

        // If user press any of the favorites start brewing.
        new_item2.setOnClickListener {
            val MAKE_FAVORITE = Intent(this, CoffeeLoadingScreen::class.java)
            startActivity(MAKE_FAVORITE)
        }


        // Display the updated List View
        myListId.addView(new_item2)
    }

}