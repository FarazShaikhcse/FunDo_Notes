package com.example.notesapp.viewmodels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.notesapp.Service.AuthenticationService
import com.example.notesapp.Utils.AuthStatus

class ResetPasswordViewModel: ViewModel() {
    private val _resetPasswordStatus = MutableLiveData<AuthStatus>()
    val resetPasswordStatus = _resetPasswordStatus as LiveData<AuthStatus>

    fun resetPassword(emailValue: String) {
        AuthenticationService.resetPassword(emailValue) {
            _resetPasswordStatus.value = it
        }
    }

}