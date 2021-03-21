package com.example.chatapp.activities

import android.R.attr
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login1.*
import kotlinx.android.synthetic.main.activity_login1.btn_login
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.email
import kotlinx.android.synthetic.main.activity_register.password


class RegisterActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var reference: DatabaseReference
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

//        setSupportActionBar(toolbar)
//        supportActionBar!!.setTitle("Register");
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener {
            val txt_username = username.text.toString()
            val txt_email = email.text.toString()
            val txt_password= password.text.toString()
            register_progress.visibility= View.VISIBLE
            btn_register.visibility=View.GONE
            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(
                    txt_password
                )
            ) {
                Toast.makeText(
                    this@RegisterActivity,
                    "All fields are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (txt_password.length < 6) {
                Toast.makeText(
                    this@RegisterActivity,
                    "password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                register(txt_username, txt_email, txt_password)
            }
        }

    }
//    private fun register(
//        username: String,
//        email: String,
//        password: String
//    ) {
//        auth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val firebaseUser = auth.currentUser!!
//                    val userid = firebaseUser.uid
//                    reference =
//                        FirebaseDatabase.getInstance().getReference("Users").child(userid)
//                    val hashMap: HashMap<String, String> = HashMap()
//                    hashMap["id"] = userid
//                    hashMap["username"] = username
//                    hashMap["imageURL"] = "default"
//                    hashMap["status"] = "offline"
//                    hashMap["search"] = username.toLowerCase()
//                    reference.setValue(hashMap)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                val intent =
//                                    Intent(this@RegisterActivity, MainActivity::class.java)
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                                startActivity(intent)
//                                finish()
//                            }
//                        }
//                } else {
//                    Toast.makeText(
//                        this@RegisterActivity,
//                        "You can't register woth this email or password",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//    }
private fun register(username:String, email:String, password:String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(object: OnCompleteListener<AuthResult> {
            override fun onComplete(@NonNull task: Task<AuthResult>) {
                if (task.isSuccessful)
                {
                    register_progress.visibility= View.GONE
                    btn_register.visibility=View.VISIBLE
                    val firebaseUser = auth.currentUser
                    assert(firebaseUser != null)
                    val userid = firebaseUser!!.uid
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap.put("id", userid)
                    hashMap.put("username", username)
                    hashMap.put("imageURL", "default")
                    hashMap.put("status", "offline")
                    hashMap.put("search", username.toLowerCase())
                    hashMap.put("email",email)
                    reference.setValue(hashMap).addOnCompleteListener(object:
                        OnCompleteListener<Void> {
                        override fun onComplete(@NonNull task:Task<Void>) {
                            if (task.isSuccessful)
                            {
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                    })
                }
                else
                {
                    register_progress.visibility= View.GONE
                    btn_register.visibility=View.VISIBLE
                    Toast.makeText(this@RegisterActivity, "You can't register worth this email or password", Toast.LENGTH_SHORT).show()
                }
            }
        })
}
}