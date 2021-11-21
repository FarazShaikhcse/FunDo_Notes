package com.example.notesapp.utils

class Note(
    val title: String,
    val note: String,
    val time: String,
    val modifiedTime: String,
    val deleted: Boolean = false,
    val archived: Boolean = false,
    val reminder: Long = 0L
)