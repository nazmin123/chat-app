package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashScreenActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    private lateinit var logoLoader: ImageView
    private lateinit var logo: ImageView
    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //check if user is null
        if (firebaseUser != null) {
            val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        logoLoader = findViewById(R.id.loaderBg)
        window.statusBarColor = Color.parseColor("#FFFFFF");

        logo = findViewById(R.id.logo_view)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //check if user is null
        if (firebaseUser == null) {
            Handler(Looper.getMainLooper()).postDelayed({
                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                    finish()
                }

            }, 4000)
        }


        logoLoader.animate().scaleX(0.0f).scaleY(0.0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).duration = 0
        logoLoader.animate().scaleX(+4.5f).scaleY(+4.5f)
            .setInterpolator(AccelerateDecelerateInterpolator()).duration = 2000

        logo.animate().scaleX(+2.0f).scaleY(+2.0f)
            .setInterpolator(AccelerateDecelerateInterpolator()).duration = 2200

        Handler().postDelayed({
            logoLoader.animate().scaleX(+2.2f).scaleY(+2.2f)
                .setInterpolator(AccelerateDecelerateInterpolator()).duration = 200
        }, 2000)

        Handler().postDelayed({
            logoLoader.animate().scaleX(+30f).scaleY(+30f)
                .setInterpolator(AccelerateDecelerateInterpolator()).duration = 500
        }, 2300)
    }
}