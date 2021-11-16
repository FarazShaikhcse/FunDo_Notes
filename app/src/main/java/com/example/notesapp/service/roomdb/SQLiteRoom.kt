package com.example.notesapp.service

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notesapp.service.roomdb.NoteDao
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.service.roomdb.UserDao
import com.example.notesapp.service.roomdb.UserEntity


@Database(entities = [NoteEntity::class, UserEntity::class],version = 5)
abstract class RoomDatabase:RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val userDao: UserDao
}