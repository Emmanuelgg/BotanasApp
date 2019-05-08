package com.example.botanas.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.botanas.MainActivity
import com.example.botanas.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*




class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "botanasapp"
    private val vibratePattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Log.i(TAG, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let { message ->
            Log.i(TAG, message.getData().get("message"))

            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //Setting up Notification channels for android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                setupNotificationChannels()

            val notificationId = Random().nextInt(60000)

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_botanas_round)  //a resource for your custom small icon
                .setContentTitle(message.data["title"]) //the "title" value you sent in your notification
                .setContentText(message.data["message"]) //ditto
                .setAutoCancel(true)  //dismisses the notification on click
                .setSound(defaultSoundUri)
                .setVibrate(vibratePattern)

            //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build())

        }

    }

    
    fun sendCustomMessage(title: String, message: String, context: Context) {
       val notificationId = Random().nextInt(60000)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setupNotificationChannels(context)


        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
            this.putExtra("openSales", true)
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_botanas_round)  //a resource for your custom small icon
            .setContentTitle(title) //the "title" value you sent in your notification
            .setContentText(message) //ditto
            .setAutoCancel(true)  //dismisses the notification on click
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(notifyPendingIntent)
            .setSound(defaultSoundUri)
            .setVibrate(vibratePattern)
            //.addAction(R.drawable.ic_cloud, context.getString(R.string.sync), notifyPendingIntent)
            //.setProgress(100,0,true)

        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels(context: Context? = null) {
        var adminChannelName: String
        var adminChannelDescription: String
        if  (context != null) {
            val appContext = context.applicationContext as Context
            adminChannelName = appContext.getString(R.string.notifications_admin_channel_name)
            adminChannelDescription = appContext.getString(R.string.notifications_admin_channel_description)
        } else {
            adminChannelName = getString(R.string.notifications_admin_channel_name)
            adminChannelDescription = getString(R.string.notifications_admin_channel_description)
        }


        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.BLUE
        adminChannel.enableVibration(true)
        adminChannel.vibrationPattern = vibratePattern
        notificationManager.createNotificationChannel(adminChannel)
    }
}