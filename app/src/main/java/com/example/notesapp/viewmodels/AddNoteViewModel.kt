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
import com.example.notesapp.utils.Note
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.Util
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AddNoteViewModel : ViewModel() {
    private val _databaseNoteAddedStatus = MutableLiveData<Boolean>()
    var databaseNoteAddedStatus = _databaseNoteAddedStatus as LiveData<Boolean>
    private val _databaseNoteUpdatedStatus = MutableLiveData<Boolean>()
    var databaseNoteUpdatedStatus = _databaseNoteUpdatedStatus as LiveData<Boolean>
    private val _databaseNoteDeletedStatus = MutableLiveData<Boolean>()
    var databaseNoteDeletedStatus = _databaseNoteDeletedStatus as LiveData<Boolean>
    private val _databaseNoteArchivedStatus = MutableLiveData<Boolean>()
    var databaseNoteArchivedStatus = _databaseNoteArchivedStatus as LiveData<Boolean>
    private val _unarchiveNotesStatus = MutableLiveData<Boolean>()
    var unarchiveNotesStatus = _unarchiveNotesStatus as LiveData<Boolean>


    @RequiresApi(Build.VERSION_CODES.O)
    fun addNotesToDatabase(note: NoteEntity, context: Context) {
        viewModelScope.launch {
            _databaseNoteAddedStatus.value = DatabaseService().addNotesToDatabase(note, context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotesInDatabase(note: Note, context: Context) {
        viewModelScope.launch {
            if (Util.checkInternet(context)) {
                FireBaseDatabase.updateNotesinDatabase(false, note)
            }
            val titleText = note.title
            val noteText = note.note
            _databaseNoteUpdatedStatus.value = MainActivity.roomDBClass.noteDao.updateNote(
                titleText, noteText,
                SharedPref.get("noteid").toString(), note.modifiedTime
            ) > 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun deleteNotesFromDatabase(context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.tempDeleteNotesFromDatabase(false, time) {
            }
        }
        _databaseNoteDeletedStatus.value = MainActivity.roomDBClass.noteDao.tempDeleteNote(
            SharedPref.get("fuid").toString(),
            SharedPref.get("noteid").toString(), true, false, time
        ) > 0

    }

    fun linkNotesandLabels(noteid: String, labelsList: MutableList<String>, context: Context) {
        viewModelScope.launch {
            FireBaseDatabase.linkNotesandLabels(noteid, labelsList)
        }
    }

    fun archiveNotes(titleText: String, noteText: String, isArchived: Boolean, context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.archiveNotesInDatabase(isArchived, time)
        }

        if (titleText.isNotEmpty() && noteText.isNotEmpty()) {
            _databaseNoteArchivedStatus.value = MainActivity.roomDBClass.noteDao.archiveNotes(
                SharedPref.get("fuid").toString(),
                SharedPref.get("noteid").toString(), isArchived, time
            ) > 0

        }
    }

    fun unArchiveNotes( context: Context) {
        val time = LocalDateTime.now().toString()
        if (Util.checkInternet(context)) {
            FireBaseDatabase.archiveNotesInDatabase(false, time)
        }
        _unarchiveNotesStatus.value = MainActivity.roomDBClass.noteDao.archiveNotes(
            SharedPref.get("fuid").toString(),
            SharedPref.get("noteid").toString(), false, time
        ) > 0
    }

}