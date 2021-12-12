package com.example.emailauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.emailauth.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.signOutButton.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, SignInOptionActivity::class.java).apply {  })
            finish()
        }
        binding.deleteAccountButton.setOnClickListener {
            database.getReference("userUid").child(mAuth.currentUser?.uid.toString()).removeValue()
            mAuth.currentUser?.delete()
            startActivity(Intent(this, SignInOptionActivity::class.java).apply {  })
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, SignUpOptionActivity::class.java).apply {  })
            finish()
        }
        else {
            database.getReference("userUid").child(currentUser.uid).get()
                .addOnSuccessListener {
                    binding.tvName.text = it.child("name").value.toString()
                    binding.tvContact.text = it.child("contact").value.toString()
                    binding.tvUsername.text = it.child("username").value.toString()
                    binding.tvProvider.text = it.child("provider").value.toString()
                }.addOnFailureListener{
                showToast(it.toString())
            }

//            val query = database.getReference("userUid").orderByKey().equalTo(currentUser.uid)
//            query.addListenerForSingleValueEvent(object: ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        binding.tvName.text = snapshot.child(currentUser.uid).child("name").value.toString()
//                        binding.tvContact.text = snapshot.child(currentUser.uid).child("contact").value.toString()
//                        binding.tvUsername.text = snapshot.child(currentUser.uid).child("username").value.toString()
//                    }
//                    else {
//                        Log.d("Aao", "No Data Found")
//                    }
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}