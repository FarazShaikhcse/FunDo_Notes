package com.example.notesapp.service


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.notesapp.utils.Note
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import java.time.LocalDateTime
import kotlin.coroutines.suspendCoroutine


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
                    listener(it.get("name").toString(), it.get("email").toString())
                }
                .addOnFailureListener {
                    Log.w("Firebasedatabase", "Error getting documents.", it)

                }
            listener("Username", "email")
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun addNotetoDatabase(note: NoteEntity): Boolean {
            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()

                db.collection("users").document(AuthenticationService.checkUser().toString())
                    .collection("notes")
                    .document(note.noteid).set(Note(note.title, note.content, note.noteid,
                        note.noteid)).addOnSuccessListener {
                        cont.resumeWith(Result.success(true))
                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }

        suspend fun readNotes(isDeleted: Boolean): MutableList<NoteEntity>{
            return suspendCoroutine { cont ->
                var list = mutableListOf<NoteEntity>()
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(AuthenticationService.checkUser().toString())
                    .collection("notes").orderBy("time", Query.Direction.DESCENDING)
                    .whereEqualTo("deleted", isDeleted)
                    .get().addOnSuccessListener { result ->
                        for (doc in result) {
                            val title = doc.get("title").toString()
                            val note = doc.get("note").toString()
                            val time = doc.get("time").toString()
                            val modifiedTime = doc.get("modifiedTime").toString()
                            val userNote = NoteEntity(
                                time, SharedPref.get("fuid").toString(),
                                title, note, modifiedTime, isDeleted
                            )
                            list.add(userNote)
                        }
                        cont.resumeWith(Result.success(list))
                    }
                    .addOnFailureListener { exception ->
                        Log.w("Firebasedatabase", "Error getting documents.", exception)
                        cont.resumeWith(Result.failure(exception))
                    }
            }
        }

        fun getSelectedNotesDocReference(
            isDeleted: Boolean,
            listener: (DocumentReference?) -> Unit
        ) {
            val pos = SharedPref.getUpdateNotePosition("position")
            val db = FirebaseFirestore.getInstance()
            Log.d("FirebaseReference", SharedPref.get("noteid").toString())
            db.collection("users").document(AuthenticationService.checkUser().toString())
                .collection("notes").document(SharedPref.get("noteid").toString()).get()
                .addOnSuccessListener { result ->
                    listener(result.reference)
                }
                .addOnFailureListener {
                    listener(null)
                    Log.w("Firebasedatabase", "get selected notes Error getting document" + it)
                }

        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun updateNotesinDatabase(isDeleted: Boolean, note: Note, listener: (Boolean) -> Unit) {
            val updateData = hashMapOf(
                "modifiedTime" to note.modifiedTime,
                "note" to note.note,
                "title" to note.title
            )
            getSelectedNotesDocReference(isDeleted) {
                if (it != null) {
                    it.update(updateData as Map<String, Any>).addOnSuccessListener {
                        listener(true)
                    }.addOnFailureListener {
                        listener(false)
                        Log.w("Firebasedatabase", "update notes Error getting document" + it)
                    }
                } else
                    listener(false)
            }
        }


        fun tempDeleteNotesFromDatabase(isDeleted: Boolean, time: String, listener: (Boolean) -> Unit) {
            val updateData = hashMapOf(
                "modifiedTime" to time,
                "deleted" to !isDeleted
            )
            getSelectedNotesDocReference(isDeleted) {
                if (it != null)
                    it.update(updateData as Map<String, Any>)
                        .addOnFailureListener {
                            listener(false)
                        }.addOnSuccessListener {
                            listener(true)
                        }
                else
                    listener(false)
            }

        }

        fun restoreNotesFromDatabase(
            time: String,
            isDeleted: Boolean,
            listener: (Boolean) -> Unit
        ) {
            val updateData = hashMapOf(
                "modifiedTime" to time,
                "deleted" to !isDeleted
            )
            getSelectedNotesDocReference(!isDeleted) {
                if (it != null)
                    it.update(updateData as Map<String, Any>)
                        .addOnFailureListener {
                            listener(false)
                        }.addOnSuccessListener {
                            listener(true)
                        }
                else
                    listener(false)
            }

        }

        fun addFbDataToDB(jsonObject: JSONObject?) {
            val user = hashMapOf(
                "userid" to AuthenticationService.checkUser(),
                "email" to jsonObject?.getString("email"),
                "name" to (jsonObject?.getString("name")),
                "age" to "age"
            )
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(AuthenticationService.checkUser()!!).set(user)
                .addOnSuccessListener { documentReference ->
                    Log.w(
                        "Firebasedatabase",
                        "DocumentSnapshot added with ID:${documentReference}"
                    )

                }
                .addOnFailureListener { e ->
                    Log.w("Firebasedatabase", "Error adding document", e)

                }
        }

        fun permDeleteNotesFromDatabase(
            isDeleted: Boolean,
            listener: (Boolean) -> Unit
        ) {
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

        suspend fun checkNote(noteid: String): DocumentSnapshot {
            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(AuthenticationService.checkUser().toString())
                    .collection("notes").document(noteid).get()
                    .addOnSuccessListener {
                        if(it.exists())
                            cont.resumeWith(Result.success(it))

                    }
                    .addOnFailureListener{
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }

    }


}


