package com.example.notesapp.Service


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.notesapp.Utils.Note
import com.example.notesapp.Utils.SharedPref
import com.example.notesapp.Utils.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import java.time.LocalDateTime


class FireBaseDatabase {
    companion object {
        fun addUser(user: User, listner: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val user = hashMapOf(
                "userid" to AuthenticationService.checkUser(),
                "email" to user.email,
                "name" to user.fullName,
                "age" to user.age
            )

//Adding a new document with generated ID
            db.collection("users").document(AuthenticationService.checkUser()!!).set(user)
                .addOnSuccessListener { documentReference ->
                    Log.w("Firebasedatabase", "DocumentSnapshot added with ID:${documentReference}")
                    listner(true)
                }
                .addOnFailureListener { e ->
                    Log.w("Firebasedatabase", "Error adding document", e)
                    listner(false)
                }

        }

        fun readUser(userid: String?, listener: (String, String) -> Unit) {

            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userid.toString())
                .get().addOnSuccessListener {
                    listener(it.get("name").toString(),it.get("email").toString())
                }
                .addOnFailureListener {
                    Log.w("Firebasedatabase", "Error getting documents.", it)

                }
            listener("Username","email")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun addNotetoDatabase(note: Note, listener: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(AuthenticationService.checkUser().toString())
                .collection("notes")
                .document(note.time).set(note).addOnSuccessListener {
                    listener(true)
                }
                .addOnFailureListener {
                    listener(false)
                }


        }

        fun readNotes(isDeleted: Boolean, listener: (Boolean, MutableList<Note>) -> Unit) {
            var list = mutableListOf<Note>()
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(AuthenticationService.checkUser().toString())
                .collection("notes").orderBy("time", Query.Direction.DESCENDING)
                .whereEqualTo("deleted",isDeleted)
                .get().addOnSuccessListener { result ->
                    for (doc in result) {
                        val title = doc.get("title").toString()
                        val note = doc.get("note").toString()
                        val time = doc.get("time").toString()
                        val modifiedTime = doc.get("modifiedTime").toString()
                        val userNote = Note(title, note, time, modifiedTime)
                        list.add(userNote)
                    }
                    listener(true, list)
                }
                .addOnFailureListener { exception ->
                    Log.w("Firebasedatabase", "Error getting documents.", exception)
                    listener(false, list)
                }
        }

        fun getSelectedNotesDocReference(isDeleted: Boolean, listener: (DocumentReference?) -> Unit) {
            val pos = SharedPref.getUpdateNotePosition("position")
            val db = FirebaseFirestore.getInstance()
//            var count = 1
//            var reference: DocumentReference? = null
//            db.collection("users").document(Authentication.checkUser().toString())
//                .collection("notes").orderBy("time", Query.Direction.DESCENDING)
//                .whereEqualTo("deleted",isDeleted)
//                .get().addOnSuccessListener { result ->
//                    for (doc in result) {
//                        if (count == pos) {
//                            listener(doc.reference)
//                            break
//                        }
//                        count += 1
//                    }
//                }.addOnFailureListener {
//                    listener(null)
//                }
            Log.d("FirebaseReference",SharedPref.get("noteid").toString())
            db.collection("users").document(AuthenticationService.checkUser().toString())
                .collection("notes").document(SharedPref.get("noteid").toString()).get()
                .addOnSuccessListener {
                        result -> listener(result.reference)
                }
                .addOnFailureListener{
                    listener(null)
                    Log.w("Firebasedatabase", "get selected notes Error getting document"+it)
                }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun updateNotesinDatabase(isDeleted: Boolean, note: Note, listener: (Boolean) -> Unit) {
            val updateData = hashMapOf(
                "modifiedTime" to LocalDateTime.now().toString(),
                "note" to note.note,
                "title" to note.title
            )
            getSelectedNotesDocReference(isDeleted) {
                if (it != null) {
                    it.update(updateData as Map<String, Any>).addOnSuccessListener {
                        listener(true)
                    }.addOnFailureListener {
                        listener(false)
                        Log.w("Firebasedatabase", "update notes Error getting document"+it)
                    }
                } else
                    listener(false)
            }
        }


        fun tempDeleteNotesFromDatabase(isDeleted: Boolean, listener: (Boolean) -> Unit) {

            getSelectedNotesDocReference(isDeleted) {
                if (it != null)
                    it.update("deleted",!isDeleted)
                        .addOnFailureListener {
                            listener(false)
                        }.addOnSuccessListener {
                            listener(true)
                        }
                else
                    listener(false)
            }

        }
        fun restoreNotesFromDatabase(isDeleted: Boolean, note: Note, listener: (Boolean) -> Unit) {

            getSelectedNotesDocReference(!isDeleted) {
                if (it != null)
                    it.update("deleted",isDeleted)
                        .addOnFailureListener {
                            listener(false)
                        }.addOnSuccessListener {
                            listener(true)
                        }
                else
                    listener(false)
            }

        }

        fun addFbDataToDB(jsonObject: JSONObject?){
            val user = hashMapOf(
                "userid" to AuthenticationService.checkUser(),
                "email" to jsonObject?.getString("email"),
                "name" to (jsonObject?.getString("name")),
                "age" to "age"
            )
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(AuthenticationService.checkUser()!!).set(user)
                .addOnSuccessListener { documentReference ->
                    Log.w("Firebasedatabase", "DocumentSnapshot added with ID:${documentReference}")

                }
                .addOnFailureListener { e ->
                    Log.w("Firebasedatabase", "Error adding document", e)

                }
        }

        fun permDeleteNotesFromDatabase(isDeleted: Boolean, note: Note, listener: (Boolean) -> Unit) {
            getSelectedNotesDocReference(isDeleted) {
                if (it != null)
                    it.delete()
                        .addOnFailureListener {
                            listener(false)
                            Log.w("Firebasedatabase", "delete error")
                        }.addOnSuccessListener {
                            listener(true)
                        }
                else
                    listener(false)
            }
        }

    }


}

