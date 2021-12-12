package com.example.emailauth

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.emailauth.databinding.ActivityPhoneSignBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit

class PhoneSignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneSignBinding
    private lateinit var mAuth: FirebaseAuth
    private var mVerificationId: String ?= null
    private var mToken: PhoneAuthProvider.ForceResendingToken ?= null

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneSignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        supportActionBar?.hide()

        database = FirebaseDatabase.getInstance()

        binding.getOtp.setOnClickListener {
            showProgressBar()
            hideKeyboard()

            val phoneNo = binding.etPhoneInput.text.toString()
            when {
                phoneNo.isEmpty() -> {
                    hideProgressBar()
                    showSnackBar("Enter Phone No", binding.etPhoneInput)
                }
                phoneNo.length != 10 -> {
                    hideProgressBar()
                    showSnackBar("Enter 10 digit Phone No", binding.etPhoneInput)
                }
                else -> {

                    setSignPage()

                    val option = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91$phoneNo")
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(option)
                }
            }
        }

        binding.verifyAndSubmit.setOnClickListener {
            showProgressBar()
            val phoneNo = binding.etPhoneInput.text.toString()
            val code = binding.etOtpInput.text.toString()
            when {
                phoneNo.isEmpty() -> {
                    hideProgressBar()
                    showSnackBar("Enter Phone No", binding.etPhoneInput)
                }
                phoneNo.length != 10 -> {
                    hideProgressBar()
                    showSnackBar("Enter 10 digit Phone No", binding.etPhoneInput)
                }
                code.isEmpty() -> {
                    hideProgressBar()
                    showSnackBar("OTP can't be empty", binding.etOtpInput)
                }
                code.length != 6 -> {
                    hideProgressBar()
                    showSnackBar("Enter 6 digit OTP", binding.etOtpInput)
                }
                else -> {
                    val credential = PhoneAuthProvider.getCredential(mVerificationId.toString(), code)
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }

        binding.signPhonePage.setOnClickListener {
            hideKeyboard()
        }
        binding.ibPhoneEdit.setOnClickListener {
            resetSignPage()
        }
        binding.resendOtp.setOnClickListener {

        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, token)
            hideProgressBar()
            mVerificationId = verificationId
            mToken = token
        }

        override fun onVerificationFailed(e: FirebaseException) {
            hideProgressBar()
            resetSignPage()
            showSnackBar(e.message.toString(), binding.etPhoneInput)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            task ->
            if (task.isSuccessful) {
                hideProgressBar()
                isUser(mAuth.currentUser?.phoneNumber.toString())
            }
            else {
                hideProgressBar()
                resetSignPage()
                showSnackBar(task.exception?.message.toString(), binding.etPhoneInput)
            }
        }
    }

    private fun isUser(contact: String) {
        val query = database.getReference("userUid").orderByKey().equalTo(mAuth.currentUser?.uid.toString())
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    startActivity(Intent(applicationContext, MainActivity::class.java).apply {  }
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
                else{
                    startActivity(Intent(applicationContext, RegisterInfo::class.java).apply {
                        putExtra("contact", contact)
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setSignPage() {
        binding.getOtp.visibility = View.GONE
        binding.etPhoneInput.isEnabled = false
        binding.ibPhoneEdit.visibility = View.VISIBLE
    }
    private fun resetSignPage() {
        binding.getOtp.visibility = View.VISIBLE
        binding.etPhoneInput.isEnabled = true
        binding.ibPhoneEdit.visibility = View.GONE
        binding.resendOtp.visibility = View.INVISIBLE
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    private fun showSnackBar(msg :String, view : View) {
        var snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("X", View.OnClickListener {  })
        snackBar.setActionTextColor(Color.parseColor("#000000"))
        snackBar.setBackgroundTint(Color.parseColor("#D7F0EA"))
        snackBar.setTextColor(Color.parseColor("#000000"))
        snackBar.setAnchorView(view).show()
    }
    private fun hideKeyboard() {
        var imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        binding.etPhoneInput.clearFocus()
        binding.etOtpInput.clearFocus()
    }
}