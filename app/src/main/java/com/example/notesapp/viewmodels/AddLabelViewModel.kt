package com.example.notesapp.viewmodels

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.service.roomdb.LabelEntity
import kotlinx.coroutines.launch

class AddLabelViewModel : ViewModel() {
    private val _labelAddedToDbStatus = MutableLiveData<Boolean>()
    var labelAddedToDbStatus = _labelAddedToDbStatus as LiveData<Boolean>
    private val _labelDeletedFromDbStatus = MutableLiveData<Boolean>()
    var labelDeletedFromDbStatus = _labelDeletedFromDbStatus as LiveData<Boolean>
    private val _labelEditedinDbStatus = MutableLiveData<Boolean>()
    var labelEditedinDbStatus = _labelEditedinDbStatus as LiveData<Boolean>
    private val _labelNoteRelationStatus = MutableLiveData<Boolean>()
    var labelNoteRelationStatus = _labelNoteRelationStatus as LiveData<Boolean>
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

    fun deleteLabelFromDB(label: String, context: Context) {
        viewModelScope.launch {
            _labelDeletedFromDbStatus.value = DatabaseService().deleteLabelFromDB(label, context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun editLabelinDB(label: String, newLabel: String, context: Context) {
        viewModelScope.launch {
            _labelEditedinDbStatus.value = DatabaseService().editLabelinDB(label, newLabel, context)
        }
    }

    fun checkNoteLabelRelation(label: String, noteid: String): Boolean{
        var status = false
        viewModelScope.launch {
            status = DatabaseService().checkNoteLabelRelation(label, noteid)
        }
        return status
    }

    fun deleteLabelRelationsFromDB(label: String, context: Context) {
        viewModelScope.launch {
            DatabaseService().deleteLabelRelationsFromDB(label, context)
        }
    }
}