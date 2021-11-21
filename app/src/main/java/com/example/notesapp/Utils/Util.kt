package com.example.notesapp.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

object Util {
    fun validateEmail(emailValue: String, email: EditText): Boolean {
        if (emailValue.isEmpty()) {
            email.error = "Email is needed"
            email.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            email.error = "Enter valid email"
            email.requestFocus()
            return false
        }
        return true

    }

    fun validatePassword(passwordValue: String, password: EditText): Boolean {
        if (passwordValue.isEmpty()) {
            password.error = "Enter password"
            password.requestFocus()
            return false
        }
        if (passwordValue.length < 6) {
            password.error = "Password is too small"
            password.requestFocus()
            return false
        }
        return true
    }

    fun validateName(fullNameValue: String, fullName: EditText): Boolean {

        if (fullNameValue.isEmpty()) {
            fullName.error = "Full name is needed"
            fullName.requestFocus()
            return false
        }
        return true
    }

    fun validateAge(ageValue: String, age: EditText): Boolean {
        if (ageValue.isEmpty()) {
            age.error = "Age is required"
            age.requestFocus()
            return false
        }
        return true

    }

    fun createDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog)
        var dialog_profile: ImageView = dialog.findViewById(R.id.dialogProfile)
        val dailog_email: TextView = dialog.findViewById(R.id.emailtv)
        val dailog_username: TextView = dialog.findViewById(R.id.usernametv)
        val sharePrefName = SharedPref.get("name")
        val sharePrefEmail = SharedPref.get("email")
        val sharePrefUriString = SharedPref.get("uri")
        val photoUri = sharePrefUriString?.toUri()

        dailog_username.text = sharePrefName
        dailog_email.text = sharePrefEmail
        if (sharePrefUriString == "") {
            dialog_profile.setImageResource(R.drawable.avatar)
        } else {
            Picasso.get().load(photoUri).into(dialog_profile)
        }


        dialog.window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                context,
                R.drawable.customdialogbg
            )
        )
        dialog.window
            ?.setLayout(800, 750)
        dialog.setCancelable(false) //Optional
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        return dialog
    }

    fun loadAvatar(userIcon: ImageView?) {
        userIcon?.setImageResource(R.drawable.avatar)
    }


    fun loadToolBar(activity: Activity, fragment: String) {
        var deleteBtn: ImageView = activity.findViewById(R.id.deleteButton)
        var userIcon: ShapeableImageView = activity.findViewById(R.id.userProfile)
        var layout: ImageView = activity.findViewById(R.id.notesLayout)
        var searchview: SearchView = activity.findViewById(R.id.searchView)
        var deleteLabel: TextView = activity.findViewById(R.id.deleteLabelTV)
        var fundoLabel: TextView = activity.findViewById(R.id.FunDo)
        activity.findViewById<ImageView>(R.id.archiveButton).isVisible = false

        if (fragment == "homefragment") {
            userIcon.isVisible = true
            searchview.isVisible = true
            deleteLabel.isVisible = false
            layout.isVisible = true
            fundoLabel.isVisible = true
            fundoLabel.text = "Fundoo"
        } else {
            userIcon.isVisible = false
            searchview.isVisible = false
            deleteLabel.isVisible = true
            layout.isVisible = false
            fundoLabel.isVisible = false
            if(fragment == "archivefragment")
                deleteLabel.text = "Archived"
            else
                deleteLabel.text = "Deleted"
        }

        deleteBtn.isVisible = false

        val toggle = ActionBarDrawerToggle(
            activity,
            activity.findViewById(R.id.drawerLayout),
            activity.findViewById(R.id.myToolbar),
            R.string.open,
            R.string.close
        )
        val drawerLayout = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
        drawerLayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

    }


    fun checkLayout(gridrecyclerView: RecyclerView, adapter: NotesViewAdapter, layout: ImageView) {
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    fun getDate(milliSeconds: Long): String? {
        val dateFormat = "dd/MM/yy hh:mm"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

}