package com.example.notesapp.service

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.notesapp.MainActivity
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
    suspend fun readNotes(isDeleted: Boolean, context: Context): MutableList<NoteEntity> {
        // var tempList = mutableListOf<NotesKey>()
//        var listFromRoom: MutableList<NoteEntity>
//
//        return withContext(Dispatchers.IO) {
//            listFromRoom =
//                MainActivity.roomDBClass.noteDao.readNotes(SharedPref.get("fuid").toString(), isDeleted)
//            Log.d("roomNoteSize", listFromRoom.size.toString())
//
//            for (i in listFromRoom) {
//                val note = NotesKey(i.title, i.note, i.fid, i.deleted, i.modifiedTime)
//                tempList.add(note)
//            }
//            listFromRoom
//        }
        return withContext(Dispatchers.IO) {
            val roomNoteList = MainActivity.roomDBClass.noteDao.readNotes(
                SharedPref.get("fuid").toString(),
                isDeleted
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
                    Log.d("addstatus",status.toString())
                } else {
                    val index = firebasenoteid.indexOf(note.noteid)
                    if (firebaseNoteList[index].modifiedTime.compareTo(note.modifiedTime) > 0) {
                        MainActivity.roomDBClass.noteDao.updateNotes(
                            firebaseNoteList[index].title,
                            firebaseNoteList[index].content, firebaseNoteList[index].noteid,
                            firebaseNoteList[index].modifiedTime, firebaseNoteList[index].deleted as Boolean
                        )

                    } else if(firebaseNoteList[index].modifiedTime.compareTo(note.modifiedTime) < 0) {
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
                            "deleted" to noteList[index].deleted as Boolean
                        )
                        FireBaseDatabase.checkNote(note.noteid).reference.update(updateData as Map<String, Any>)
                    } else if (noteList[index].modifiedTime.compareTo(note.modifiedTime) < 0) {
                        MainActivity.roomDBClass.noteDao.updateNotes(
                            note.title,
                            note.content, note.noteid,
                            note.modifiedTime, note.deleted
                        )
                    }
                } else {
                    MainActivity.roomDBClass.noteDao.insertNote(
                        NoteEntity(
                            note.noteid,
                            SharedPref.get("fuid").toString(),
                            note.title,
                            note.content,
                            note.modifiedTime,
                            note.deleted
                        )
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sync(context: Context?) {
        GlobalScope.launch(Dispatchers.IO){
            if (Util.checkInternet(context!!)) {
                var firebaseNoteList = mutableListOf<NoteEntity>()
                if (Util.checkInternet(context)) {
                    Log.d("internet", "entered")
                    firebaseNoteList.addAll(FireBaseDatabase.readNotes(false))
                    firebaseNoteList.addAll(FireBaseDatabase.readNotes(true))
                }
                val roomNoteList = mutableListOf<NoteEntity>()
                roomNoteList.addAll(
                    MainActivity.roomDBClass.noteDao.readNotes(
                        SharedPref.get("fuid").toString(),
                        false
                    )
                )

                roomNoteList.addAll(
                    MainActivity.roomDBClass.noteDao.readNotes(
                        SharedPref.get("fuid").toString(),
                        true
                    )
                )

                Log.d("firebaselistsize", firebaseNoteList.size.toString())
                Log.d("roomlistsize", roomNoteList.size.toString())
                addFirebaseNotesToRoom(firebaseNoteList, roomNoteList)
                addRoomNotesToFirebase(firebaseNoteList, roomNoteList)
            }
        }

    }

}