package com.example.notesapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.service.DatabaseService
import com.example.notesapp.utils.Constants
import com.example.notesapp.utils.SharedPref
import com.example.notesapp.viewmodels.AddLabelViewModel

class LabelCBAdapter(
    var labels: MutableList<String?>, var addLabelViewModel: AddLabelViewModel, var context: Context
) : RecyclerView.Adapter<LabelCBAdapter.LabelViewHolder>() {

    inner class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    val labelList: MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.label_checkbox_layout, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        val labelCB = holder.itemView.findViewById<CheckBox>(R.id.labelCheckBox)
        holder.itemView.apply {
            labelCB.setText(labels[position])
        }
        labelCB.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                labelList.add(labelCB.text.toString())
            } else {
                labelList.remove(labelCB.text.toString())
            }
            Log.d("checkedlabels", labelList.toString())
        }
        if( SharedPref.getUpdateStatus(Constants.UPDATE_STATUS) )
        {
            if (DatabaseService().checkNoteLabelRelation(labelCB.text.toString(), SharedPref.get(Constants.NOTEID).toString())) {
                labelCB.isChecked = true
            }
        }
    }

    override fun getItemCount(): Int {
        return labels.size
    }

    fun getSelectedLabels(): MutableList<String> {
        return labelList
    }
}

