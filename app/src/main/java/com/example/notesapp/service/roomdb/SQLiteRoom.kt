package com.example.notesapp.service

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.service.roomdb.*


@Database(entities = [NoteEntity::class, UserEntity::class, LabelEntity::class],version = 6)
abstract class RoomDatabase:RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val userDao: UserDao
    abstract val labelDao: LabelDao
}