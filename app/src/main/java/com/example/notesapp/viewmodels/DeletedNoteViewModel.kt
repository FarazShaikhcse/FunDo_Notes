package com.example.notesapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.MainActivity
import com.example.notesapp.Service.AuthenticationService
import com.example.notesapp.Service.FireBaseDatabase
import com.example.notesapp.Utils.Note
import com.example.notesapp.Utils.SharedPref

class DeletedNoteViewModel: ViewModel() {

    private val _readNotesFromDatabaseStatus = MutableLiveData<MutableList<Note>>()
    var readNotesFromDatabaseStatus = _readNotesFromDatabaseStatus as LiveData<MutableList<Note>>

    private val _restoreNotesStatus = MutableLiveData<Boolean>()
    var restoreNotesStatus = _restoreNotesStatus as LiveData<Boolean>

    private val _permNotesDeleteStatus = MutableLiveData<Boolean>()
    var permNotesDeleteStatus = _permNotesDeleteStatus as LiveData<Boolean>

    fun readNotesFromDatabase(isDeleted: Boolean){
        FireBaseDatabase.readNotes(isDeleted){ status, list->
            if(status) {
                _readNotesFromDatabaseStatus.value = list
            }
        }
    }

    fun restoreDeletedNotes(note: Note) {
        FireBaseDatabase.restoreNotesFromDatabase(false, note){
            if(it)
                _restoreNotesStatus.value = it
        }
        MainActivity.roomDBClass.noteDao.tempDeleteNote(SharedPref.get("fuid").toString(), SharedPref.get("noteid").toString(), false)
    }

    fun permDeleteNotes(note: Note) {
        FireBaseDatabase.permDeleteNotesFromDatabase(true, note){
            if(it)
                _permNotesDeleteStatus.value = it
        }
        MainActivity.roomDBClass.noteDao.permDeleteNote(note.time, SharedPref.get("fuid").toString())
    }

}