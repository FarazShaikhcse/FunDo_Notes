package com.example.notesapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.notesapp.service.RoomDatabase
import com.example.notesapp.service.notification.NotificationHelper
import com.example.notesapp.service.pushnotfication.FbMessagingService
import com.example.notesapp.ui.*
import com.example.notesapp.utils.Constants
import com.example.notesapp.utils.Note
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.ktx.messaging

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        lateinit var roomDBClass: RoomDatabase
    }

    lateinit var flashFragment: SplashFragment
    lateinit var profileIcon: ImageView
    lateinit var navMenu: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FacebookSdk.sdkInitialize(FacebookSdk.getApplicationContext())
        AppEventsLogger.activateApp(application)
        profileIcon = findViewById(R.id.userProfile)
        toolbar = findViewById(R.id.myToolbar)
        drawerLayout = findViewById(R.id.drawerLayout)
        navMenu = findViewById(R.id.myNavMenu)
        setSupportActionBar(toolbar)
        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)

        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
        navMenu.setNavigationItemSelectedListener(this)
        sharedViewModel = ViewModelProvider(
            this@MainActivity,
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        observeNavigation()
        SharedPref.initSharedPref(this)
        flashFragment = SplashFragment()
        val bundle = intent.extras
        if (savedInstanceState == null && bundle == null) {
            gotoSplashScreen()
        }
        roomDBClass = Room.databaseBuilder(applicationContext, RoomDatabase::class.java, "myDB")
            .fallbackToDestructiveMigration().allowMainThreadQueries().build()
        NotificationHelper.createNotificationChannel(this,
            NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            "Reminder Note", "shows reminder notes")
        getFirebaseMessagingToken()
        subscribeToMessaging("news")
        SharedPref.addString("start","true")
        if (bundle != null) {
            if (bundle.getString("Destination") == "userNote") {
                val note = bundle.getSerializable("reminderNote") as Note
                loadReminderNotesData(note)
            }
        }

    }
    private fun loadReminderNotesData(note: Note) {
        SharedPref.setUpdateStatus("updateStatus", true)
        SharedPref.addString(Constants.TITLE, note.title)
        SharedPref.addString(Constants.NOTE, note.note)
        SharedPref.addString(Constants.NOTEID, note.time)
        SharedPref.addLong(Constants.REMINDER, note.reminder)
        gotoAddNotePage()
    }


    private fun observeNavigation() {
        sharedViewModel.gotoHomePageStatus.observe(this@MainActivity, {
            if (it) {
                gotoHomePage()
            }
        })

        sharedViewModel.gotoLoginPageStatus.observe(this@MainActivity, {
            if (it) {
                gotoLoginPage()
            }
        })

        sharedViewModel.gotoRegistrationPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoRegistrationPage()
                }
            })

        sharedViewModel.gotoResetPasswordPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoResetPasswordFragment()
                }
            })

        sharedViewModel.gotoAddNotesPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoAddNotePage()
                }
            })

        sharedViewModel.gotoDeletedNotesPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoDeleteNotesPage()
                }
            })
        sharedViewModel.gotoAddLabelPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoAddLabelPage()
                }
            })
        sharedViewModel.gotoArchivedPageStatus.observe(this@MainActivity,
            {
                if (it) {
                    gotoArchivedFragment()
                }
            })

    }

    private fun gotoAddLabelPage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, AddLabelFragment())
            commit()
        }
    }

    private fun gotoDeleteNotesPage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, DeletedNotesFragment())
            commit()
        }
    }

    private fun gotoAddNotePage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, AddNoteFragment())
            commit()
        }
    }

    private fun gotoSplashScreen() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, SplashFragment())
            commit()
        }
    }

    private fun gotoLoginPage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, LoginFragment())
            commit()
        }

    }

    private fun gotoRegistrationPage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, RegistrationFragment())
            addToBackStack(null)
            commit()
        }
    }

    private fun gotoHomePage() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, HomeFragment())
            commit()
        }

    }


    private fun gotoResetPasswordFragment() {

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, ResetPasswordFragment())
            addToBackStack(null)
            commit()
        }
    }

    private fun gotoArchivedFragment() {

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, ArchivedFragment())
            commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.menuNotes -> {
                SharedPref.addString(Constants.NOTES_TYPE, "MainNotes")
                navMenu.getMenu().getItem(0).setChecked(true)
                sharedViewModel.setGotoHomePageStatus(true)
            }
            R.id.menuAddNotes -> {
                sharedViewModel.setGoToAddNotesPageStatus(true)
            }
            R.id.menuReminder -> {
                SharedPref.addString(Constants.NOTES_TYPE, "Reminder")
                navMenu.getMenu().getItem(1).setChecked(true)
                sharedViewModel.setGotoHomePageStatus(true)
            }
            R.id.menuLabel -> {
                sharedViewModel.setGoToAddLabelPageStatus(true)
            }
            R.id.menuNotes -> {
                Toast.makeText(applicationContext, "Clicked on notes", Toast.LENGTH_LONG).show()
            }
            R.id.menuDeleted -> {
                sharedViewModel.setGoToDeletedNotesPageStatus(true)
            }
            R.id.menuArchived -> {
                SharedPref.addString(Constants.NOTES_TYPE, "Archived")
                navMenu.getMenu().getItem(5).setChecked(true)
                sharedViewModel.setGotoHomePageStatus(true)
            }

        }
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        SharedPref.addString(Constants.NOTES_TYPE, "MainNotes")
    }
    fun getFirebaseMessagingToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("firebasemessaging", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("firebasemessaging", token.toString())
        })

    }
    fun subscribeToMessaging(topic: String) {
        Firebase.messaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "subscribed"
                if (!task.isSuccessful) {
                    msg = "failed to subscribe"
                }
                Log.d("firebeasemessage", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
    }




}


