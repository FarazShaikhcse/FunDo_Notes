package com.example.notesapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.R
import com.example.notesapp.service.AuthenticationService
import com.example.notesapp.service.FireBaseDatabase.Companion.addFbDataToDB
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.utils.Util
import com.example.notesapp.viewmodels.LoginViewModel
import com.example.notesapp.viewmodels.LoginViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.FirebaseApp
import org.json.JSONObject
import java.util.*


class LoginFragment : Fragment(), View.OnClickListener {
    lateinit var register: TextView
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: Button
    lateinit var forgotPassword: TextView
    lateinit var callbackManager: CallbackManager
    lateinit var loginButton: LoginButton
    lateinit var sharedViewModel: SharedViewModel
    lateinit var loginViewModel: LoginViewModel

    var sharePref = activity?.getSharedPreferences("Mypref", Context.MODE_PRIVATE)
    var editor = sharePref?.edit()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        getValues(view)
        sharedViewModel = ViewModelProvider(
            requireActivity(),
            SharedViewModelFactory()
        )[SharedViewModel::class.java]
        loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory()
        )[LoginViewModel::class.java]
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        FirebaseApp.initializeApp(requireContext())
        callbackManager = CallbackManager.Factory.create();
        val EMAIL = "email"
        loginButton = view.findViewById(R.id.login_button) as LoginButton
        loginButton.setReadPermissions(Arrays.asList(EMAIL))
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(result: LoginResult?) {
                //handleFacebookAccessToken(result!!.accessToken);
                val request = GraphRequest.newMeRequest(
                    result?.accessToken,
                    object : GraphRequest.GraphJSONObjectCallback {
                        override fun onCompleted(
                            jsonObject: JSONObject?,
                            response: GraphResponse?
                        ) {
                            Log.v("Main", response.toString())
                            addFbDataToDB(jsonObject)
                        }
                    })
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,gender, birthday")
                request.parameters = parameters
                request.executeAsync()
                SharedPref.addString("fuid", AuthenticationService.checkUser().toString())
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }


        })
        loginViewModel.loginStatus.observe(viewLifecycleOwner, {
            if (it.status) {
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
                SharedPref.addString("fuid", AuthenticationService.checkUser().toString())
                sharedViewModel.setGotoHomePageStatus(true)
            } else {
                Toast.makeText(
                    requireContext(),
                    it.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        return view
    }

    private fun getValues(view: View) {
        register = view.findViewById(R.id.loginRegister)
        email = view.findViewById(R.id.loginEmail)
        password = view.findViewById(R.id.loginPassword)
        login = view.findViewById(R.id.login)
        forgotPassword = view.findViewById(R.id.forgotPassword)
        login.setOnClickListener(this)
        register.setOnClickListener(this)
        forgotPassword.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.loginRegister -> {
                sharedViewModel.setGoToRegisterPageStatus(true)
            }
            R.id.login -> {
                loginUser()
            }
            R.id.forgotPassword -> {
                sharedViewModel.setGoToResetPasswordPageStatus(true)
            }
        }

    }

    private fun loginUser() {
        var homeFragment = HomeFragment()
        val emailValue = email.text.toString().trim()
        val passwordValue = password.text.toString().trim()
        val validEmail = Util.validateEmail(emailValue, email)
        val validPassword = Util.validatePassword(passwordValue, password)

        if (validEmail && validPassword) {
            loginViewModel.loginWithEmailandPassword(emailValue, passwordValue)

        }
    }

    fun gotoProfile(emailValue: String, uid: String?) {
        sharedViewModel.setGotoHomePageStatus(true)
    }


}

