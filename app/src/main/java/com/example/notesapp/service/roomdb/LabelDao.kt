package com.example.notesapp.service.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LabelDao {

    @Insert
    fun addLabel(label: LabelEntity): Long

    @Query("Select * from Label")
    fun getLabels():MutableList<LabelEntity>

}