package com.example.emailauth

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.emailauth.databinding.ActivityRegisterInfoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterInfo : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterInfoBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.RegisterInfoActivity.setOnClickListener {
            hideKeyboard()
        }

        binding.Register.setOnClickListener {
            hideKeyboard()
            val name = binding.etNameInput.text.toString()
            val username = binding.etUsernameInput.text.toString()
            if (name.isEmpty()) {
                showSnackBar("Name can't be empty", binding.etNameInput)
            }
            else if (username.isEmpty()) {
                showSnackBar("Username can't be empty", binding.etUsernameInput)
            }
            else {
                val query = database.getReference("userUid").orderByChild("username").equalTo(username)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            showSnackBar("This username already exist", binding.etUsernameInput)
                        }
                        else {
                            val contact = intent.getStringExtra("contact").toString()
                            val provider = mAuth.currentUser?.getIdToken(false)?.result?.signInProvider.toString()

                            database.getReference("userUid").child(mAuth.currentUser?.uid.toString())
                                .setValue(UserProfile(name, contact, username, provider))
                            startActivity(Intent(applicationContext, MainActivity::class.java).apply {  }
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mAuth.signOut()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun showSnackBar(msg :String, view : View) {
        val snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("X", View.OnClickListener {  })
        snackBar.setActionTextColor(Color.parseColor("#000000"))
        snackBar.setBackgroundTint(Color.parseColor("#D7F0EA"))
        snackBar.setTextColor(Color.parseColor("#000000"))
        snackBar.setAnchorView(view).show()
    }
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        binding.etNameInput.clearFocus()
        binding.etUsernameInput.clearFocus()
    }
}