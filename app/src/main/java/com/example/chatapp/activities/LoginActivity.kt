package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.btn_login
import kotlinx.android.synthetic.main.activity_login.email
import kotlinx.android.synthetic.main.activity_login.forgot_password
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login1.*
import android.view.View
import kotlinx.android.synthetic.main.activity_start.register


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login1)
        window.statusBarColor = Color.parseColor("#FF22B2FF");
        auth = FirebaseAuth.getInstance();
   forgot_password.setOnClickListener {
       startActivity(
           Intent(
               this@LoginActivity,
               ResetPasswordActivity::class.java
           )
       ) }
        register.setOnClickListener {  startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) }
        btn_login.setOnClickListener {
            login_progress.visibility= View.VISIBLE
            btn_login.visibility=View.GONE
            val txt_email: String = email.getText().toString()
            val txt_password: String = password.getText().toString()
            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                Toast.makeText(
                    this@LoginActivity,
                    "All fileds are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth!!.signInWithEmailAndPassword(txt_email, txt_password)
                    .addOnCompleteListener(object: OnCompleteListener<AuthResult> {
                        override fun onComplete(@NonNull task:Task<AuthResult>) {
                            if (task.isSuccessful)
                            {
                                login_progress.visibility= View.GONE
                                btn_login.visibility=View.VISIBLE
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                            else
                            {
                                login_progress.visibility= View.GONE
                                btn_login.visibility=View.VISIBLE
                                Toast.makeText(this@LoginActivity, "Authentication failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            }
        }
    }
}