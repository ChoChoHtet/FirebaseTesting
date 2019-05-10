package com.android.chatapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.android.chatapp.ForgotPasswordActivity
import com.android.chatapp.PhoneNumberActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService() {
    val TAG="Service"
    /**
     * received message from FCM
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        if(remoteMessage!=null){
            sendNotificationTo(remoteMessage)
            val intent = Intent(this@MyFirebaseMessagingService, PhoneNumberActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("message", remoteMessage.data["content"])
            Log.e("Message", "${remoteMessage.data["content"]}")
            startActivity(intent)
        }
        else  Log.e("Message", "Failed")

    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    private fun sendNotificationTo(remoteMessage: RemoteMessage?) {
        val intent=Intent(this,ForgotPasswordActivity::class.java)
        val pendingIntent=PendingIntent.getActivity(applicationContext,0,intent,0)
        val title=remoteMessage!!.data["title"]
        val content=remoteMessage.data["content"]
        val notiManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL="Chat App"
       if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
           var channel=NotificationChannel(NOTIFICATION_CHANNEL,"Notification",NotificationManager.IMPORTANCE_LOW)
           channel.description="Notification channel"
           channel.enableLights(true)
           channel.lightColor=Color.RED
           channel.vibrationPattern= longArrayOf(0,1000,500,500)
           channel.enableVibration(true)
           notiManager.createNotificationChannel(channel)
       }
        val notificationBuilder=NotificationCompat.Builder(this,NOTIFICATION_CHANNEL)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(android.support.v4.R.drawable.notification_icon_background)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
        notiManager.notify(1,notificationBuilder.build())

    }
}