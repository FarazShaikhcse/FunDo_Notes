package com.example.notesapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.Utils.*
import com.example.notesapp.viewmodels.DeletedNoteViewModel
import com.example.notesapp.viewmodels.DeletedNoteViewModelFactory
import android.content.DialogInterface
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi


class DeletedNotesFragment : Fragment() {
    private lateinit var deletedNoteViewModel: DeletedNoteViewModel
    var noteList = mutableListOf<Note>()
    lateinit var gridrecyclerView: RecyclerView
    lateinit var adapter: TodoAdapter
    lateinit var linearAdapter: TodoAdpaterLinear

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        adapter = TodoAdapter(noteList)
        linearAdapter = TodoAdpaterLinear(noteList)
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        Util.checkLayout(
            gridrecyclerView,
            adapter,
            requireActivity().findViewById(R.id.notesLayout)
        )
        adapter.setOnItemClickListner(object : TodoAdapter.onItemClickListner {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onItemClick(position: Int) {
                SharedPref.updateNotePosition("position",position+1)
                SharedPref.addString("noteid",noteList[position].time)
                alertDialog(position)
            }

        })
        linearAdapter.setOnItemClickListner(object : TodoAdpaterLinear.onItemClickListner {
            override fun onItemClick(position: Int) {
                SharedPref.updateNotePosition("position",position+1)
                SharedPref.addString("noteid",noteList[position].time)
                alertDialog(position)
            }
        })
        Util.loadNotesInLayoutType(
            requireActivity(),
            requireContext(),
            view,
            adapter,
            linearAdapter
        )
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        deletedNoteViewModel.readNotesFromDatabase(true)
        deletedNoteViewModel.readNotesFromDatabaseStatus.observe(viewLifecycleOwner) {
            noteList.clear()
            gridrecyclerView.isVisible = false
            for (i in it) {
                noteList.add(i)
            }
            //tempList.addAll(noteList)
            SharedPref.addNoteSize("noteSize", noteList.size)

            if (SharedPref.get("counter") == "") {
                gridrecyclerView.adapter = adapter
                adapter.notifyItemInserted(noteList.size - 1)
                gridrecyclerView.isVisible = true
            } else if (SharedPref.get("counter") == "true") {
                gridrecyclerView.isVisible = false
                gridrecyclerView.layoutManager = LinearLayoutManager(requireContext())
                linearAdapter.notifyItemInserted(noteList.size - 1)
                gridrecyclerView.adapter = linearAdapter
                gridrecyclerView.isVisible = true
            } else if (SharedPref.get("counter") == "false") {
                gridrecyclerView.isVisible = false
                gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                adapter.notifyItemInserted(noteList.size - 1)
                gridrecyclerView.adapter = adapter
                gridrecyclerView.isVisible = true
            }
            Log.d("reading notes", "Size of note  list is" + noteList.size)

        }
        deletedNoteViewModel.restoreNotesStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Restored successfully", Toast.LENGTH_SHORT).show()
                deletedNoteViewModel.readNotesFromDatabase(true)
            }
        }
        deletedNoteViewModel.permNotesDeleteStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                deletedNoteViewModel.readNotesFromDatabase(true)
            }
        }


        requireActivity().findViewById<ImageView>(R.id.notesLayout).setOnClickListener {
            Util.loadNotesInLayoutType(
                requireActivity(),
                requireContext(),
                view,
                adapter,
                linearAdapter
            )
        }
        return view
    }

    private fun alertDialog(position: Int) {
        var alertDialog = AlertDialog.Builder(requireContext()).create()

        alertDialog.setTitle(noteList[position].title)

        alertDialog.setMessage(noteList[position].note)

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Restore",
            DialogInterface.OnClickListener { dialog, id ->
                deletedNoteViewModel.restoreDeletedNotes(noteList[position])
            })

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete",
            DialogInterface.OnClickListener { dialog, id ->
                deletedNoteViewModel.permDeleteNotes(noteList[position])
            })

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
            DialogInterface.OnClickListener { dialog, id ->

            })

        alertDialog.show()
    }
}


