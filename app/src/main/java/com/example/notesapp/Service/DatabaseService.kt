package com.example.notesapp.service

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.notesapp.MainActivity
import com.example.notesapp.service.roomdb.LabelEntity
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.Util
import kotlinx.coroutines.*

class DatabaseService {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addNotesToDatabase(noted: NoteEntity, context: Context): Boolean {
        var firebaseStatus = false
        return withContext(Dispatchers.IO) {
            if (Util.checkInternet(context)) {
                firebaseStatus = FireBaseDatabase.addNotetoDatabase(noted)
            }
            val insertStatus = MainActivity.roomDBClass.noteDao.insertNote(note = noted) > 0
            insertStatus
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun readNotes(isDeleted: Boolean, isArchived: Boolean): MutableList<NoteEntity> {
        return withContext(Dispatchers.IO) {
            val roomNoteList = MainActivity.roomDBClass.noteDao.readNotes(
                SharedPref.get("fuid").toString(),
                isDeleted, isArchived
            )

            roomNoteList
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addRoomNotesToFirebase(
        firebaseNoteList: MutableList<NoteEntity>,
        noteList: MutableList<NoteEntity>
    ) {
        return withContext(Dispatchers.IO) {
            val firebasenoteid = mutableListOf<String>()
            for (note in firebaseNoteList) {
                firebasenoteid.add(note.noteid)
            }
            for (note in noteList) {
                if ((note.noteid !in firebasenoteid)) {

                    val status = FireBaseDatabase.addNotetoDatabase(note)
                    Log.d("addstatus", status.toString())
                } else {
                    val index = firebasenoteid.indexOf(note.noteid)
                    if (firebaseNoteList[index].modifiedTime.compareTo(note.modifiedTime) > 0) {
                        MainActivity.roomDBClass.noteDao.updateNotes(
                            firebaseNoteList[index].title,
                            firebaseNoteList[index].content,
                            firebaseNoteList[index].noteid,
                            firebaseNoteList[index].modifiedTime,
                            firebaseNoteList[index].deleted as Boolean,
                            firebaseNoteList[index].archived as Boolean

                        )

                    } else if (firebaseNoteList[index].modifiedTime.compareTo(note.modifiedTime) < 0) {
                        val updateData = hashMapOf(
                            "modifiedTime" to note.modifiedTime,
                            "note" to note.content,
                            "title" to note.title,
                            "deleted" to note.deleted
                        )
                        val status = FireBaseDatabase.checkNote(note.noteid)
                        status.reference.update(updateData as Map<String, Any>)
                    }
                }

            }
        }
    }

    suspend fun addFirebaseNotesToRoom(
        firebaseNoteList: MutableList<NoteEntity>,
        noteList: MutableList<NoteEntity>
    ) {
        return withContext(Dispatchers.IO) {
            val roomnoteid = mutableListOf<String>()
            for (note in noteList) {
                roomnoteid.add(note.noteid)
            }
            for (note in firebaseNoteList) {

                if (note.noteid in roomnoteid) {
                    val index = roomnoteid.indexOf(note.noteid)
                    if (noteList[index].modifiedTime.compareTo(note.modifiedTime) > 0) {
                        val updateData = hashMapOf(
                            "modifiedTime" to noteList[index].modifiedTime,
                            "note" to noteList[index].content,
                            "title" to noteList[index].title,
                            "deleted" to noteList[index].deleted as Boolean,
                            "archived" to noteList[index].archived as Boolean
                        )
                        FireBaseDatabase.checkNote(note.noteid).reference.update(updateData as Map<String, Any>)
                    } else if (noteList[index].modifiedTime.compareTo(note.modifiedTime) < 0) {
                        MainActivity.roomDBClass.noteDao.updateNotes(
                            note.title,
                            note.content, note.noteid,
                            note.modifiedTime, note.deleted, note.archived
                        )
                    }
                } else if (note.noteid !in roomnoteid) {
                    MainActivity.roomDBClass.noteDao.insertNote(
                        NoteEntity(
                            note.noteid,
                            SharedPref.get("fuid").toString(),
                            note.title,
                            note.content,
                            note.modifiedTime,
                            note.deleted,
                            note.archived
                        )
                    )
                    roomnoteid.add(note.noteid)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sync(context: Context?) {
        GlobalScope.launch(Dispatchers.IO) {
            if (Util.checkInternet(context!!)) {
                var firebaseNoteList = mutableListOf<NoteEntity>()
                if (Util.checkInternet(context)) {
                    Log.d("internet", "entered")
                    firebaseNoteList.addAll(FireBaseDatabase.readAllNotes())
                }
                val roomNoteList = mutableListOf<NoteEntity>()
                roomNoteList.addAll(
                    MainActivity.roomDBClass.noteDao.readAllNotes(
                        SharedPref.get("fuid").toString()
                    )
                )


                Log.d("firebaselistsize", firebaseNoteList.size.toString())
                Log.d("roomlistsize", roomNoteList.size.toString())
                addFirebaseNotesToRoom(firebaseNoteList, roomNoteList)
                addRoomNotesToFirebase(firebaseNoteList, roomNoteList)
            }
        }

    }

    suspend fun getLabelFromDatabase(context: Context): MutableList<String?> {
        return withContext(Dispatchers.IO) {
            var list : MutableList<String?> = arrayListOf()
            if (Util.checkInternet(context)) {
               list = FireBaseDatabase.getLabelsfromFirestore()
            }
            list

        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun addLabelToDatabase(label: LabelEntity, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            var status = false
            if (Util.checkInternet(context)) {
                status = FireBaseDatabase.addLabeltoFirestore(label)
            }
            status
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun deleteLabelFromDB(label: String, context: Context): Boolean? {
        return withContext(Dispatchers.IO) {
            var status = false
            if (Util.checkInternet(context)) {
                status = FireBaseDatabase.deleteLabelFromFirebaseDB(label)
            }
            status
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    suspend fun editLabelinDB(label: String, newLabel:String, context: Context): Boolean? {
        return withContext(Dispatchers.IO) {
            var status = false
            if (Util.checkInternet(context)) {
                status = FireBaseDatabase.editLabelinFirebaseDB(label, newLabel)
            }
            status
        }
    }


    suspend fun readNotesWithReminder(): MutableList<NoteEntity>? {
        return withContext(Dispatchers.IO) {
            val roomNoteList = MainActivity.roomDBClass.noteDao.readReminderNotes(
                SharedPref.get("fuid").toString()
            )

            roomNoteList
        }
    }

    suspend fun readLimitedNotes(modifiedTime: String, isDeleted: Boolean, isArchived: Boolean): MutableList<NoteEntity>? {
        return withContext(Dispatchers.IO) {

            val roomNoteList = FireBaseDatabase.readNotes(modifiedTime, isDeleted, isArchived)
            Log.d("paginationdbserv", roomNoteList.size.toString())
            roomNoteList
        }
    }

    suspend fun readReminderNotes(
        modifiedTime: String,
        isDeleted: Boolean,
        isArchived: Boolean
    ): MutableList<NoteEntity>? {

        return withContext(Dispatchers.IO) {

            val roomNoteList = FireBaseDatabase.readReminderNotes(modifiedTime, isDeleted, isArchived)
            if (roomNoteList != null) {
                Log.d("paginationdbserv", roomNoteList.size.toString())
            }
            roomNoteList
        }
    }

}