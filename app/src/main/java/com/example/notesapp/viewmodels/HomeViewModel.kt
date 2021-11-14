package com.example.notesapp.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.Service.AuthenticationService
import com.example.notesapp.Service.FireBaseDatabase
import com.example.notesapp.Service.Storage
import com.example.notesapp.Utils.Note

class HomeViewModel: ViewModel(){
    private val _profilePhotoFetch= MutableLiveData<Uri>()
    val profilePhotoFetch=_profilePhotoFetch as LiveData<Uri>

    private val _profilePhotoUploadStatus=MutableLiveData<Boolean>()
    val profilePhotoUploadStatus = _profilePhotoUploadStatus as LiveData<Boolean>

    private val _databaseReadingStatus = MutableLiveData<Array<String>>()
    val databaseReadingStatus = _databaseReadingStatus as LiveData<Array<String>>

    private val _readNotesFromDatabaseStatus = MutableLiveData<MutableList<Note>>()
    var readNotesFromDatabaseStatus=_readNotesFromDatabaseStatus as LiveData<MutableList<Note>>

    fun fetchProfile() {
        try {
            Storage.fetchPhoto(AuthenticationService.getCurrentUid()) { status, uri ->
                _profilePhotoFetch.value = uri
            }
        }
        catch (e: Exception){
        }
    }
    fun uploadProfile(uid: String?, imageUri: Uri) {
        Storage.uploadImage(uid,imageUri){
            _profilePhotoUploadStatus.value=it
        }
    }

    fun readUserFromDatabase(userid: String) {
        FireBaseDatabase.readUser(userid) { username, email ->
            _databaseReadingStatus.value = arrayOf(username,email)
        }
    }

    fun readNotesFromDatabase(isDeleted: Boolean){
        FireBaseDatabase.readNotes(isDeleted){ status, list->
            if(status) {
                _readNotesFromDatabaseStatus.value = list
            }
        }
    }

}