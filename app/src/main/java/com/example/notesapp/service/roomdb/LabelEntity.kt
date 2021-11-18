package com.example.notesapp.service.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Label")
data class LabelEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "labelid") var labelid: Long = 1L,
    @ColumnInfo(name = "labelname") var labelname: String,
)