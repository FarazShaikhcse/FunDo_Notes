package com.example.notesapp.service.roomdb

import androidx.room.*

@Dao
interface NoteDao {

    @Query("Select * from notes where deleted=:isDeleted  and archived=:isArchived " +
            "ORDER BY modifiedtime DESC")
    fun readNotes(isDeleted: Boolean, isArchived: Boolean):MutableList<NoteEntity>

    @Query("Select * from notes where uid=:uid")
    fun readAllNotes(uid: String):MutableList<NoteEntity>

    @Insert
    fun insertNote(note: NoteEntity):Long

    @Query("Update notes set deleted=:isDeleted, modifiedtime=:time, archived=:isArchived " +
            "where uid=:uid and noteid=:noteid")
    fun tempDeleteNote(uid: String, noteid: String, isDeleted: Boolean, isArchived: Boolean,time: String):Int

    @Query("delete from notes where  uid=:uid and noteid=:noteid")
    fun permDeleteNote(noteid: String, uid: String):Int

    @Query("delete from notes where uid=:uid")
    fun deleteAllUserNotes(uid: String):Int

    @Query("Update notes set title=:title,content=:note,modifiedtime=:modifiedTime where noteid=:key")
    fun updateNote(title:String,note:String,key: String,modifiedTime: String):Int

    @Query("Update notes set title=:title,content=:note,modifiedtime=:modifiedTime,deleted=:isDeleted," +
            "archived=:isArchived where noteid=:key")
    fun updateNotes(title:String,note:String,key: String,modifiedTime: String,isDeleted: Boolean, isArchived: Boolean):Int

    @Query("Update notes set archived=:isArchived, modifiedtime=:time where uid=:uid and noteid=:noteid")
    fun archiveNotes(uid: String, noteid: String, isArchived: Boolean, time: String):Int

    @Query("Select * from notes where reminder!=:reminder")
    fun readReminderNotes(reminder: Long = 0L):MutableList<NoteEntity>
}