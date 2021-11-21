package com.example.notesapp.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.MainActivity
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.service.FireBaseDatabase
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.User
import com.google.firebase.auth.FirebaseAuth

class SharedViewModel : ViewModel() {

    private val _gotoHomePageStatus = MutableLiveData<Boolean>()
    val gotoHomePageStatus = _gotoHomePageStatus as LiveData<Boolean>

    private val _gotoLoginPageStatus = MutableLiveData<Boolean>()
    val gotoLoginPageStatus = _gotoLoginPageStatus as LiveData<Boolean>

    private val _gotoRegistrationPageStatus = MutableLiveData<Boolean>()
    val gotoRegistrationPageStatus = _gotoRegistrationPageStatus as LiveData<Boolean>

    private val _gotoResetPasswordPageStatus = MutableLiveData<Boolean>()
    val gotoResetPasswordPageStatus = _gotoResetPasswordPageStatus as LiveData<Boolean>

    private val _gotoFacebookLoginPageStatus = MutableLiveData<Boolean>()
    val gotoFacebookLoginPageStatus = _gotoFacebookLoginPageStatus as LiveData<Boolean>

    private val _databaseRegistrationStatus = MutableLiveData<Boolean>()
    val databaseRegistrationStatus = _databaseRegistrationStatus as LiveData<Boolean>

    private val _gotoAddNotesPageStatus = MutableLiveData<Boolean>()
    val gotoAddNotesPageStatus = _gotoAddNotesPageStatus as LiveData<Boolean>

    private val _gotoDeletedNotesPageStatus = MutableLiveData<Boolean>()
    val gotoDeletedNotesPageStatus = _gotoDeletedNotesPageStatus as LiveData<Boolean>

    private val _gotoAddLabelPageStatus = MutableLiveData<Boolean>()
    val gotoAddLabelPageStatus = _gotoAddLabelPageStatus as LiveData<Boolean>

    private val _gotoArchivedPageStatus = MutableLiveData<Boolean>()
    val gotoArchivedPageStatus = _gotoArchivedPageStatus as LiveData<Boolean>


    fun setGotoHomePageStatus(status: Boolean) {
        Log.d("loginStatus", "Home page involked")
        _gotoHomePageStatus.value = status
    }

    fun setGoToLoginPageStatus(status: Boolean) {
        Log.d("loginStatus", "login page involked")
        _gotoLoginPageStatus.value = status
    }

    fun setGoToRegisterPageStatus(status: Boolean) {
        _gotoRegistrationPageStatus.value = status
    }

    fun setGoToResetPasswordPageStatus(status: Boolean) {
        _gotoResetPasswordPageStatus.value = status
    }

    fun setGoToAddNotesPageStatus(status: Boolean) {
        _gotoAddNotesPageStatus.value = status
    }

    fun setGoToDeletedNotesPageStatus(status: Boolean) {
        _gotoDeletedNotesPageStatus.value = status
    }

    fun setGoToAddLabelPageStatus(status: Boolean) {
        _gotoAddLabelPageStatus.value = status
    }

    fun addUserToDatabase(user: User) {
        FireBaseDatabase.addUser(user) {
            _databaseRegistrationStatus.value = it
        }
    }

    fun getAuth(): FirebaseAuth {
        return AuthenticationService.getAuth()
    }

    fun logout() {
        Log.d("logout",SharedPref.get("fuid").toString()+"test")
        MainActivity.roomDBClass.noteDao.deleteAllUserNotes(SharedPref.get("fuid").toString())
        SharedPref.clearAll()
        AuthenticationService.signout()

    }


    fun getCurrentUid(): String {
        return AuthenticationService?.getCurrentUser()!!.uid.toString()
    }

    fun setGoToArchivedNotesPageStatus(status: Boolean) {
        _gotoArchivedPageStatus.value = status
    }

}
