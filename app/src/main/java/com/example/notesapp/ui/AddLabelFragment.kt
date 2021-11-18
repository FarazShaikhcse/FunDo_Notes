package com.example.notesapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.LabelEntity
import com.example.notesapp.viewmodels.AddLabelViewModel
import com.example.notesapp.viewmodels.AddLabelViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_add_label.*


class AddLabelFragment : Fragment() {

    private lateinit var addLabelViewModel: AddLabelViewModel
    lateinit var toolbar: Toolbar
    lateinit var userIcon: ShapeableImageView
    lateinit var deleteBtn: ImageView
    lateinit var layout: ImageView
    lateinit var searchview: SearchView
    lateinit var arrayAdapter: ArrayAdapter<*>
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_label, container, false)

        var labels : MutableList<String?>
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        addLabelViewModel =
            ViewModelProvider(requireActivity(), AddLabelViewModelFactory())[AddLabelViewModel::class.java]
        addLabelViewModel.getLabelsFromDatabase(requireContext())
        view.findViewById<ImageView>(R.id.saveLabelBtn).setOnClickListener {
            val labelEditText = view.findViewById<EditText>(R.id.labelName)
            if(labelEditText.text.toString() != ""){
                addLabelViewModel.addLabelToDatabase(LabelEntity(labelname = labelEditText.text.toString()),
                    requireContext())
            }
        }
        var mListView = view.findViewById<ListView>(R.id.userlist)
        addLabelViewModel.getLabelStatus.observe(viewLifecycleOwner){
            labels = it
            arrayAdapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, labels)
            mListView.adapter = arrayAdapter
        }
        addLabelViewModel.labelAddedToDbStatus.observe(viewLifecycleOwner){
            if(it){
                view.findViewById<EditText>(R.id.labelName).setText("")
                addLabelViewModel.getLabelsFromDatabase(requireContext())
                arrayAdapter.notifyDataSetChanged()
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
        requireActivity().findViewById<TextView>(R.id.deleteLabel).isVisible = false
        userIcon.isVisible = false
        layout.isVisible = false
        searchview.isVisible = false

    }
}