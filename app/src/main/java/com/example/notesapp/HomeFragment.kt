package com.example.notesapp

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.Service.AuthenticationService
import com.example.notesapp.Utils.*
import com.example.notesapp.viewmodels.HomeViewModel
import com.example.notesapp.viewmodels.HomeViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import java.util.*


class HomeFragment : Fragment() {

    lateinit var dialog: Dialog
    lateinit var userIcon: ShapeableImageView
    lateinit var layout: ImageView
    lateinit var deleteBtn: ImageView
    lateinit var searchview: androidx.appcompat.widget.SearchView
    lateinit var addNotesButton: FloatingActionButton
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var homeViewModel: HomeViewModel
    lateinit var getImage: ActivityResultLauncher<String>
    lateinit var adapter: TodoAdapter
    lateinit var linearAdpater: TodoAdpaterLinear
    lateinit var gridrecyclerView: RecyclerView
    var noteList = mutableListOf<Note>()
    var tempList = mutableListOf<Note>()
    var email: String? = null
    var fullName: String? = null


    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        requireActivity().findViewById<NavigationView>(R.id.myNavMenu).getMenu().getItem(0).setChecked(true);
        var profilePhoto: Uri? = null
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        homeViewModel =
            ViewModelProvider(requireActivity(), HomeViewModelFactory())[HomeViewModel::class.java]
        dialog = Util.createDialog(requireContext())
        userIcon = requireActivity().findViewById(R.id.userProfile)
        layout = requireActivity().findViewById(R.id.notesLayout)
        deleteBtn = requireActivity().findViewById(R.id.deleteButton)
        searchview = requireActivity().findViewById(R.id.searchView)
        addNotesButton = view.findViewById(R.id.addNotesButton)
        adapter = TodoAdapter(tempList)
        linearAdpater = TodoAdpaterLinear(tempList)
        adapter.setOnItemClickListner(object :TodoAdapter.onItemClickListner{
            override fun onItemClick(position: Int) {

                setValuesForUpdation(position)
                Toast.makeText(requireContext(),"You clicked item ${position+1}",Toast.LENGTH_SHORT).show()
                sharedViewModel.setGoToAddNotesPageStatus(true)
            }

        })
        linearAdpater.setOnItemClickListner(object :TodoAdpaterLinear.onItemClickListner{
            override fun onItemClick(position: Int) {
                setValuesForUpdation(position)
                Toast.makeText(requireContext(),"You clicked item ${position+1}",Toast.LENGTH_SHORT).show()
                sharedViewModel.setGoToAddNotesPageStatus(true)
            }

        })
        gridrecyclerView = view.findViewById(R.id.rvNotes)
        gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                dialog.findViewById<ImageView>(R.id.dialogProfile).setImageURI(it)
                val uid = AuthenticationService.checkUser()
                homeViewModel.uploadProfile(uid, it)
            }
        )

        Util.loadToolBar(requireActivity(), "homefragment")
        observe()
        getUserDetails()
        getUserNotes()
        Util.checkLayout(gridrecyclerView, adapter, layout)
        loadAvatar(userIcon)
        homeViewModel.fetchProfile()
        listeners()
        searchNotes()
        Log.d("homefragment","userid"+SharedPref.get("fuid"))
        return view
    }
    private fun getCheckedItem(navigationView: NavigationView): Int {
        val menu = navigationView.menu
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.isChecked) {
                return i
            }
        }
        return -1
    }

    private fun searchNotes() {
        searchview.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                requireActivity().findViewById<TextView>(R.id.FunDo).isVisible = false
                tempList.clear()

                val searchTxt=newText!!.toLowerCase(Locale.getDefault())
                if(searchTxt.isNotEmpty()){
                    noteList.forEach {
                        if(it.title.toLowerCase(Locale.getDefault()).contains(searchTxt)){
                            tempList.add(it)
                        }
                    }
                    gridrecyclerView.adapter!!.notifyDataSetChanged()
                }
                else{
                    tempList.clear()
                    tempList.addAll(noteList)
                    gridrecyclerView.adapter!!.notifyDataSetChanged()

                }

                return false
            }

        })
    }

    private fun setValuesForUpdation(position: Int) {
        SharedPref.setUpdateStatus("updateStatus",true)
        SharedPref.updateNotePosition("position",position+1)
        SharedPref.addString("title",noteList[position].title)
        SharedPref.addString("note",noteList[position].note)
        SharedPref.addString("noteid",noteList[position].time)
    }

    private fun getUserDetails() {
        AuthenticationService.checkUser()?.let { homeViewModel.readUserFromDatabase(it) }
    }

    private fun getUserNotes() {
        homeViewModel.readNotesFromDatabase(false)
    }

    private fun checkLayout() {
        var count = SharedPref.get("counter")
        if (count == "") {
            gridrecyclerView.adapter = adapter
            gridrecyclerView.isVisible = true

        } else if (count == "true") {
            layout.setImageResource(R.drawable.ic_baseline_grid_on_24)
            gridrecyclerView.isVisible = false

        } else if (count == "false") {
            layout.setImageResource(R.drawable.ic_baseline_dehaze_24)
            gridrecyclerView.adapter = adapter
            gridrecyclerView.isVisible = true
        }
    }

    private fun listeners() {
        userIcon.setOnClickListener {
            dialog.show()
        }
        dialog.findViewById<ImageView>(R.id.editProfile).setOnClickListener {
            getImage.launch("image/*")
        }

        userIcon.setOnClickListener {
            dialog.show()
        }

        dialog.findViewById<Button>(R.id.dailogueLogout).setOnClickListener {
            userIcon.setImageResource(R.drawable.avatar)
            dialog.findViewById<ImageView>(R.id.dialogProfile).setImageResource(R.drawable.avatar)
            SharedPref.clearAll()
            dialog.dismiss()
            sharedViewModel.logout()
            sharedViewModel.setGoToLoginPageStatus(true)
        }
        dialog.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dialog.dismiss()
        }
        addNotesButton.setOnClickListener {
            sharedViewModel.setGoToAddNotesPageStatus(true)
        }
        layout.setOnClickListener {

            loadNotesInLayoutType()
        }
    }

    private fun loadAvatar(userIcon: ImageView?) {
        userIcon?.setImageResource(R.drawable.avatar)
    }


    fun observe() {
        homeViewModel.profilePhotoUploadStatus.observe(viewLifecycleOwner) {
            if (it) {
                homeViewModel.fetchProfile()
            }
        }

        homeViewModel.profilePhotoFetch.observe(viewLifecycleOwner) {
            // profilePhot = it

            if (it != null) {
                SharedPref.addString("uri", it.toString())
                Picasso.get().load(it).into(userIcon)
                Picasso.get().load(it).into(dialog.findViewById<ImageView>(R.id.dialogProfile))
            }
        }

        homeViewModel.databaseReadingStatus.observe(viewLifecycleOwner) {
            email = it.get(1)
            fullName = it.get(0)
            SharedPref.addString("email", email.toString())
            SharedPref.addString("name", fullName.toString())
            dialog.findViewById<TextView>(R.id.emailtv).text = email
            dialog.findViewById<TextView>(R.id.usernametv).text = fullName
        }
        homeViewModel.readNotesFromDatabaseStatus.observe(viewLifecycleOwner) {
            noteList.clear()
            tempList.clear()
            gridrecyclerView.isVisible = false
            for (i in it) {
                noteList.add(i)
            }
            tempList.addAll(noteList)
            SharedPref.addNoteSize("noteSize", noteList.size)

            if (SharedPref.get("counter") == "") {
                gridrecyclerView.adapter = adapter
                adapter.notifyItemInserted(noteList.size - 1)
                gridrecyclerView.isVisible = true
            } else if (SharedPref.get("counter") == "true") {
                gridrecyclerView.isVisible = false
                gridrecyclerView.layoutManager = LinearLayoutManager(requireContext())
                linearAdpater.notifyItemInserted(noteList.size - 1)
                gridrecyclerView.adapter = linearAdpater
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
    }

    private fun loadNotesInLayoutType() {

        var flag: Boolean
        var count = SharedPref.get("counter")
        if (count == "") {
            flag = true
        } else if (count == "true") {
            flag = false
        } else {
            flag = true
        }

        if (flag) {
            layout.setImageResource(R.drawable.ic_baseline_grid_on_24)
            gridrecyclerView.isVisible = false
            gridrecyclerView.layoutManager = LinearLayoutManager(requireContext())
            gridrecyclerView.adapter = linearAdpater
            gridrecyclerView.isVisible = true
            SharedPref.addString("counter", "true")

        } else {
            layout.setImageResource(R.drawable.ic_baseline_dehaze_24)
            gridrecyclerView.isVisible = false
            gridrecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            gridrecyclerView.adapter = adapter
            gridrecyclerView.isVisible = true
            SharedPref.addString("counter", "false")

        }
    }

    override fun onStart() {
        super.onStart()
        view?.findViewById<NavigationView>(R.id.myNavMenu)?.getMenu()?.getItem(0)?.setChecked(true);
    }


}