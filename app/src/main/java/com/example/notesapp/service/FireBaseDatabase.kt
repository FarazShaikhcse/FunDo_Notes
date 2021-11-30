package com.example.notesapp.service


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.notesapp.service.roomdb.LabelEntity
import com.example.notesapp.utils.Note
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.Constants
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine


class FireBaseDatabase {
    companion object {
        lateinit var dbref: DocumentSnapshot
        fun addUser(user: User, listner: (Boolean) -> Unit) {
            val db = FirebaseFirestore.getInstance()
            val user = hashMapOf(
                Constants.USERID to AuthenticationService.checkUser(),
                Constants.EMAIL to user.email,
                Constants.NAME to user.fullName,
                Constants.AGE to user.age
            )

            db.collection(Constants.USERS).document(AuthenticationService.checkUser()!!).set(user)
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

            db.collection(Constants.USERS).document(userid.toString())
                .get().addOnSuccessListener {
                    listener(it.get(Constants.NAME).toString(), it.get(Constants.EMAIL).toString())
                }
                .addOnFailureListener {
                    Log.w("Firebasedatabase", "Error getting documents.", it)

                }
            listener(Constants.NAME, Constants.EMAIL)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        suspend fun addNotetoDatabase(note: NoteEntity): Boolean {
            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()

                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.NOTES)
                    .document(note.noteid).set(
                        Note(
                            note.title, note.content, note.noteid,
                            note.noteid, note.deleted, note.archived, note.reminder
                        )
                    ).addOnSuccessListener {
                        cont.resumeWith(Result.success(true))
                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }

        suspend fun readNotes(
            modifiedTime: String,
            isDeleted: Boolean,
            isArchived: Boolean
        ): MutableList<NoteEntity> {
            return suspendCoroutine { cont ->
                var list = mutableListOf<NoteEntity>()
                val db = FirebaseFirestore.getInstance()
                if (modifiedTime == "") {
                    db.collection(Constants.USERS)
                        .document(AuthenticationService.checkUser().toString())
                        .collection(Constants.NOTES)
                        .whereEqualTo(Constants.DELETED, isDeleted)
                        .whereEqualTo(Constants.ARCHIVED, isArchived)
                        .orderBy(Constants.MODIFIEDTIME, Query.Direction.DESCENDING)
                        .limit(10)
                        .get().addOnSuccessListener { result ->
                            for (doc in result) {
                                val title = doc.get(Constants.TITLE).toString()
                                val note = doc.get("note").toString()
                                val time = doc.get(Constants.TIME).toString()
                                val modifiedTime1 = doc.get(Constants.MODIFIEDTIME).toString()
                                val reminder = doc.get(Constants.REMINDER) as Long
                                val userNote = NoteEntity(
                                    time, SharedPref.get("fuid").toString(),
                                    title, note, modifiedTime1, isDeleted, isArchived, reminder
                                )
                                list.add(userNote)
                                dbref = doc
                            }

                            cont.resumeWith(Result.success(list))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebasedatabase", "Error getting documents.", exception)
                            cont.resumeWith(Result.failure(exception))
                        }

                } else {
                    db.collection(Constants.USERS)
                        .document(AuthenticationService.checkUser().toString())
                        .collection(Constants.NOTES)
                        .whereEqualTo(Constants.DELETED, isDeleted)
                        .whereEqualTo(Constants.ARCHIVED, isArchived)
                        .orderBy(Constants.MODIFIEDTIME, Query.Direction.DESCENDING)
                        .startAfter(dbref)
                        .limit(10)
                        .get().addOnSuccessListener { result ->
                            for (doc in result) {
                                val title = doc.get(Constants.TITLE).toString()
                                val note = doc.get(Constants.NOTE).toString()
                                val time = doc.get(Constants.TIME).toString()
                                val modifiedTime2 = doc.get(Constants.MODIFIEDTIME).toString()
                                val reminder = doc.get(Constants.REMINDER) as Long
                                val userNote = NoteEntity(
                                    time, SharedPref.get("fuid").toString(),
                                    title, note, modifiedTime2, isDeleted, isArchived, reminder
                                )
                                list.add(userNote)
                                dbref = doc
                            }
                            cont.resumeWith(Result.success(list))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebasedatabase", "Error getting documents.", exception)
                            cont.resumeWith(Result.failure(exception))
                        }
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
            db.collection(Constants.USERS).document(AuthenticationService.checkUser().toString())
                .collection(Constants.NOTES).document(SharedPref.get("noteid").toString()).get()
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
                Constants.MODIFIEDTIME to note.modifiedTime,
                Constants.NOTE to note.note,
                Constants.TITLE to note.title
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


        fun tempDeleteNotesFromDatabase(
            isDeleted: Boolean,
            time: String,
            listener: (Boolean) -> Unit
        ) {
            val updateData = hashMapOf(
                Constants.MODIFIEDTIME to time,
                Constants.DELETED to !isDeleted
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
                Constants.MODIFIEDTIME to time,
                Constants.DELETED to !isDeleted
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
                Constants.USERID to AuthenticationService.checkUser(),
                Constants.EMAIL to jsonObject?.getString(Constants.EMAIL),
                Constants.NAME to (jsonObject?.getString(Constants.NAME)),
                Constants.AGE to Constants.AGE
            )
            val db = FirebaseFirestore.getInstance()
            db.collection(Constants.USERS).document(AuthenticationService.checkUser()!!).set(user)
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
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.NOTES).document(noteid).get()
                    .addOnSuccessListener {
                        if (it.exists())
                            cont.resumeWith(Result.success(it))

                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }

        suspend fun addLabeltoFirestore(label: LabelEntity): Boolean {
            return suspendCoroutine { cont ->
                val labelmap = hashMapOf(
                    "labelname" to label.labelname
                )
                val db = FirebaseFirestore.getInstance()
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.LABELS).document().set(labelmap)
                    .addOnSuccessListener {
                        cont.resumeWith(Result.success(true))
                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }

        suspend fun getLabelsfromFirestore(): MutableList<String?> {
            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()
                var list = mutableListOf<String?>()
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.LABELS)
                    .get().addOnSuccessListener {
                        for (doc in it) {
                            val labelname = doc.get("labelname")
                            list.add(labelname.toString())
                        }
                        cont.resumeWith(Result.success(list))
                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }

            }
        }

        suspend fun deleteLabelFromFirebaseDB(label: String): Boolean {
            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.LABELS).whereEqualTo("labelname", label).get()
                    .addOnSuccessListener {
                        it.documents[0].reference.delete()
                            .addOnSuccessListener {
                                cont.resumeWith(Result.success(true))
                            }
                            .addOnFailureListener {
                                cont.resumeWith(Result.failure(it))
                            }

                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }

        }

        suspend fun editLabelinFirebaseDB(label: String, newLabel: String): Boolean {

            return suspendCoroutine { cont ->
                val db = FirebaseFirestore.getInstance()
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.LABELS).whereEqualTo("labelname", label).get()
                    .addOnSuccessListener {
                        it.documents[0].reference.update("labelname", newLabel)
                            .addOnSuccessListener {
                                cont.resumeWith(Result.success(true))
                            }
                            .addOnFailureListener {
                                cont.resumeWith(Result.failure(it))
                            }

                    }
                    .addOnFailureListener {
                        cont.resumeWith(Result.failure(it))
                    }
            }
        }


        fun linkNotesandLabels(noteid: String, labelsList: MutableList<String>) {
            val db = FirebaseFirestore.getInstance()
            for (label in labelsList) {
                val data = hashMapOf(
                    "labelname" to label,
                    "noteid" to noteid
                )
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection("noteLabelReln").document().set(data)
            }
        }

        fun getLabelId(label: String): DocumentReference? {
            val db = FirebaseFirestore.getInstance()
            var documentRef: DocumentReference? = null
            db.collection(Constants.USERS).document(AuthenticationService.checkUser().toString())
                .collection(Constants.LABELS).whereEqualTo("labelname", label).get()
                .addOnSuccessListener {
                    documentRef = it.documents[0].reference
                }
            return documentRef
        }

        fun getNotesWithLabel(label: String): MutableList<NoteEntity> {
            val db = FirebaseFirestore.getInstance()
            val noteList: MutableList<NoteEntity> = ArrayList()
            val noteidlist: MutableList<String> = ArrayList()
            val uid = AuthenticationService.checkUser().toString()
            db.collection(Constants.USERS).document(uid)
                .collection("noteLabelReln").get().addOnSuccessListener {
                    for (i in it) {
                        if (i.get("labelname").toString() == label) {
                            noteidlist.add(i.get("noteid").toString())
                        }
                    }
                }
            db.collection(Constants.USERS).document(uid)
                .collection(Constants.NOTES).whereEqualTo(Constants.DELETED, false)
                .get().addOnSuccessListener {
                    for (i in it) {
                        if (i.get(Constants.TIME).toString() in noteidlist) {
                            noteList.add(
                                NoteEntity(
                                    i.getString(Constants.TIME)!!,
                                    uid,
                                    i.getString(Constants.TITLE)!!,
                                    i.getString(Constants.NOTE)!!,
                                    i.getString(Constants.MODIFIEDTIME)!!,
                                    false
                                )
                            )
                        }
                    }
                }
            return noteList
        }

        fun archiveNotesInDatabase(isArchived: Boolean, time: String) {
            val updateData = hashMapOf(
                Constants.MODIFIEDTIME to time,
                Constants.ARCHIVED to isArchived
            )
            getSelectedNotesDocReference(isArchived) {
                if (it != null)
                    it.update(updateData as Map<String, Any>)
            }

        }

        suspend fun readAllNotes(): MutableList<NoteEntity> {
            return suspendCoroutine { cont ->
                var list = mutableListOf<NoteEntity>()
                val db = FirebaseFirestore.getInstance()
                db.collection(Constants.USERS)
                    .document(AuthenticationService.checkUser().toString())
                    .collection(Constants.NOTES).orderBy(Constants.TIME, Query.Direction.DESCENDING)
                    .get().addOnSuccessListener { result ->
                        for (doc in result) {
                            val title = doc.get(Constants.TITLE).toString()
                            val note = doc.get(Constants.NOTE).toString()
                            val time = doc.get(Constants.TIME).toString()
                            val modifiedTime = doc.get(Constants.MODIFIEDTIME).toString()
                            val isDeleted = doc.get(Constants.DELETED) as Boolean
                            val isArchived = doc.get(Constants.ARCHIVED) as Boolean
                            val userNote = NoteEntity(
                                time, SharedPref.get("fuid").toString(),
                                title, note, modifiedTime, isDeleted, isArchived
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

        suspend fun readReminderNotes(
            modifiedTime: String,
            isDeleted: Boolean,
            isArchived: Boolean
        ): MutableList<NoteEntity>? {
            return suspendCoroutine { cont ->
                var list = mutableListOf<NoteEntity>()
                val db = FirebaseFirestore.getInstance()

                if (modifiedTime == "") {
                    db.collection(Constants.USERS)
                        .document(AuthenticationService.checkUser().toString())
                        .collection(Constants.NOTES)
                        .whereNotEqualTo(Constants.REMINDER, 0)
                        .orderBy(Constants.REMINDER, Query.Direction.DESCENDING)
                        .limit(10)
                        .get().addOnSuccessListener { result ->
                            for (doc in result) {
                                val title = doc.get(Constants.TITLE).toString()
                                val note = doc.get(Constants.NOTE).toString()
                                val time = doc.get(Constants.TIME).toString()
                                val modifiedTime1 = doc.get(Constants.MODIFIEDTIME).toString()
                                val reminder = doc.get(Constants.REMINDER) as Long
                                val userNote = NoteEntity(
                                    time, SharedPref.get("fuid").toString(),
                                    title, note, modifiedTime1, isDeleted, isArchived, reminder
                                )
                                list.add(userNote)
                                dbref = doc
                            }

                            cont.resumeWith(Result.success(list))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebasedatabase", "Error getting documents.", exception)
                            cont.resumeWith(Result.failure(exception))
                        }

                } else {
                    db.collection(Constants.USERS)
                        .document(AuthenticationService.checkUser().toString())
                        .collection(Constants.NOTES)
                        .whereNotEqualTo(Constants.REMINDER, 0)
                        .orderBy(Constants.REMINDER, Query.Direction.DESCENDING)
                        .startAfter(dbref)
                        .limit(10)
                        .get().addOnSuccessListener { result ->
                            for (doc in result) {
                                val title = doc.get(Constants.TITLE).toString()
                                val note = doc.get(Constants.NOTE).toString()
                                val time = doc.get(Constants.TIME).toString()
                                val modifiedTime2 = doc.get(Constants.MODIFIEDTIME).toString()
                                val reminder = doc.get(Constants.REMINDER) as Long
                                val userNote = NoteEntity(
                                    time, SharedPref.get("fuid").toString(),
                                    title, note, modifiedTime2, isDeleted, isArchived, reminder
                                )
                                list.add(userNote)
                                dbref = doc
                            }
                            cont.resumeWith(Result.success(list))
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Firebasedatabase", "Error getting documents.", exception)
                            cont.resumeWith(Result.failure(exception))
                        }
                }
            }
        }
    }
}

