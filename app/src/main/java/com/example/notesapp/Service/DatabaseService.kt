package com.example.notesapp.Service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.notesapp.MainActivity
import com.example.notesapp.Utils.Note
import com.example.notesapp.Utils.NoteEntity
import com.example.notesapp.Utils.Util

class DatabaseService {
    @RequiresApi(Build.VERSION_CODES.O)
    fun addNotesToDatabase(note: Note,context: Context): Boolean {
        if(Util.checkInternet(context)) {
            FireBaseDatabase.addNotetoDatabase(note) {
            }
        }
        val titleText = note.title
        val noteText = note.note
        val uid = AuthenticationService.checkUser()

        val noteEntity = NoteEntity(note.time,uid.toString(), titleText, noteText,note.time, false)
        val insertStatus = MainActivity.roomDBClass.noteDao.insertNote(note = noteEntity)
        return insertStatus.toInt() > 0

    }
}