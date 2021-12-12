package com.example.emailauth

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.example.emailauth.databinding.ActivityForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.navigateSignInOptionActivity.setOnClickListener {
            startActivity(Intent(this, SignUpOptionActivity::class.java).apply {  })
        }
        binding.sendLink.setOnClickListener {
            sendLink()
        }
        binding.forgotPasswordPage.setOnClickListener {
            hideKeyboard()
        }
    }
    private fun sendLink() {
        val emailText = binding.etEmailInput.text.toString()
        when {
            emailText.isEmpty() -> {
                showSnackBar("Email can't be Empty", binding.etEmailInput)
            }
            else -> {
                showProgressBar()
                mAuth.sendPasswordResetEmail(emailText).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val dialog = AlertDialog.Builder(this)
                        dialog.setMessage("We have sent a password reset link to your email id. Please verified it")
                            .setPositiveButton("Ok") { sub_dialog, _ ->
                                sub_dialog.cancel()
                                Log.d("AAo", it.exception?.message.toString())
                                startActivity(
                                    Intent(this, EmailSignInActivity::class.java).apply { })
                            }
                        hideProgressBar()
                        dialog.show()
                    }
                    else {
                        hideProgressBar()
                        showSnackBar(it.exception?.message.toString(), binding.etEmailInput)
                    }
                }
            }
        }
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
        binding.etEmailInput.clearFocus()
    }
}