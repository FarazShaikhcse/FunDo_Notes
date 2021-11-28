package com.example.notesapp.service.notification

import android.R
import android.app.Notification
import android.app.NotificationManager

import android.app.PendingIntent

import com.example.notesapp.MainActivity

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.notesapp.utils.SharedPref


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, "reminder")
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(intent.getStringExtra("titleExtra"))
            .setContentText(intent.getStringExtra("noteExtra"))
            .setStyle(NotificationCompat.BigTextStyle().bigText(intent.getStringExtra("noteExtra")))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(SharedPref.getInt("notificationID"), notification)
    }


}
