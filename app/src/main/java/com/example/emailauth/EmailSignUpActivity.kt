package com.example.emailauth

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.emailauth.databinding.ActivityEmailSignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap

class EmailSignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailSignUpBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide();

        mAuth = FirebaseAuth.getInstance()

        binding.navigateSignInOptionActivity.setOnClickListener {
            startActivity(Intent(this, SignInOptionActivity::class.java).apply {  })
        }
        binding.signUpEmailPage.setOnClickListener {
            hideKeyboard()
        }
        binding.signUp.setOnClickListener {
            showProgressBar()
            hideKeyboard()
            createUser()
        }
    }
    private fun createUser(){
        val emailText = binding.etEmailInput.text.toString()
        val passwordText = binding.etPasswordInput.text.toString()
        val confirmPasswordText = binding.etConfirmPasswordInput.text.toString()

        when {
            emailText.isEmpty() -> {
                hideProgressBar()
                showSnackBar("Email can't be Empty", binding.etEmailInput)
            }
            passwordText.isEmpty() -> {
                hideProgressBar()
                showSnackBar("Password can't be empty", binding.etPasswordInput)
            }
            confirmPasswordText.isEmpty() -> {
                hideProgressBar()
                showSnackBar("Password can't be empty", binding.etConfirmPasswordInput)
            }
            passwordText != confirmPasswordText -> {
                hideProgressBar()
                showSnackBar("Password can't be matched", binding.etConfirmPasswordInput)
            }
            else -> {

                mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        mAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                            if(task.isSuccessful){
                                val dialog = AlertDialog.Builder(this)
                                dialog.setMessage("We have sent a confirmation link to your email id. Please verified it")
                                    .setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
                                        dialog.cancel()
                                        showToast("Registered Successfully")
                                        Log.d("AAo", task.exception?.message.toString())
                                        startActivity(Intent(this, EmailSignInActivity::class.java).apply {  })
                                    })
                                hideProgressBar()
                                dialog.show()
                            }
                            else{
                                hideProgressBar()
                                Log.d("AAo", task.exception?.message.toString())
                            }
                        }
                    }
                    else{
                        hideProgressBar()
                        showSnackBar(task.exception?.message.toString(), binding.etEmailInput)
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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
        binding.etEmailInput.clearFocus()
        binding.etPasswordInput.clearFocus()
        binding.etConfirmPasswordInput.clearFocus()
    }
}