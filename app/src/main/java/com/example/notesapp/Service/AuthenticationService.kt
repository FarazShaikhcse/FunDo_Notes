package com.example.notesapp.service

import android.util.Log
import com.example.notesapp.utils.AuthStatus
import com.facebook.*
import com.google.firebase.auth.FirebaseAuth
import com.facebook.login.LoginManager

object AuthenticationService {
    var uid: String = ""
    private lateinit var firebaseAuth: FirebaseAuth
    var validFbuser =
        AccessToken.getCurrentAccessToken() != null && AccessToken.getCurrentAccessToken()?.isExpired == false

    fun signIn(email: String, password: String, listener: (AuthStatus) -> Unit) {
        Log.d("signin", "Inside sign of firebase")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = AuthStatus(true, "Login successful")
                    listener(user)
                } else {
                    val user = AuthStatus(false, it.exception?.message.toString())
                    listener(user)
                }
            }

    }

    fun registerUser(email: String, password: String, listener: (AuthStatus) -> Unit) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    //listener(true,"Registration successful")
                    listener(AuthStatus(true, "Registration successful"))
                } else {
                    //listener(false,it.exception?.message.toString())
                    listener(AuthStatus(false, it.exception?.message.toString()))

                }
            }
    }

    fun checkUser(): String? {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance()!!.getCurrentUser()!!.uid

        } else if (AccessToken.getCurrentAccessToken() != null && AccessToken.getCurrentAccessToken()?.isExpired == false)
            return AccessToken.getCurrentAccessToken()!!.userId
        else
            return null
    }

    fun signout() {

        if (AccessToken.getCurrentAccessToken() != null && AccessToken.getCurrentAccessToken()?.isExpired == false)
            LoginManager.getInstance().logOut()
        else
            FirebaseAuth.getInstance().signOut()
    }


    fun resetPassword(emailValue: String, listener: (AuthStatus) -> Unit) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val authStatus =
                        AuthStatus(true, "Check your mail,reset password link has been sent")
                    //listener(true,"Check your mail,reset password link has been sent")
                    listener(authStatus)
                } else {
                    val authStatus = AuthStatus(false, task.exception?.message.toString())
                    listener(authStatus)
                    //listener(false,task.exception?.message.toString())
                }

            }
    }

    fun getCurrentUser() = FirebaseAuth.getInstance().currentUser
    fun getAuth() = FirebaseAuth.getInstance()
    fun getCurrentUid(): String {
        if (getAuth().uid != null)
            return FirebaseAuth.getInstance()!!.uid.toString()
        return ""
    }

}


