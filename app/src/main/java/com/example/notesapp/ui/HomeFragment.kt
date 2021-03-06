package com.example.notesapp.ui

import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.R
import com.example.notesapp.adapter.NotesViewAdapter
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
    lateinit var gridLayoutManager: StaggeredGridLayoutManager
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
        (activity as AppCompatActivity).supportActionBar?.show()

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
        dialog = context?.let { Util.createDialog(it) }!!
        if (activity != null) {
            userIcon = requireActivity().findViewById(R.id.userProfile)
            layout = requireActivity().findViewById(R.id.notesLayout)
            deleteBtn = requireActivity().findViewById(R.id.deleteButton)
            searchview = requireActivity().findViewById(R.id.searchView)
        }
        addNotesButton = view.findViewById(R.id.addNotesButton)
        progressBar = view.findViewById(R.id.rvProgressBar)
        adapter = NotesViewAdapter(tempList)
        linearLayoutManager = LinearLayoutManager(context)
        gridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView = view.findViewById(R.id.rvNotes)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        adapter.setOnItemClickListner(object : NotesViewAdapter.onItemClickListner {
            override fun onItemClick(position: Int) {
                setValuesForUpdation(position)
                sharedViewModel.setGoToAddNotesPageStatus(true)
            }
        })
        searchview.setOnCloseListener(this)
        getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                dialog.findViewById<ImageView>(R.id.dialogProfile).setImageURI(it)
                val uid = AuthenticationService.checkUser()
                homeViewModel.uploadProfile(uid, it)
            }
        )
        activity?.let { Util.loadToolBar(it, "homefragment") }
        observe()
        getUserDetails()
//        getUserNotes()
        context?.let { addLabelViewModel.getLabelsFromDatabase(it) }
        getNotes()
        Util.checkLayout(recyclerView, adapter, layout)
        loadAvatar(userIcon)
        homeViewModel.fetchProfile()
        listeners()
        searchNotes()
        Log.d("homefragment", "userid" + SharedPref.get("fuid"))
        pagination()
//        DatabaseService().sync(requireContext())
        return view
    }

    private fun pagination() {
        if (SharedPref.get(Constants.NOTES_TYPE).toString() == Constants.MAIN_NOTES) {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    Log.d("paginationdbserv", "scroll notes called")
                    if (SharedPref.get("counter") == "" || SharedPref.get("counter") == "false") {
                        currentItem =
                            (recyclerView.layoutManager as StaggeredGridLayoutManager).childCount
                        totalItem =
                            (recyclerView.layoutManager as StaggeredGridLayoutManager).itemCount
                        scrolledOutItems =
                            (recyclerView.layoutManager as StaggeredGridLayoutManager)
                                .findFirstVisibleItemPositions(null)[0];
                        if (!isLoading) {
                            if ((currentItem + scrolledOutItems) >= totalItem && scrolledOutItems >= 0) {
                                isLoading = true
                                progressBar.visibility = View.VISIBLE
                                getNotes()
                            }
                        }
                    } else {
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
        }
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
        SharedPref.setUpdateStatus(Constants.UPDATE_STATUS, true)
        SharedPref.updateNotePosition(Constants.POSITION, position + 1)
        SharedPref.addString(Constants.TITLE, noteList[position].title)
        SharedPref.addString(Constants.NOTE, noteList[position].content)
        SharedPref.addString(Constants.NOTEID, noteList[position].noteid)
        SharedPref.addLong(Constants.REMINDER, noteList[position].reminder)
    }

    private fun getUserDetails() {
        AuthenticationService.checkUser()
            ?.let { context?.let { it1 -> homeViewModel.readUserFromDatabase(it, it1) } }
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
            activity?.findViewById<TextView>(R.id.FunDo)?.isVisible = false
            searchview.maxWidth = Integer.MAX_VALUE
        }
    }

    fun getNotes() {
        Log.d("paginationdbserv", "get notes called")
        context?.let { homeViewModel.readNotesFromDatabaseWithPagination(startTime, it) }
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
            if (SharedPref.get(Constants.NOTES_TYPE).toString() == Constants.MAIN_NOTES) {
                isLoading = false
                Log.d("Limited notes", it.size.toString())
                if (it.size == 0) {
                    progressBar.visibility = View.GONE
                } else if (it.size > 1) {
                    startTime = it[it.size - 1].modifiedTime
                    for (i in 0..it.size - 1) {
                        noteList.add(it[i])
                        tempList.add(it[i])
                        Log.d("Limited", startTime)
                        adapter.notifyItemInserted(tempList.size - 1)
                        progressBar.visibility = View.GONE
                    }
                }
            } else {
                adapter.notifyItemRangeRemoved(0, tempList.size)
                tempList.clear()
                for (i in 0 until it.size) {
                    noteList.add(it[i])
                    tempList.add(it[i])
                    adapter.notifyItemInserted(i)
                    progressBar.visibility = View.GONE
                }
            }
            Log.d("reading notes", "Size of note  list is" + noteList.size)
        }
        addLabelViewModel.getLabelStatus.observe(viewLifecycleOwner) {
            if (activity != null) {
                val navigationView = requireActivity().findViewById<NavigationView>(R.id.myNavMenu)
                val menu: Menu = navigationView.getMenu()
                for (i in it) {
                    if ((SharedPref.get(i!!).toString() == "") or (SharedPref.get("start")
                            .toString() == "true")
                    ) {
                        val menuList = it
                        val labelmenu = menu.add(i)
                        labelmenu.setIcon(resources.getDrawable(R.drawable.ic_baseline_label_important_24))
                        labelmenu.setOnMenuItemClickListener {
                            Log.d("menuclicked", "clicked" + i)
                            SharedPref.addString(Constants.NOTES_TYPE, Constants.LABEL_NOTES)
                            SharedPref.addString("selectedLabel", i)
                            menu.getItem(menuList.indexOf(i) + 6).isChecked = true
                            sharedViewModel.setGotoHomePageStatus(true)
                            return@setOnMenuItemClickListener false
                        }
                        SharedPref.addString(i.toString(), "updated")
                    }
                }
                SharedPref.addString("start", "false")
            }
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
        activity?.findViewById<TextView>(R.id.FunDo)?.isVisible = true
        return false
    }

    private val syncNotes = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            DatabaseService().sync(context)
            mainHandler.postDelayed(this, 120000)
        }
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(syncNotes)
    }
    override fun onResume() {
        super.onResume()
//        addLabelViewModel.getLabelsFromDatabase(requireContext())
        mainHandler.post(syncNotes)
    }
}