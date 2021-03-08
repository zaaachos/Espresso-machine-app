package com.example.espressomachineapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            if (loadFirstTime()) {
                saveFirstTime(false)
                startActivity(Intent(this, WelcomeScreenForBlindPeople::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, SPLASH_TIME_OUT)

    }

    private fun saveFirstTime(flag: Boolean) {
        val sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("WELCOME_SCREEN", flag)
        editor.apply()
    }

    private fun loadFirstTime(): Boolean {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val savedFlag = sharedPreferences.getBoolean("WELCOME_SCREEN", true)
        return savedFlag
    }
}