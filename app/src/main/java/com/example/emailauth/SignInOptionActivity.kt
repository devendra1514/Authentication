package com.example.emailauth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.emailauth.databinding.ActivitySignInOptionBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInOptionActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private lateinit var binding: ActivitySignInOptionBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide();

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.signInWithEmail.setOnClickListener {
            startActivity(Intent(this, EmailSignInActivity::class.java).apply{  })
        }
        binding.navigateSignUpOptionActivity.setOnClickListener {
            startActivity(Intent(this, SignUpOptionActivity::class.java).apply{  })
        }
        binding.signInWithPhone.setOnClickListener {
            startActivity(Intent(this, PhoneSignActivity::class.java).apply {  })
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInWithGoogle.setOnClickListener {
            signIn()
        }
    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task?.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        showProgressBar()
        GlobalScope.launch(IO) {
            val auth = mAuth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Main) {
                isUser(mAuth.currentUser?.email.toString())
            }

        }
//        mAuth.signInWithCredential(credential).addOnCompleteListener {
//            if (it.isSuccessful) {
//                hideProgressBar()
////                Log.d("Aao", mAuth.currentUser?.email.toString())
//                isUser(mAuth.currentUser?.email.toString())
//
//            }
//            else {
//                Log.d("Aao", it.exception.message.toString())
//            }
//        }
    }

    private fun isUser(contact: String) {
        val query = database.getReference("userUid").orderByKey().equalTo(mAuth.currentUser?.uid.toString())
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {

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
                TODO("Not yet implemented")
            }
        })
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


}