package com.example.notesapp.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.notesapp.MainActivity
import com.example.notesapp.R

class NotificationService(context: Context) {
    fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean, name: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = "${context.packageName}-$name"
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createSampleDataNotification(context: Context): NotificationCompat.Builder {
        val channelId = "${context?.packageName}-${context?.getString(R.string.app_name)}"
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_baseline_alarm_24) // 3
            setContentTitle("Notes Title") // 4
            setContentText("content") // 5
            setStyle(NotificationCompat.BigTextStyle().bigText("Notes Content")) // 6
            priority = NotificationCompat.PRIORITY_DEFAULT // 7
            setAutoCancel(false) // 8
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            setContentIntent(pendingIntent)

        }
        return notificationBuilder
    }
}