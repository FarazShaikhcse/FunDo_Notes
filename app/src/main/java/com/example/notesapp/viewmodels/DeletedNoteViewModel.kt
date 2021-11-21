package com.example.notesapp.viewmodels

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.MainActivity
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.service.FireBaseDatabase
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.Util
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class DeletedNoteViewModel : ViewModel() {

    private val _readNotesFromDatabaseStatus = MutableLiveData<MutableList<NoteEntity>>()
    var readNotesFromDatabaseStatus =
        _readNotesFromDatabaseStatus as LiveData<MutableList<NoteEntity>>

    private val _restoreNotesStatus = MutableLiveData<Boolean>()
    var restoreNotesStatus = _restoreNotesStatus as LiveData<Boolean>

    private val _permNotesDeleteStatus = MutableLiveData<Boolean>()
    var permNotesDeleteStatus = _permNotesDeleteStatus as LiveData<Boolean>

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readNotesFromDatabase(isDeleted: Boolean, context: Context) {
        viewModelScope.launch {
            val noteList = DatabaseService().readNotes(true, false, context)
            _readNotesFromDatabaseStatus.value = noteList
        }
    }

    fun restoreDeletedNotes(note: NoteEntity, context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.restoreNotesFromDatabase(time, true) {

            }
        }
        _restoreNotesStatus.value = MainActivity.roomDBClass.noteDao.tempDeleteNote(
            SharedPref.get("fuid").toString(),
            SharedPref.get("noteid").toString(), false, false, time
        ) > 0
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun permDeleteNotes(note: NoteEntity, context: Context) {
        if (Util.checkInternet(context)) {
            FireBaseDatabase.permDeleteNotesFromDatabase(true) {
            }
            _permNotesDeleteStatus.value = MainActivity.roomDBClass.noteDao.permDeleteNote(
                note.noteid,
                SharedPref.get("fuid").toString()
            ) > 0
        } else
            _permNotesDeleteStatus.value = false

    }

}