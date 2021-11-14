package com.example.notesapp.Service

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.Utils.NoteDao
import com.example.notesapp.Utils.NoteEntity
import com.example.notesapp.Utils.UserDao
import com.example.notesapp.Utils.UserEntity


@Database(entities = [NoteEntity::class, UserEntity::class],version = 5)
abstract class RoomDatabase:RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val userDao: UserDao
}