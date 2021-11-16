package com.example.notesapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.notesapp.R
import com.example.notesapp.utils.Util
import com.example.notesapp.viewmodels.ResetPasswordViewModel
import com.example.notesapp.viewmodels.ResetPasswordViewModelFactory
import com.example.notesapp.viewmodels.SharedViewModel
import com.example.notesapp.viewmodels.SharedViewModelFactory


class ResetPasswordFragment : Fragment() {

    lateinit var resetEmail: EditText
    lateinit var resetBtn: Button
    lateinit var sharedViewModel: SharedViewModel
    lateinit var resetPasswordViewModel: ResetPasswordViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_reset_password, container, false)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        sharedViewModel = ViewModelProvider(requireActivity(), SharedViewModelFactory())[SharedViewModel::class.java]
        resetPasswordViewModel= ViewModelProvider(this, ResetPasswordViewModelFactory())[ResetPasswordViewModel::class.java]
        resetEmail=view.findViewById(R.id.resetEmail)
        resetBtn=view.findViewById(R.id.resetPassword)
        resetBtn.setOnClickListener {
            resetPassword()
        }
        return view
    }



    private fun resetPassword() {
        val emailValue=resetEmail.text.toString().trim()
        if(Util.validateEmail(emailValue,resetEmail)){
            resetPasswordViewModel.resetPassword(emailValue)
            resetPasswordViewModel.resetPasswordStatus.observe(viewLifecycleOwner){
                if(it.status){
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                    sharedViewModel.setGoToLoginPageStatus(true)
                }
                else{
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}