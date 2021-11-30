package com.example.notesapp.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.adapter.NotesViewAdapter
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.Util
import com.example.notesapp.viewmodels.ArchiveViewModel
import com.example.notesapp.viewmodels.ArchiveViewModelFactory


class ArchivedFragment : Fragment() {
    var noteList = mutableListOf<NoteEntity>()
    private lateinit var gridrecyclerView: RecyclerView
    private lateinit var adapter: NotesViewAdapter
    private lateinit var archiveViewModel: ArchiveViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_archived, container, false)
        archiveViewModel =
            ViewModelProvider(
                requireActivity(),
                ArchiveViewModelFactory()
            )[ArchiveViewModel::class.java]
        Util.loadToolBar(requireActivity(), "archivefragment")
        adapter = NotesViewAdapter(noteList)
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        Util.checkLayout(
            gridrecyclerView,
            adapter,
            requireActivity().findViewById(R.id.notesLayout)
        )
        adapter.setOnItemClickListner(object : NotesViewAdapter.onItemClickListner {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onItemClick(position: Int) {
                SharedPref.updateNotePosition("position", position + 1)
                SharedPref.addString("noteid", noteList[position].noteid)
                alertDialog(position)
            }

        })
        archiveViewModel.readNotesFromDatabase(requireContext())
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        archiveViewModel.readNotesFromDatabaseStatus.observe(viewLifecycleOwner) {
            noteList.clear()
            gridrecyclerView.isVisible = false
            for (i in it) {
                noteList.add(i)
            }
            //tempList.addAll(noteList)
            SharedPref.addNoteSize("noteSize", noteList.size)


            gridrecyclerView.adapter = adapter
            adapter.notifyItemInserted(noteList.size - 1)
            gridrecyclerView.isVisible = true


            Log.d("reading notes", "Size of note  list is" + noteList.size)

        }
        archiveViewModel.unarchiveNotesStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Unarchived successfully", Toast.LENGTH_SHORT).show()
                archiveViewModel.readNotesFromDatabase( requireContext())
            }
        }
        archiveViewModel.tempNotesDeleteStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                archiveViewModel.readNotesFromDatabase( requireContext())
            } else {
                Toast.makeText(
                    requireContext(), "Internet connection required", Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun alertDialog(position: Int) {
        var alertDialog = AlertDialog.Builder(requireContext()).create()

        alertDialog.setTitle(noteList[position].title)

        alertDialog.setMessage(noteList[position].content)

        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE, "UnArchive",
            DialogInterface.OnClickListener { dialog, id ->
                archiveViewModel.unArchiveNotes(noteList[position], requireContext())
            })

        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "Delete",
            DialogInterface.OnClickListener { dialog, id ->
                archiveViewModel.tempDeleteNotes(noteList[position], requireContext())
            })

        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "Cancel",
            DialogInterface.OnClickListener { dialog, id ->

            })

        alertDialog.show()
    }

}