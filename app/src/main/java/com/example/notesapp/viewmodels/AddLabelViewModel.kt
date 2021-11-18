package com.example.notesapp.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.service.roomdb.LabelEntity
import com.example.notesapp.service.roomdb.NoteEntity
import kotlinx.coroutines.launch

class AddLabelViewModel : ViewModel() {
    private val _labelAddedToDbStatus = MutableLiveData<Boolean>()
    var labelAddedToDbStatus = _labelAddedToDbStatus as LiveData<Boolean>
    private val _getLabelStatus = MutableLiveData<MutableList<String?>>()
    var getLabelStatus = _getLabelStatus as LiveData<MutableList<String?>>

    fun addLabelToDatabase(label: LabelEntity, context: Context) {
        viewModelScope.launch {
            _labelAddedToDbStatus.value = DatabaseService().addLabelToDatabase(label, context)
        }
    }
    fun getLabelsFromDatabase(context: Context) {
        viewModelScope.launch {
            _getLabelStatus.value = DatabaseService().getLabelFromDatabase(context)
        }
    }
}