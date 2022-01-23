package com.example.frauddetection.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.frauddetection.MainActivity
import com.example.frauddetection.R
import com.example.frauddetection.home.model.MessageModel
import com.example.frauddetection.utils.FraudDetection

class MessageDetectionService: Service() {

    val CHANNEL_ID = "2"
    private lateinit var builder: NotificationCompat.Builder

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStart(intent: Intent?, startId: Int) {
//        val filter = IntentFilter()
//        val receiver = object: BroadcastReceiver() {
//            override fun onReceive(p0: Context?, p1: Intent?) {
//                Log.d("receiver", "Helloo")
//                notificationBuilder("Title", "New Message Arrived")
//            }
//        }
//
//        registerReceiver(receiver, filter)
        createNotificationChannel()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            checkForMessage()
        }
    }

    private fun checkForMessage() {
        Handler().postDelayed(Runnable {
            checkForMessage()
        }, 5000)
        val inboxUri: Uri = Uri.parse("content://sms/inbox")
        val reqCols = arrayOf("_id", "address", "body")

        val cr: ContentResolver = contentResolver

        val messageList: MutableList<MessageModel> = mutableListOf()

        val c: Cursor? = cr.query(inboxUri, reqCols, null, null, null)
        try {
            while (c?.moveToNext()!!) {
                val detectionResult =
                    FraudDetection.detectFromSenderAndMessage(c.getString(1), c.getString(2))
                if (detectionResult != "0% Fraud") {
                    messageList.add(
                        MessageModel(
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            detectionResult
                        )
                    )
                }
            }
            Log.d("sizes", "${getSharedPreferences("messageCount", Context.MODE_PRIVATE).getInt("count", 0)} and ${messageList.size}")
            if (getSharedPreferences("messageCount", Context.MODE_PRIVATE).getInt("count", 0) != messageList.size) {
                notificationBuilder("New Message Arrived from ${messageList[0].title}", FraudDetection.detectFromSenderAndMessage(messageList[0].title, messageList[0].body))
                getSharedPreferences("messageCount", Context.MODE_PRIVATE)?.edit()?.putInt("count", messageList.size)?.apply()
            }
        } catch (e: Exception) {}

    }

    override fun onDestroy() {
        super.onDestroy()
        createNotificationChannel()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            checkForMessage()
        }
    }

    private fun notificationBuilder(title: String, message: String) {
        // Create an explicit intent for an Activity in your app

        // to opem app
        val openApp = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
        sendNotification()
    }

    private fun sendNotification() {
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}