package com.example.notesapp.Utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid")
    var uid: Long = 0L,
    @ColumnInfo(name = "firebaseuid") var firebaseuid: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "age") var age: String,
    @ColumnInfo(name = "email") var email : String
)
