package com.example.emailauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.lang.Thread.sleep

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide();
        val thread = Thread() {
            try {
                sleep(1000)
            }catch (e: Exception){
                Log.d("AAo", "Akhada")
            }finally {
                startActivity(Intent(this, MainActivity::class.java).apply {  })
                finish()
            }
        }
        thread.start()
    }
}