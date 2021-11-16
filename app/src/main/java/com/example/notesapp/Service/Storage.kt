package com.example.notesapp.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class Storage {
    companion object {

        fun uploadImage(uid: String?, imageUri: Uri, listener: (Boolean) -> Unit) {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("User_PFP/" + uid + ".jpg")
            if (imageUri != null) {
                fileRef.putFile(imageUri).addOnSuccessListener {
                    listener(true)
                }
            }
            listener(false)
        }

        fun fetchPhoto(currentUid: String, listener: (Boolean, Uri?) -> Unit) {
            val storageRef = FirebaseStorage.getInstance().reference

            val fileRef = storageRef.child("User_PFP/" + currentUid + ".jpg")

            fileRef.downloadUrl.addOnSuccessListener {
                listener(true, it)
            }
                .addOnFailureListener {
                    listener(false, null)
                }


            listener(false, null)


        }
    }
}