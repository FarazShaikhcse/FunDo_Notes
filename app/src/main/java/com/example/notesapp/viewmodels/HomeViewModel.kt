package com.example.notesapp.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.service.FireBaseDatabase
import com.example.notesapp.service.Storage
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.Constants
import com.example.notesapp.utils.SharedPref
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _profilePhotoFetch = MutableLiveData<Uri>()
    val profilePhotoFetch = _profilePhotoFetch as LiveData<Uri>

    private val _profilePhotoUploadStatus = MutableLiveData<Boolean>()
    val profilePhotoUploadStatus = _profilePhotoUploadStatus as LiveData<Boolean>

    private val _databaseReadingStatus = MutableLiveData<Array<String>>()
    val databaseReadingStatus = _databaseReadingStatus as LiveData<Array<String>>

    private val _readNotesFromDatabaseStatus = MutableLiveData<MutableList<NoteEntity>>()
    var readNotesFromDatabaseStatus =
        _readNotesFromDatabaseStatus as LiveData<MutableList<NoteEntity>>

    fun fetchProfile() {
        try {
            Storage.fetchPhoto(AuthenticationService.getCurrentUid()) { status, uri ->
                _profilePhotoFetch.value = uri
            }
        } catch (e: Exception) {
        }
    }

    fun uploadProfile(uid: String?, imageUri: Uri) {
        Storage.uploadImage(uid, imageUri) {
            _profilePhotoUploadStatus.value = it
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readUserFromDatabase(userid: String, context: Context) {
        if (com.example.notesapp.utils.Util.checkInternet(context)) {
            FireBaseDatabase.readUser(userid) { username, email ->
                _databaseReadingStatus.value = arrayOf(username, email)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readNotesFromDatabase(context: Context) {
        viewModelScope.launch {
            if (SharedPref.get("NotesType").toString() == "Reminder") {
                val noteList = DatabaseService().readNotesWithReminder()
                Log.d("listsize", noteList?.size.toString())
                _readNotesFromDatabaseStatus.value = noteList
            } else if (SharedPref.get("NotesType").toString() == "MainNotes") {
                val noteList = DatabaseService().readNotes(false, false)
                Log.d("listsize", noteList.size.toString())
                _readNotesFromDatabaseStatus.value = noteList
            }
            else {
                val noteList = DatabaseService().readNotes(false, true)
                _readNotesFromDatabaseStatus.value = noteList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readNotesFromDatabaseWithPagination(modifiedTime: String, context: Context) {
        viewModelScope.launch {
            var noteList : MutableList<NoteEntity> = ArrayList()
            var noteidList : MutableList<String> = ArrayList()
            if (SharedPref.get(Constants.NOTES_TYPE).toString() == "MainNotes") {
                noteList = DatabaseService().readLimitedNotes(modifiedTime, false, false)!!

            }
            else if(SharedPref.get(Constants.NOTES_TYPE).toString() == "Archived"){
                noteList = DatabaseService().readLimitedNotes(modifiedTime, false, true)!!
            }
            else if(SharedPref.get(Constants.NOTES_TYPE).toString() == "Reminder"){
                noteList = DatabaseService().readReminderNotes(modifiedTime, false, false)!!
            }
            else if(SharedPref.get(Constants.NOTES_TYPE).toString() == "labelnotes"){
                noteidList = FireBaseDatabase.getNotesWithLabel(SharedPref.get("selectedLabel").toString())
                val tempNoteList = DatabaseService().readNotes(false, false)
                Log.d("noteidlist", noteidList.size.toString())
                Log.d("tempnotelist", tempNoteList.size.toString())
                for ( note in tempNoteList){
                    if(note.noteid in noteidList)
                    {
                        noteList.add(note)
                    }
                }
                Log.d("labellednotes", noteList.size.toString())
            }

            _readNotesFromDatabaseStatus.value = noteList
        }
    }

}