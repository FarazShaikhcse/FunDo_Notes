package com.example.notesapp.ui

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
import com.example.notesapp.R
import com.example.notesapp.utils.Note
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.viewmodels.AddNoteViewModel
import com.example.notesapp.viewmodels.AddNoteViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.time.LocalDateTime


class AddNoteFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    lateinit var addNoteViewModel: AddNoteViewModel
    lateinit var toolbar: Toolbar
    lateinit var userIcon: ShapeableImageView
    lateinit var deleteBtn: ImageView
    lateinit var layout: ImageView
    lateinit var searchview: SearchView
    lateinit var notesTitle: EditText
    lateinit var notesContent: EditText
    lateinit var saveBtn: FloatingActionButton


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
        loadToolBar()
        deleteBtn = requireActivity().findViewById(R.id.deleteButton)
        notesTitle = view.findViewById(R.id.notesTitle)
        notesContent = view.findViewById(R.id.notesContent)
        saveBtn = view.findViewById(R.id.saveButton)
        saveBtn.setOnClickListener {
            saveNote()
        }
        deleteBtn.setOnClickListener {
            deleteNotes()
        }
        observe()
        loadNotesValuesForUpdation()
        return view
    }

    private fun deleteNotes() {
        val titleText = notesTitle.text.toString()
        val noteText = notesContent.text.toString()

        addNoteViewModel.deleteNotesFromDatabase(titleText, noteText, requireContext())
    }

    private fun loadNotesValuesForUpdation() {
        if (SharedPref.get("title").toString() != "") {
            notesTitle.setText(SharedPref.get("title").toString())
            notesContent.setText(SharedPref.get("note").toString())
            deleteBtn.isVisible = true
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
        requireActivity().findViewById<TextView>(R.id.deleteLabel).isVisible = false
        userIcon.isVisible = false
        layout.isVisible = false
        searchview.isVisible = false

    }

    private fun clearSharedPref() {
        SharedPref.setUpdateStatus("updateStatus", false)
        SharedPref.updateNotePosition("position", 0)
        SharedPref.addString("title", "")
        SharedPref.addString("note", "")
        SharedPref.addString("noteid", "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveNote() {
        val titleText = notesTitle.text.toString()
        val noteText = notesContent.text.toString()

        if (SharedPref.get("title").toString() != "" && SharedPref.get("note").toString() != "") {
            val note = Note(
                titleText,
                noteText,
                SharedPref.get("noteid").toString(),
                LocalDateTime.now().toString()
            )
            addNoteViewModel.updateNotesInDatabase(note, requireContext())
        } else {
            val time = LocalDateTime.now().toString()
            val note = NoteEntity(
                time, SharedPref.get("fuid").toString(), titleText, noteText,
                time, false
            )
            addNoteViewModel.addNotesToDatabase(note, requireContext())
        }

    }

    private fun observe() {
        addNoteViewModel.databaseNoteAddedStatus.observe(viewLifecycleOwner) {
            if (it) {
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
    }

    override fun onStop() {
        super.onStop()
        clearSharedPref()
    }

}