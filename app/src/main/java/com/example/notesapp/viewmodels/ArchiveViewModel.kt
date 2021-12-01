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

class ArchiveViewModel: ViewModel() {
    private val _readNotesFromDatabaseStatus = MutableLiveData<MutableList<NoteEntity>>()
    var readNotesFromDatabaseStatus =
        _readNotesFromDatabaseStatus as LiveData<MutableList<NoteEntity>>

    private val _unarchiveNotesStatus = MutableLiveData<Boolean>()
    var unarchiveNotesStatus = _unarchiveNotesStatus as LiveData<Boolean>

    private val _tempNotesDeleteStatus = MutableLiveData<Boolean>()
    var tempNotesDeleteStatus = _tempNotesDeleteStatus as LiveData<Boolean>

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readNotesFromDatabase( context: Context) {
        viewModelScope.launch {
            val noteList = DatabaseService().readNotes(false, true)
            _readNotesFromDatabaseStatus.value = noteList
        }
    }

    fun unArchiveNotes(note: NoteEntity, context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.archiveNotesInDatabase(false, time)
        }
        _unarchiveNotesStatus.value = MainActivity.roomDBClass.noteDao.archiveNotes(
            SharedPref.get("fuid").toString(),
            SharedPref.get("noteid").toString(), false, time
        ) > 0
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun tempDeleteNotes(note: NoteEntity, context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.tempDeleteNotesFromDatabase(false, time) {
            }
            _tempNotesDeleteStatus.value = MainActivity.roomDBClass.noteDao.tempDeleteNote(
                SharedPref.get("fuid").toString(),
                note.noteid,
                true,
                false,
                time
            ) > 0
        } else
            _tempNotesDeleteStatus.value = false

    }
}