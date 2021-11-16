package com.example.notesapp.service.roomdb

import androidx.room.*

@Dao
interface NoteDao {


    @Query("Select * from notes where uid=:uid and deleted=:isDeleted")
    fun readNotes(uid:String, isDeleted: Boolean):MutableList<NoteEntity>

    @Query("Select * from notes where noteid=:noteid")
    fun readNote(noteid: String):MutableList<NoteEntity>

    @Query("Select * from notes where uid=:uid")
    fun readAllNotes(uid: String):MutableList<NoteEntity>

    @Insert
    fun insertNote(note: NoteEntity):Long

    @Query("Update notes set deleted=:isDeleted, modifiedtime=:time where uid=:uid and noteid=:noteid")
    fun tempDeleteNote(uid: String, noteid: String, isDeleted: Boolean, time: String):Int

    @Query("delete from notes where  uid=:uid and noteid=:noteid")
    fun permDeleteNote(noteid: String, uid: String):Int

    @Query("delete from notes where uid=:uid")
    fun deleteAllUserNotes(uid: String):Int

    @Query("Update notes set title=:title,content=:note,modifiedtime=:modifiedTime where noteid=:key")
    fun updateNote(title:String,note:String,key: String,modifiedTime: String):Int

    @Query("Update notes set title=:title,content=:note,modifiedtime=:modifiedTime,deleted=:isDeleted where noteid=:key")
    fun updateNotes(title:String,note:String,key: String,modifiedTime: String,isDeleted: Boolean):Int
}