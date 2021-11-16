package com.example.notesapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.MainActivity
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.utils.AuthStatus
import com.example.notesapp.service.roomdb.UserEntity

class RegistrationViewModel: ViewModel() {
    private val _registrationStatus = MutableLiveData<AuthStatus>()
    val registrationStatus = _registrationStatus as LiveData<AuthStatus>

    fun registerUser(email: String, password: String, fullNameValue: String, ageValue: String) {
        AuthenticationService.registerUser(email, password) {
            if(it.status)
                MainActivity.roomDBClass.userDao.registerUser(
                    UserEntity(firebaseuid = AuthenticationService.checkUser().toString(),
                        name = fullNameValue, age = ageValue, email = email)
                )
            _registrationStatus.value = it
        }


    }
}