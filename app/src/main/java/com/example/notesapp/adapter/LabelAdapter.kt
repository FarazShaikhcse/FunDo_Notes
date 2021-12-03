package com.example.notesapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.viewmodels.AddLabelViewModel


class LabelAdapter(
    var labels: MutableList<String?>, var addLabelViewModel: AddLabelViewModel, var context: Context
) : RecyclerView.Adapter<LabelAdapter.LabelViewHolder>() {

    inner class LabelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.label_list_layout, parent, false)
        return LabelViewHolder(view)
    }

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        val delete = holder.itemView.findViewById<ImageView>(R.id.deleteLabel)
        val label = holder.itemView.findViewById<EditText>(R.id.labelEditText)
        val update = holder.itemView.findViewById<ImageView>(R.id.save_label)
        val edit = holder.itemView.findViewById<ImageView>(R.id.editLabelBtn)
        edit.setOnClickListener {
            label.requestFocus()
        }

        holder.itemView.apply {
            label.setText(labels[position])
        }
        label.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                update.visibility = View.VISIBLE
                edit.visibility = View.GONE
            } else {
                update.visibility = View.GONE
                edit.visibility = View.VISIBLE
            }
        }
        delete.setOnClickListener {
            addLabelViewModel.deleteLabelFromDB(labels[position]!!, context)
            addLabelViewModel.deleteLabelRelationsFromDB(labels[position]!!, context)
        }

        update.setOnClickListener {
            val text = label.text.toString()
            addLabelViewModel.editLabelinDB(labels[position]!!, text, context)
        }

    }

    override fun getItemCount(): Int {
        return labels.size
    }

}