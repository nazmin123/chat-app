package com.example.chatapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.example.chatapp.activities.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage






class MyFirebaseMessaging: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val sented = remoteMessage.data["sented"]
        val user = remoteMessage.data["user"]
        val preferences =
            getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUser = preferences.getString("currentuser", "none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && sented == firebaseUser.uid) {
           if (currentUser != user) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
           }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token
        if (firebaseUser != null) {
            updateToken(refreshToken!!)
        }
    }
    private fun updateToken(refreshToken: String) {
        if (FirebaseAuth.getInstance().currentUser != null)
        {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().getReference("Tokens")
            val token = Token(refreshToken)
            reference.child(firebaseUser!!.uid).setValue(token)
        }
    }

    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.getData().get("user")
        val icon = remoteMessage.getData().get("icon")
        val title = remoteMessage.getData().get("title")
        val body = remoteMessage.getData().get("body")
        val notification = remoteMessage.getNotification()
        val j = Integer.parseInt(user!!.replace(("[\\D]").toRegex(), ""))
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val oreoNotification = OreoNotification1(this)
        val builder = oreoNotification.getOreoNotification(
            title!!, body!!, pendingIntent,
            defaultSound, icon!!
        )

        var i = 0
        if (j > 0)
        {
            i = j
        }
        oreoNotification.manager!!.notify(i, builder.build())
    }
    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val notification = remoteMessage.notification
        val j = Integer.parseInt(user!!.replace(("[\\D]").toRegex(), ""))
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid", user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(Integer.parseInt(icon))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j > 0)
        {
            i = j
        }
        noti.notify(i, builder.build())
    }

}