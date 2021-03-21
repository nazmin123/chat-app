package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //check if user is null
        if (firebaseUser != null) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        login.setOnClickListener {
            startActivity(Intent(this@StartActivity, LoginActivity::class.java))
        }
        register.setOnClickListener {  startActivity(Intent(this@StartActivity, RegisterActivity::class.java)) }

    }

}