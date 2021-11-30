package com.example.notesapp.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.notesapp.MainActivity
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.Constants
import com.example.notesapp.utils.Note

object NotificationHelper {

    fun createNotificationChannel(
        context: Context,
        importance: Int,
        showBadge: Boolean,
        name: String,
        description: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = Constants.CHANNEL_ID
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            channel.setShowBadge(showBadge)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createSampleDataNotification(context: Context, note : Note) {

        val channelId = Constants.CHANNEL_ID

        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.drawable.ic_baseline_alarm_24)
            setContentTitle(note.title)
            setContentText(note.note)
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)

            val bundle = Bundle().apply {
                putString("Destination","userNote")
                putSerializable("reminderNote",note)
            }
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtras(bundle)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getActivity(context, 0, intent,
                   PendingIntent.FLAG_UPDATE_CURRENT)
            }
            setContentIntent(pendingIntent)

        }
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, notificationBuilder.build())
    }



}
