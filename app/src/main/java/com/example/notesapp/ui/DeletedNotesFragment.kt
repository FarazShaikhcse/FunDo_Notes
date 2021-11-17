package com.example.notesapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.utils.*
import com.example.notesapp.viewmodels.DeletedNoteViewModel
import com.example.notesapp.viewmodels.DeletedNoteViewModelFactory
import android.content.DialogInterface
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.NoteEntity


class DeletedNotesFragment : Fragment() {
    private lateinit var deletedNoteViewModel: DeletedNoteViewModel
    var noteList = mutableListOf<NoteEntity>()
    lateinit var gridrecyclerView: RecyclerView
    lateinit var adapter: NotesViewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_deleted_notes, container, false)
        // Inflate the layout for this fragment
        deletedNoteViewModel =
            ViewModelProvider(
                requireActivity(),
                DeletedNoteViewModelFactory()
            )[DeletedNoteViewModel::class.java]
        Util.loadToolBar(requireActivity(), "deletefragment")
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
        deletedNoteViewModel.readNotesFromDatabase(true, requireContext())
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        deletedNoteViewModel.readNotesFromDatabaseStatus.observe(viewLifecycleOwner) {
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
        deletedNoteViewModel.restoreNotesStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Restored successfully", Toast.LENGTH_SHORT).show()
                deletedNoteViewModel.readNotesFromDatabase(true, requireContext())
            }
        }
        deletedNoteViewModel.permNotesDeleteStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                deletedNoteViewModel.readNotesFromDatabase(true, requireContext())
            }
            else{
                Toast.makeText(requireContext(), "Internet connection required"
                    , Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun alertDialog(position: Int) {
        var alertDialog = AlertDialog.Builder(requireContext()).create()

        alertDialog.setTitle(noteList[position].title)

        alertDialog.setMessage(noteList[position].content)

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Restore",
            DialogInterface.OnClickListener { dialog, id ->
                deletedNoteViewModel.restoreDeletedNotes(noteList[position], requireContext())
            })

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete",
            DialogInterface.OnClickListener { dialog, id ->
                deletedNoteViewModel.permDeleteNotes(noteList[position], requireContext())
            })

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
            DialogInterface.OnClickListener { dialog, id ->

            })

        alertDialog.show()
    }
}


