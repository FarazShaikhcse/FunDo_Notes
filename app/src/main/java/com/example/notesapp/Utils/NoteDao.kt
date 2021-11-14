package com.example.notesapp.Utils

import androidx.room.*

@Dao
interface NoteDao {


    @Query("Select * from notes where uid=:uid")
    fun readNotes(uid:String):MutableList<NoteEntity>

    @Insert
    fun insertNote(note:NoteEntity):Long

    @Query("Update notes set deleted=:isDeleted where uid=:uid and noteid=:noteid")
    fun tempDeleteNote(uid: String, noteid: String, isDeleted: Boolean):Int

    @Query("delete from notes where  uid=:uid and noteid=:noteid")
    fun permDeleteNote(noteid: String, uid: String):Int

    @Query("delete from notes where uid=:uid")
    fun deleteAllUserNotes(uid: String):Int

    @Query("Update notes set title=:title,content=:note,modifiedtime=:modifiedTime where noteid=:key")
    fun updateNote(title:String,note:String,key: String,modifiedTime: String):Int
}