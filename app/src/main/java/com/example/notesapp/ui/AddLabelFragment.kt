package com.example.notesapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.adapter.LabelAdapter
import com.example.notesapp.service.roomdb.LabelEntity
import com.example.notesapp.viewmodels.AddLabelViewModel
import com.example.notesapp.viewmodels.AddLabelViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.google.android.material.imageview.ShapeableImageView


class AddLabelFragment : Fragment() {

    private lateinit var addLabelViewModel: AddLabelViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var userIcon: ShapeableImageView
    private lateinit var layout: ImageView
    private lateinit var searchview: SearchView
    private lateinit var adapter: LabelAdapter
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var labels: MutableList<String?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_label, container, false)
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        addLabelViewModel =
            ViewModelProvider(
                requireActivity(),
                AddLabelViewModelFactory()
            )[AddLabelViewModel::class.java]
        addLabelViewModel.getLabelsFromDatabase(requireContext())
        view.findViewById<ImageView>(R.id.saveLabelBtn).setOnClickListener {
            val labelEditText = view.findViewById<EditText>(R.id.labelName)
            if (labelEditText.text.toString() != "") {
                addLabelViewModel.addLabelToDatabase(
                    LabelEntity(labelname = labelEditText.text.toString()),
                    requireContext()
                )
            }
        }
        var mListView = view.findViewById<RecyclerView>(R.id.labelList)

        addLabelViewModel.getLabelStatus.observe(viewLifecycleOwner) {
            labels = it
            adapter = LabelAdapter(labels, addLabelViewModel, requireContext())

            mListView.layoutManager = LinearLayoutManager(requireContext())
            mListView.adapter = adapter
        }

        addLabelViewModel.labelAddedToDbStatus.observe(viewLifecycleOwner) {
            if (it) {
                view.findViewById<EditText>(R.id.labelName).setText("")
                addLabelViewModel.getLabelsFromDatabase(requireContext())
                adapter.notifyDataSetChanged()
            }
        }

        addLabelViewModel.labelDeletedFromDbStatus.observe(viewLifecycleOwner) {
            if (it) {
                addLabelViewModel.getLabelsFromDatabase(requireContext())
                adapter.notifyDataSetChanged()
            }
        }
        addLabelViewModel.labelEditedinDbStatus.observe(viewLifecycleOwner) {
            if (it) {
                addLabelViewModel.getLabelsFromDatabase(requireContext())
                adapter.notifyDataSetChanged()

            }
        }
        loadToolBar()
        return view
    }

    private fun loadToolBar() {
        toolbar = requireActivity().findViewById(R.id.myToolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            sharedViewModel.setGotoHomePageStatus(true)
        }
        userIcon = requireActivity().findViewById(R.id.userProfile)
        searchview = requireActivity().findViewById(R.id.searchView)
        layout = requireActivity().findViewById(R.id.notesLayout)
        requireActivity().findViewById<TextView>(R.id.deleteLabelTV).isVisible = false
        userIcon.isVisible = false
        layout.isVisible = false
        searchview.isVisible = false

    }
}