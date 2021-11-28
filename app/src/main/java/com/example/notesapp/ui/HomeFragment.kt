package com.example.notesapp.ui

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment

import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.service.roomdb.NoteEntity
import com.example.notesapp.utils.*
import com.example.notesapp.viewmodels.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(), SearchView.OnCloseListener {

    private lateinit var dialog: Dialog
    private lateinit var userIcon: ShapeableImageView
    private lateinit var layout: ImageView
    private lateinit var deleteBtn: ImageView
    private lateinit var searchview: androidx.appcompat.widget.SearchView
    private lateinit var addNotesButton: FloatingActionButton
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var addLabelViewModel: AddLabelViewModel
    private lateinit var getImage: ActivityResultLauncher<String>
    private lateinit var adapter: NotesViewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var mainHandler: Handler
    private lateinit var progressBar: ProgressBar
    var noteList = mutableListOf<NoteEntity>()
    var tempList = mutableListOf<NoteEntity>()
    var email: String? = null
    var fullName: String? = null
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var gridLayoutManager: GridLayoutManager
    var startTime = ""
    var isLoading = false
    var currentItem: Int = 0
    var totalItem: Int = 0
    var scrolledOutItems: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        var profilePhoto: Uri? = null
        mainHandler = Handler(Looper.getMainLooper())
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        addLabelViewModel =
            ViewModelProvider(
                requireActivity(),
                AddLabelViewModelFactory()
            )[AddLabelViewModel::class.java]
        homeViewModel =
            ViewModelProvider(requireActivity(), HomeViewModelFactory())[HomeViewModel::class.java]
        dialog = Util.createDialog(requireContext())
        userIcon = requireActivity().findViewById(R.id.userProfile)
        layout = requireActivity().findViewById(R.id.notesLayout)
        deleteBtn = requireActivity().findViewById(R.id.deleteButton)
        searchview = requireActivity().findViewById(R.id.searchView)
        addNotesButton = view.findViewById(R.id.addNotesButton)
        progressBar = view.findViewById(R.id.rvProgressBar)
        adapter = NotesViewAdapter(tempList)
        linearLayoutManager = LinearLayoutManager(requireContext())
        gridLayoutManager =  GridLayoutManager(requireContext(), 2)
        adapter.setOnItemClickListner(object : NotesViewAdapter.onItemClickListner {
            override fun onItemClick(position: Int) {

                setValuesForUpdation(position)
                Toast.makeText(
                    requireContext(),
                    "You clicked item ${position + 1}",
                    Toast.LENGTH_SHORT
                ).show()
                sharedViewModel.setGoToAddNotesPageStatus(true)
            }

        })
        recyclerView = view.findViewById(R.id.rvNotes)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                dialog.findViewById<ImageView>(R.id.dialogProfile).setImageURI(it)
                val uid = AuthenticationService.checkUser()
                homeViewModel.uploadProfile(uid, it)
            }
        )
        searchview.setOnCloseListener(this)
        Util.loadToolBar(requireActivity(), "homefragment")
        observe()
        getUserDetails()
//        getUserNotes()
        getNotes()
        Util.checkLayout(recyclerView, adapter, layout)
        loadAvatar(userIcon)
        homeViewModel.fetchProfile()
        listeners()
        searchNotes()
        Log.d("homefragment", "userid" + SharedPref.get("fuid"))
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("paginationdbserv", "scroll notes called")
                if (SharedPref.get("counter") == "" || SharedPref.get("counter") == "false") {
                    currentItem = (recyclerView.layoutManager as GridLayoutManager).childCount
                    totalItem = (recyclerView.layoutManager as GridLayoutManager).itemCount
                    scrolledOutItems = (recyclerView.layoutManager as GridLayoutManager)
                        .findFirstVisibleItemPosition()
                    if (!isLoading) {
                        if ((currentItem + scrolledOutItems) >= totalItem && scrolledOutItems >= 0) {
                            isLoading = true
                            progressBar.visibility = View.VISIBLE
                            getNotes()
                        }
                    }
                }
                else {
                    currentItem = (recyclerView.layoutManager as LinearLayoutManager).childCount
                    totalItem = (recyclerView.layoutManager as LinearLayoutManager).itemCount
                    scrolledOutItems = (recyclerView.layoutManager as LinearLayoutManager)
                        .findFirstVisibleItemPosition()
                    if (!isLoading) {
                        if ((currentItem + scrolledOutItems) >= totalItem && scrolledOutItems >= 0) {
                            Log.d("SCROLLED", "scrolled linear")
                            isLoading = true
                            progressBar.visibility = View.VISIBLE
                            getNotes()
                        }
                    }
                }
            }
        })
