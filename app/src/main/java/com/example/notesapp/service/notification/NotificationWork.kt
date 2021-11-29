package com.example.notesapp.service.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.notesapp.utils.Note

class NotificationWork(val context: Context, val workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val noteTitle = inputData.getString("noteTitle")
        val noteContent = inputData.getString("noteContent")
        val time = inputData.getString("noteKey")
        val deleted = inputData.getBoolean("isDeleted", false)
        val archived = inputData.getBoolean("isArchived", false)
        val modifiedTime = inputData.getString("modifiedTime")
        val reminder = inputData.getLong("reminder", 0L)
        val note = Note(noteTitle!!,noteContent!!,time!!,modifiedTime!!,deleted,archived,reminder)
        NotificationHelper.createSampleDataNotification(context,note)
        return Result.success()
    }
}