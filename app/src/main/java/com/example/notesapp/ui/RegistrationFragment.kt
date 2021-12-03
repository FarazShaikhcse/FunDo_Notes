package com.example.notesapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.R
import com.example.notesapp.utils.User
import com.example.notesapp.utils.Util
import com.example.notesapp.viewmodels.RegistrationViewModel
import com.example.notesapp.viewmodels.RegistrationViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory


class RegistrationFragment : Fragment(), View.OnClickListener {


    lateinit var register: Button
    lateinit var fullName: EditText
    lateinit var age: EditText
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: TextView
    lateinit var layout: ImageView
    lateinit var registrationViewModel: RegistrationViewModel
    lateinit var sharedViewModel: SharedViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        registrationViewModel = ViewModelProvider(
            requireActivity(),
            RegistrationViewModelFactory()
        )[RegistrationViewModel::class.java]
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        register = view.findViewById(R.id.registerButton)
        login = view.findViewById(R.id.registrationLogin)
        fullName = view.findViewById(R.id.fullName)
        age = view.findViewById(R.id.age)
        email = view.findViewById(R.id.registrationEmail)
        password = view.findViewById(R.id.registrationPass)

        register.setOnClickListener(this)
        login.setOnClickListener(this)

        return view
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.registrationLogin -> {
                sharedViewModel.setGoToLoginPageStatus(true)
            }
            R.id.registerButton -> {
                registerUser(view)
            }
        }
    }

    private fun registerUser(view: View) {
        val fullNameValue = fullName.text.toString().trim()
        val ageValue = age.text.toString().trim()
        val emailValue = email.text.toString().trim()
        val passwordValue = password.text.toString().trim()

        val validName = Util.validateName(fullNameValue, fullName)
        val validAge = Util.validateAge(ageValue, age)

        val validEmail = Util.validateEmail(emailValue, email)
        val validPassword = Util.validatePassword(passwordValue, password)
        if (validName && validAge && validEmail && validPassword) {
            registrationViewModel.registerUser(emailValue, passwordValue, fullNameValue, ageValue)

        }


        registrationViewModel.registrationStatus.observe(viewLifecycleOwner) {
            if (it.status) {
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
                val user = User(fullNameValue, ageValue, emailValue)
                sharedViewModel.addUserToDatabase(user)
                //sharedViewModel.setGotoHomePageStatus(true)
            } else {
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        sharedViewModel.databaseRegistrationStatus.observe(viewLifecycleOwner) {
            if (it) {
                Toast.makeText(
                    requireContext(),
                    "database Registration successful",
                    Toast.LENGTH_LONG
                ).show()
                sharedViewModel.setGotoHomePageStatus(true)
            } else {
                Toast.makeText(
                    requireContext(),
                    "database Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}