//        DatabaseService().sync(requireContext())
        return view
    }




    private fun searchNotes() {
        searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }

        })
    }

    private fun setValuesForUpdation(position: Int) {
        SharedPref.setUpdateStatus("updateStatus", true)
        SharedPref.updateNotePosition("position", position + 1)
        SharedPref.addString("title", noteList[position].title)
        SharedPref.addString("note", noteList[position].content)
        SharedPref.addString("noteid", noteList[position].noteid)
    }

    private fun getUserDetails() {
        AuthenticationService.checkUser()
            ?.let { homeViewModel.readUserFromDatabase(it, requireContext()) }
    }

    private fun getUserNotes() {

        homeViewModel.readNotesFromDatabase(requireContext())

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
        searchview.setOnSearchClickListener {
            userIcon.isVisible = false
            layout.isVisible = false
            requireActivity().findViewById<TextView>(R.id.FunDo).isVisible = false
            searchview.maxWidth = Integer.MAX_VALUE

        }


    }
    fun getNotes() {
        Log.d("paginationdbserv", "get notes called")
        homeViewModel.readNotesFromDatabaseWithPagination(startTime, requireContext())
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

            isLoading = false
            Log.d("Limited notes", it.size.toString())
            if (it.size == 0) {
                progressBar.visibility = View.GONE
                //isLoading = false
            }
            else if (it.size > 1) {
                startTime = it[it.size-1].modifiedTime
                for (i in 0 .. it.size - 1 ) {
                        noteList.add(it[i])
                        tempList.add(it[i])
                        Log.d("Limited", startTime)
                        adapter.notifyItemInserted(tempList.size - 1)
                        progressBar.visibility = View.GONE

                }
            }
            Log.d("reading notes", "Size of note  list is" + noteList.size)

        }
        addLabelViewModel.getLabelStatus.observe(viewLifecycleOwner) {


            val navigationView = requireActivity().findViewById<NavigationView>(R.id.myNavMenu)
            val menu: Menu = navigationView.getMenu()
            for (i in it) {
                if ((SharedPref.get(i!!).toString() == "") or (SharedPref.get("start")
                        .toString() == "true")
                ) {
                    val labelmenu = menu.add(i)
                    labelmenu.setIcon(resources.getDrawable(R.drawable.ic_baseline_label_important_24))
                    labelmenu.setOnMenuItemClickListener {
                        Log.d("menuclicked", "clicked" + i)
                        return@setOnMenuItemClickListener false
                    }
                    SharedPref.addString(i.toString(), "updated")
                }
            }
            SharedPref.addString("start", "false")
        }

    }

    private fun loadLabelNotes(i: String) {
        homeViewModel.readNotesFromDatabase(requireContext())
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
            recyclerView.isVisible = false
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = adapter
            recyclerView.isVisible = true
            SharedPref.addString("counter", "true")

        } else {
            layout.setImageResource(R.drawable.ic_baseline_dehaze_24)
            recyclerView.isVisible = false
            recyclerView.layoutManager = gridLayoutManager
            recyclerView.adapter = adapter
            recyclerView.isVisible = true
            SharedPref.addString("counter", "false")

        }
    }

    override fun onClose(): Boolean {
        userIcon.isVisible = true
        layout.isVisible = true
        requireActivity().findViewById<TextView>(R.id.FunDo).isVisible = true
        return false
    }

    private val syncNotes = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            DatabaseService().sync(requireContext())
            mainHandler.postDelayed(this, 120000)
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(syncNotes)
    }

    override fun onResume() {
        super.onResume()
        addLabelViewModel.getLabelsFromDatabase(requireContext())
        mainHandler.post(syncNotes)

    }

}