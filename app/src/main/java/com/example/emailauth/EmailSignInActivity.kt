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
import com.example.emailauth.databinding.ActivityEmailSignInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmailSignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailSignInBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide();
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.signInEmailPage.setOnClickListener {
            hideKeyboard()
        }
        binding.signIn.setOnClickListener {
            hideKeyboard()
            loginUser()
        }
        binding.navigateSignUpOptionActivity.setOnClickListener {
            startActivity(Intent(this, SignUpOptionActivity::class.java).apply {  })
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java).apply {  })
        }
    }
    private fun loginUser(){
        val emailText = binding.etEmailInput.text.toString()
        val passwordText = binding.etPasswordInput.text.toString()

        when {
            emailText.isEmpty() -> {
                showSnackBar("Email can't be Empty", binding.etEmailInput)
            }
            passwordText.isEmpty() -> {
                showSnackBar("Password can't be empty", binding.etPasswordInput)
            }
            else -> {
                showProgressBar()
                mAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener {
                        task ->
                    if (task.isSuccessful){
                        if(mAuth.currentUser?.isEmailVerified == true){
                            hideProgressBar()

                            isUser(emailText)
                        }
                        else{
                            val dialog = AlertDialog.Builder(this)
                            mAuth.signOut()
                            dialog.setMessage("We have sent a confirmation link to your email id. Please verified it")
                                .setPositiveButton("ok", DialogInterface.OnClickListener { dialog, which ->
                                    dialog.cancel()
                                })
                            hideProgressBar()
                            dialog.show()
                        }
                    }
                    else {
                        hideProgressBar()
                        showSnackBar(task.exception?.message.toString(), binding.etEmailInput)
                    }
                }
            }
        }
    }

    private fun isUser(contact: String) {
        val query = database.getReference("userUid").orderByKey().equalTo(mAuth.currentUser?.uid.toString())
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    database.getReference("userUid").child(mAuth.currentUser?.uid.toString())
                        .child("provider")
                        .setValue(mAuth.currentUser?.getIdToken(false)?.result?.signInProvider.toString())

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
            }
        })
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
        binding.etEmailInput.clearFocus()
        binding.etPasswordInput.clearFocus()
    }
}