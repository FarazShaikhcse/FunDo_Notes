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
import com.example.notesapp.utils.SharedPref
import kotlinx.coroutines.launch
import okhttp3.internal.Util

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
                val noteList = DatabaseService().readNotes(false, false, context)
                Log.d("listsize", noteList.size.toString())
                _readNotesFromDatabaseStatus.value = noteList
            }
            else {
                val noteList = DatabaseService().readNotes(false, true, context)
                _readNotesFromDatabaseStatus.value = noteList
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun readNotesFromDatabaseWithPagination(modifiedTime: String, context: Context) {
        viewModelScope.launch {
            if (SharedPref.get("NotesType").toString() == "MainNotes") {
                val noteList = DatabaseService().readLimitedNotes(modifiedTime)
                Log.d("listsize", noteList?.size.toString())
                _readNotesFromDatabaseStatus.value = noteList
            }
            else {
                readNotesFromDatabase(context)
            }
        }
    }

}