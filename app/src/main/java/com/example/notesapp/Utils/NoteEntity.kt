package com.example.notesapp.Utils

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey @ColumnInfo(name = "noteid") var noteid: String,
    @ColumnInfo(name = "uid") var uid: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "modifiedtime") var modifiedTime: String,
    @ColumnInfo(name = "deleted") var deleted: Boolean
)
