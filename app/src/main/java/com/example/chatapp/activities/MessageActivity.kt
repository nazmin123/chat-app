package com.example.chatapp.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.bumptech.glide.Glide
import com.example.chatapp.Adapters.MessageAdapter
import com.example.chatapp.R
import com.example.chatapp.model.Chat
import com.example.chatapp.model.User
import com.example.chatapp.notifications.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_message.*
import org.json.JSONException
import org.json.JSONObject



class MessageActivity : AppCompatActivity() {
    lateinit var fuser: FirebaseUser
    lateinit var reference: DatabaseReference
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAUJdkdLg:APA91bHCAaVvEtNSajNq-0hutB-VopcLkmuA7Rtcoy7G-BQ4-LNVx-p80fMwGkOuJMF0A2AHGRdckboEUY8c_ofwL0oQBZOtbbfSE1X3U_9v_bcpaL0VFtlZ9y0KjOWlY-HUHX8-0DdV"
    private val contentType = "application/json"
    lateinit var messageAdapter: MessageAdapter
    var notify = false
     var userid=""
   lateinit var seenListener: ValueEventListener
    var mchat: ArrayList<Chat> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        setSupportActionBar(toolbar)
       userid= intent.getStringExtra("userid").toString()
       fuser = FirebaseAuth.getInstance().currentUser!!
       reference = FirebaseDatabase.getInstance().getReference("Users").child(userid)
       FirebaseMessaging.getInstance().subscribeToTopic("/topics/New Message")
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        btn_send.setOnClickListener {
           notify=true
           val msg = text_send.text.toString()
           if (msg != "") {
               sendMessage(fuser.uid, userid, msg)
           } else {
               Toast.makeText(
                   this@MessageActivity,
                   "You can't send empty message",
                   Toast.LENGTH_SHORT
               ).show()
           }
           text_send.setText("")
       }
        toolbar.setNavigationOnClickListener {
            onBackPressed() }
       recycler_view.setHasFixedSize(true)
       val linearLayoutManager=LinearLayoutManager(applicationContext)
       linearLayoutManager.stackFromEnd = true;
       recycler_view.layoutManager=linearLayoutManager
       reference.addValueEventListener(object : ValueEventListener {
           override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
               val user =
                   dataSnapshot.getValue(
                       User::class.java
                   )!!
               username.text = user.username
               if (user.imageURL == "default") {
                   profile_image.setImageResource(R.drawable.ic_person)
               } else {

                   //change this
                   Glide.with(applicationContext).load(user.imageURL).circleCrop().into(profile_image)
               }
               readMesagges(fuser.uid, userid, user.imageURL);
           }

           override fun onCancelled(@NonNull databaseError: DatabaseError) {}
       })
       seenMessage(userid)
    }
    private fun currentUser(userid: String) {
        val editor =
            getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        editor.putString("currentuser", userid)
        editor.apply()
    }
    private fun status(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid())
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["status"] = status
        reference.updateChildren(hashMap)
    }
    override fun onResume() {
        super.onResume()
        status("online")
        currentUser(userid)
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener)
        status("offline")
        currentUser("none")
    }
    private fun sendMessage(sender:String, receiver:String, message:String) {
        var reference = FirebaseDatabase.getInstance().reference
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["isseen"] = false
        reference.child("Chats").push().setValue(hashMap)
        // add user to chat fragment
        val chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(fuser.uid)
            .child(userid)
        chatRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot:DataSnapshot) {
                if (!dataSnapshot.exists())
                {
                    chatRef.child("id").setValue(userid)
                }
            }
            override fun onCancelled(@NonNull databaseError:DatabaseError) {
            }
        })
        val chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(userid)
            .child(fuser.uid)
        chatRefReceiver.child("id").setValue(fuser.uid)
        val msg = message
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.uid)
        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot:DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (notify)
                {
                    sendNotifiaction(receiver, user!!.username, msg)
                }
                notify = false
            }
            override fun onCancelled(@NonNull databaseError:DatabaseError) {
            }
        })

    }
    private fun sendNotifiaction(receiver:String, username:String, message:String) {
        val tokens = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = tokens.orderByKey().equalTo(receiver)
        val temp = FirebaseInstanceId.getInstance().token
        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot:DataSnapshot) {
                for (snapshot in dataSnapshot.children)
                {
                    val token = snapshot.getValue(Token::class.java)
                    val data = Data(fuser.uid, R.mipmap.ic_launcher, username + ": " + message, "New Message",
                        userid)
                    val sender = Sender(data, token!!.token!!)

                    try {
                        val senderjosn = JSONObject(Gson().toJson(sender))
                        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, senderjosn,
                            com.android.volley.Response.Listener<JSONObject> { response ->
                                Log.i("TAG", "onResponse: $response")
                                text_send.setText("")
                            },
                            com.android.volley.Response.ErrorListener {
                                Toast.makeText(this@MessageActivity, "Request error", Toast.LENGTH_LONG).show()
                                Log.i("TAG", "onErrorResponse: Didn't work")
                            }) {

                            override fun getHeaders(): Map<String, String> {
                                val params = HashMap<String, String>()
                                params["Authorization"] = serverKey
                                params["Content-Type"] = contentType
                                return params
                            }
                        }
                        requestQueue.add(jsonObjectRequest)

                    }catch (e:JSONException)
                    {
                        e.printStackTrace()
                    }
                }
            }
            override fun onCancelled(@NonNull databaseError:DatabaseError) {
            }
        })
    }
private fun seenMessage(userid: String) {
    reference = FirebaseDatabase.getInstance().getReference("Chats")
    seenListener = reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
            for (snapshot in dataSnapshot.getChildren()) {
                val chat = snapshot.getValue(Chat::class.java)
                if (chat!!.receiver == fuser.uid && chat.sender == userid) {
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap.put("isseen", true)
                    snapshot.ref.updateChildren(hashMap)
                }
            }
        }

        override fun onCancelled(@NonNull databaseError: DatabaseError) {
        }
    })
}
private fun readMesagges(myid: String, userid: String, imageurl: String) {
    mchat = ArrayList()
    reference = FirebaseDatabase.getInstance().getReference("Chats")
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
            mchat.clear()
            for (snapshot in dataSnapshot.children) {
                val chat = snapshot.getValue(Chat::class.java)
                if ((chat!!.receiver == myid && chat.sender.equals(userid) || chat.receiver.equals(
                        userid
                    ) && chat.sender.equals(myid))
                ) {
                    mchat.add(chat)
                }
                messageAdapter = MessageAdapter(mchat, this@MessageActivity, imageurl)
                recycler_view.adapter = messageAdapter
            }
        }

        override fun onCancelled(@NonNull databaseError: DatabaseError) {
        }
    })
}
}