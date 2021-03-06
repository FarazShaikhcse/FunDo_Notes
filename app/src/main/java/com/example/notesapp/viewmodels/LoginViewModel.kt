package com.example.notesapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.utils.AuthStatus

class LoginViewModel: ViewModel(){
    private val _loginStatus = MutableLiveData<AuthStatus>()
    val loginStatus = _loginStatus as LiveData<AuthStatus>

    fun loginWithEmailandPassword(email: String, password: String) {
        AuthenticationService.signIn(email, password) {
            _loginStatus.value = it
        }
    }

}