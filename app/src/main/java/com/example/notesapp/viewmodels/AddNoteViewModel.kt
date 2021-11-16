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

    @RequiresApi(Build.VERSION_CODES.O)
    fun addNotesToDatabase(note: NoteEntity, context: Context) {
        viewModelScope.launch {
            _databaseNoteAddedStatus.value = DatabaseService().addNotesToDatabase(note, context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotesInDatabase(note: Note, context: Context) {

        if(Util.checkInternet(context)) {
            FireBaseDatabase.updateNotesinDatabase(false, note) {
            }
        }
        val titleText = note.title
        val noteText = note.note
        _databaseNoteUpdatedStatus.value = MainActivity.roomDBClass.noteDao.updateNote(titleText,noteText,
            SharedPref.get("noteid").toString(), note.modifiedTime) > 0
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun deleteNotesFromDatabase(titleText: String, noteText: String, context: Context) {
        val time = LocalDateTime.now().toString()
        if(Util.checkInternet(context)) {
            FireBaseDatabase.tempDeleteNotesFromDatabase(false, time) {
            }
        }

        if (titleText.isNotEmpty() && noteText.isNotEmpty()) {
            _databaseNoteDeletedStatus.value = MainActivity.roomDBClass.noteDao.tempDeleteNote(
                SharedPref.get("fuid").toString(),
                SharedPref.get("noteid").toString(), true, time)>0

        }
    }

}