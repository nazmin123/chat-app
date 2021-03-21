package com.example.chatapp.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_reset_password.*


class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        btn_reset.setOnClickListener {
            reset_progress.visibility= View.VISIBLE
            btn_reset.visibility= View.GONE
            val email = send_email.text.toString()
            if (email == "")
            {
                Toast.makeText(this@ResetPasswordActivity, "All fileds are required!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reset_progress.visibility= View.GONE
                            btn_reset.visibility= View.VISIBLE
                            Toast.makeText(this@ResetPasswordActivity, "Please check you Email", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                        }
                        else
                        {
                            reset_progress.visibility= View.GONE
                            btn_reset.visibility= View.VISIBLE
                            val error = task.exception!!.message
                            Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}