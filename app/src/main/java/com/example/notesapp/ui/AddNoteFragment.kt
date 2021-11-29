package com.example.notesapp.ui

import android.app.*
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.viewmodels.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import android.content.Context.ALARM_SERVICE

import androidx.core.content.ContextCompat.getSystemService

import android.content.Context

import com.example.notesapp.MainActivity

import com.example.notesapp.service.notification.AlarmReceiver

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemServiceName
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.notesapp.service.notification.NotificationWork
import com.example.notesapp.utils.*
import java.util.concurrent.TimeUnit


class AddNoteFragment : Fragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var addNoteViewModel: AddNoteViewModel
    private lateinit var addLabelViewModel: AddLabelViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var userIcon: ShapeableImageView
    private lateinit var deleteBtn: ImageView
    private lateinit var archivebtn: ImageView
    private lateinit var layout: ImageView
    private lateinit var searchview: SearchView
    private lateinit var notesTitle: EditText
    private lateinit var notesContent: EditText
    private lateinit var saveBtn: FloatingActionButton
    private lateinit var reminderBtn: FloatingActionButton
    private lateinit var adapter: LabelCBAdapter
    private lateinit var  reminderTV : TextView

    var day = 0
    var year = 0
    var month = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedYear = 0
    var savedMonth = 0
    var savedHour = 0
    var savedMinute = 0


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_note, container, false)
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        addNoteViewModel = ViewModelProvider(
            this,
            AddNoteViewModelFactory()
        )[AddNoteViewModel::class.java]
        addLabelViewModel =
            ViewModelProvider(
                requireActivity(),
                AddLabelViewModelFactory()
            )[AddLabelViewModel::class.java]
        loadToolBar()
        deleteBtn = requireActivity().findViewById(R.id.deleteButton)
        notesTitle = view.findViewById(R.id.notesTitle)
        notesContent = view.findViewById(R.id.notesContent)
        saveBtn = view.findViewById(R.id.saveButton)
        reminderBtn = view.findViewById(R.id.reminderButton)
        reminderTV = view.findViewById(R.id.reminderTV)
        reminderTV?.isVisible = false
        addLabelViewModel.getLabelsFromDatabase(requireContext())
        clickListeners()
        observe()
        loadNotesValuesForUpdation()
        displayLabels()
        return view
    }

    private fun clickListeners() {
        saveBtn.setOnClickListener {
            saveNote()
        }
        deleteBtn.setOnClickListener {
            deleteNotes()
        }
        archivebtn.setOnClickListener {
            archiveNotes()
        }
        reminderBtn.setOnClickListener {
            pickDate()
        }
    }

    private fun archiveNotes() {
        val titleText = notesTitle.text.toString()
        val noteText = notesContent.text.toString()
        if (SharedPref.get("NotesType").toString() == "Archived")
            addNoteViewModel.unArchiveNotes(requireContext())
        else
            addNoteViewModel.archiveNotes(titleText, noteText, true, requireContext())
    }

    private fun displayLabels() {

    }

    private fun deleteNotes() {

        addNoteViewModel.deleteNotesFromDatabase(requireContext())
    }

    private fun loadNotesValuesForUpdation() {
        if ((SharedPref.get(Constants.TITLE).toString() != "") or (SharedPref.get(Constants.NOTE)
                .toString() != "")
        ) {
            notesTitle.setText(SharedPref.get(Constants.TITLE).toString())
            notesContent.setText(SharedPref.get(Constants.NOTE).toString())
            deleteBtn.isVisible = true
            archivebtn.isVisible = true

            if (SharedPref.get("NotesType").toString() == "Archived")
                archivebtn.setImageResource(R.drawable.ic_baseline_unarchive_24)
            else
                archivebtn.setImageResource(R.drawable.ic_baseline_archive_24)
            if(SharedPref.getLong("reminder") != 0L){
                reminderTV?.isVisible = true
                reminderTV?.text = Util.getDate(SharedPref.getLong("reminder"))
            }
            else
                reminderTV?.isVisible = false
        }
    }

    private fun loadToolBar() {
        toolbar = requireActivity().findViewById(R.id.myToolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            clearSharedPref()
            sharedViewModel.setGotoHomePageStatus(true)
        }
        userIcon = requireActivity().findViewById(R.id.userProfile)
        searchview = requireActivity().findViewById(R.id.searchView)
        layout = requireActivity().findViewById(R.id.notesLayout)
        requireActivity().findViewById<TextView>(R.id.deleteLabelTV).isVisible = false
        requireActivity().findViewById<TextView>(R.id.FunDo).text = "Add Notes"
        archivebtn = requireActivity().findViewById(R.id.archiveButton)
        archivebtn.isVisible = false
        userIcon.isVisible = false
        layout.isVisible = false
        searchview.isVisible = false

    }

    private fun clearSharedPref() {
        SharedPref.setUpdateStatus("updateStatus", false)
        SharedPref.updateNotePosition("position", 0)
        SharedPref.addString(Constants.TITLE, "")
        SharedPref.addString(Constants.NOTE, "")
        SharedPref.addString("noteid", "")
        SharedPref.addLong(Constants.REMINDER, 0L)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveNote() {
        val titleText = notesTitle.text.toString()
        val noteText = notesContent.text.toString()
        if (SharedPref.getLong(Constants.REMINDER) != 0L && !SharedPref.getUpdateStatus("updateStatus")){
            addReminder(SharedPref.getLong(Constants.REMINDER))
        }
        else if (SharedPref.get(Constants.TITLE).toString() != "" || SharedPref.get(Constants.NOTE).toString() != "") {
            val note = Note(
                titleText,
                noteText,
                SharedPref.get("noteid").toString(),
                LocalDateTime.now().toString(),
                reminder = SharedPref.getLong(Constants.REMINDER)
            )
            addNoteViewModel.updateNotesInDatabase(note, requireContext())
        } else {
            val time = LocalDateTime.now().toString()
            val note = NoteEntity(
                time, SharedPref.get("fuid").toString(), titleText, noteText,
                time
            )
            val labelsList = adapter.getSelectedLabels()
            addNoteViewModel.addNotesToDatabase(note, requireContext())
            addNoteViewModel.linkNotesandLabels(note.noteid, labelsList, requireContext())
        }

    }

    private fun observe() {
        addNoteViewModel.databaseNoteAddedStatus.observe(viewLifecycleOwner) {
            if (it) {
                SharedPref.addString(Constants.NOTES_TYPE, "MainNotes")
                sharedViewModel.setGotoHomePageStatus(true)
            } else {
                Toast.makeText(requireContext(), "Error in storing notes to DB", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        addNoteViewModel.databaseNoteUpdatedStatus.observe(viewLifecycleOwner) {
            if (it) {
                clearSharedPref()
                Toast.makeText(requireContext(), "updated", Toast.LENGTH_SHORT).show()
                SharedPref.addString(Constants.NOTES_TYPE, "MainNotes")
                sharedViewModel.setGotoHomePageStatus(true)

            } else {
                Toast.makeText(requireContext(), "Error in storing notes to DB", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        addNoteViewModel.databaseNoteDeletedStatus.observe(viewLifecycleOwner) {
            if (it) {
                clearSharedPref()
                Toast.makeText(requireContext(), "deleted", Toast.LENGTH_SHORT).show()
                sharedViewModel.setGotoHomePageStatus(true)

            } else {
                Toast.makeText(
                    requireContext(),
                    "Error in deleting notes from DB",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        addLabelViewModel.getLabelStatus.observe(viewLifecycleOwner) {
            val labels = it
            adapter = LabelCBAdapter(labels, addLabelViewModel, requireContext())
            var mListView = view?.findViewById<RecyclerView>(R.id.labelRV)
            mListView?.layoutManager = LinearLayoutManager(requireContext())
            mListView?.adapter = adapter
        }
        addNoteViewModel.databaseNoteArchivedStatus.observe(viewLifecycleOwner) {
            if (it) {
                clearSharedPref()
                Toast.makeText(requireContext(), "Archived Notes", Toast.LENGTH_SHORT).show()
                SharedPref.addString(Constants.NOTES_TYPE, "Archived")
                sharedViewModel.setGotoHomePageStatus(true)

            } else {
                Toast.makeText(
                    requireContext(),
                    "Error in archiving notes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        addNoteViewModel.unarchiveNotesStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Unarchived successfully", Toast.LENGTH_SHORT)
                    .show()
                SharedPref.addString("NotesType", "MainNotes")
                sharedViewModel.setGotoHomePageStatus(true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        clearSharedPref()
    }

    private fun pickDate() {
        getDateTimeCalender()
        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        savedDay = day
        savedMonth = month
        savedYear = year
        getDateTimeCalender()
        TimePickerDialog(requireContext(), this, hour, minute, false).show()
    }

    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        savedHour = hour
        savedMinute = minute

        val cal = Calendar.getInstance()
        cal.set(savedYear, savedMonth, savedDay, savedHour, savedMinute, 0)
        val timeInMilli = cal.timeInMillis


        if (timeInMilli > System.currentTimeMillis()) {
            reminderTV?.isVisible = true
            SharedPref.addLong(Constants.REMINDER, timeInMilli)
            reminderTV?.text = Util.getDate(SharedPref.getLong(Constants.REMINDER))
        }
        else{
            Toast.makeText(requireContext(), "Please select older Time", Toast.LENGTH_LONG)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addReminder(timeInMilli: Long) {
        val context = requireContext()
        val titleText = notesTitle.text.toString()
        val noteText = notesContent.text.toString()
        val time = LocalDateTime.now().toString()
        val notifid = SharedPref.getInt("notificationID")
        SharedPref.addInt("notificationID", notifid + 1)
        val reminder = timeInMilli
        val data = Data.Builder()
        data.putString("noteTitle", titleText)
        data.putString("noteContent", noteText)
        data.putString("noteKey", time)
        data.putBoolean("isDeleted", false)
        data.putBoolean("isArchived", false)
        data.putString("modifiedTime", time)
        data.putLong(Constants.REMINDER, reminder)
        val request = OneTimeWorkRequest.Builder(NotificationWork::class.java)
            .setInputData(data.build())
            .setInitialDelay(reminder - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            time,
            ExistingWorkPolicy.REPLACE,
            request
        )
        val note = NoteEntity(
            time, SharedPref.get("fuid").toString(), titleText, noteText, time,
            reminder = timeInMilli
        )
        if (titleText.isNotEmpty() && noteText.isNotEmpty()) {
            addNoteViewModel.addNotesToDatabase(note, context)
        }
    }

    private fun getDateTimeCalender() {
        val calender = Calendar.getInstance()
        year = calender.get(Calendar.YEAR)
        month = calender.get(Calendar.MONTH)
        day = calender.get(Calendar.DAY_OF_MONTH)
        hour = calender.get(Calendar.HOUR)
        minute = calender.get(Calendar.MINUTE)
    }


}