package com.example.emailauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.emailauth.databinding.ActivitySignUpOptionBinding

class SignUpOptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpOptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide();
        binding.signUpWithEmail.setOnClickListener {
            startActivity(Intent(this, EmailSignUpActivity::class.java).apply{  })
        }
        binding.navigateSignInOptionActivity.setOnClickListener {
            startActivity(Intent(this, SignInOptionActivity::class.java).apply{  })
        }
        binding.signUpWithPhone.setOnClickListener {
            startActivity(Intent(this, PhoneSignActivity::class.java).apply {  })
        }
    }
}