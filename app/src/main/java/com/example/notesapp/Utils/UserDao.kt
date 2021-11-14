package com.example.notesapp.Utils

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface UserDao {

    @Insert
    fun registerUser(user: UserEntity):Long

}