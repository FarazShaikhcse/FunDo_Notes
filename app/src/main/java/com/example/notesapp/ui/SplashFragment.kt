package com.example.notesapp.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.R
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory


class SplashFragment : Fragment() {

    lateinit var sharedViewModel: SharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        // Inflate the layout for this fragment
        Handler().postDelayed(Runnable {
            if (SharedPref.get("fuid").toString() != "") {
                sharedViewModel.setGotoHomePageStatus(true)
            } else
                sharedViewModel.setGoToLoginPageStatus(true)
        }, 1500)

        return view
    }


}