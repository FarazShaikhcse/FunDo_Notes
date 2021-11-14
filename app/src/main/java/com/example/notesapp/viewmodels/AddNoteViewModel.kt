package com.example.notesapp.viewmodels

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.MainActivity
import com.example.notesapp.Service.AuthenticationService
import com.example.notesapp.Service.DatabaseService
import com.example.notesapp.Service.FireBaseDatabase
import com.example.notesapp.Utils.Note
import com.example.notesapp.Utils.SharedPref
import java.time.LocalDateTime

class AddNoteViewModel : ViewModel() {
    private val _databaseNoteAddedStatus = MutableLiveData<Boolean>()
    var databaseNoteAddedStatus = _databaseNoteAddedStatus as LiveData<Boolean>
    private val _databaseNoteUpdatedStatus = MutableLiveData<Boolean>()
    var databaseNoteUpdatedStatus = _databaseNoteUpdatedStatus as LiveData<Boolean>
    private val _databaseNoteDeletedStatus = MutableLiveData<Boolean>()
    var databaseNoteDeletedStatus = _databaseNoteDeletedStatus as LiveData<Boolean>

    @RequiresApi(Build.VERSION_CODES.O)
    fun addNotesToDatabase(note: Note, context: Context) {
            _databaseNoteAddedStatus.value = DatabaseService().addNotesToDatabase(note,context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotesInDatabase(note: Note, context: Context) {
        FireBaseDatabase.updateNotesinDatabase(false, note) {
            _databaseNoteUpdatedStatus.value = it
        }
        val titleText = note.title
        val noteText = note.note
        MainActivity.roomDBClass.noteDao.updateNote(titleText,noteText,
            SharedPref.get("noteid").toString(), LocalDateTime.now().toString()
        )
    }

    fun deleteNotesFromDatabase(titleText: String, noteText: String, requireContext: Context) {
        FireBaseDatabase.tempDeleteNotesFromDatabase(false) {
            _databaseNoteDeletedStatus.value = it
        }

        val uid = AuthenticationService.checkUser()

        if (titleText.isNotEmpty() && noteText.isNotEmpty()) {
            MainActivity.roomDBClass.noteDao.tempDeleteNote(uid.toString(), SharedPref.get("noteid").toString(), true)

        }
    }

}