package com.example.notesapp.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.service.roomdb.NoteEntity
import java.util.*
import kotlin.collections.ArrayList


class NotesViewAdapter(
    var notes: MutableList<NoteEntity>
) : RecyclerView.Adapter<NotesViewAdapter.TodoViewHolder>(), Filterable {
    inner class TodoViewHolder(itemview: View, listener: onItemClickListner) :
        RecyclerView.ViewHolder(itemview) {
        init {
            itemview.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    private lateinit var mListner: onItemClickListner
    var filteredNotes: ArrayList<NoteEntity> = ArrayList()

    init {
        filteredNotes = notes as ArrayList<NoteEntity>
    }

    interface onItemClickListner {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListner(listener: onItemClickListner) {
        mListner = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_layout, parent, false)
        return TodoViewHolder(view, mListner)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val title = holder.itemView.findViewById<TextView>(R.id.gridTitle)
        val note = holder.itemView.findViewById<TextView>(R.id.gridNote)

        holder.itemView.apply {
            title.text = filteredNotes[position].title
            note.text = filteredNotes[position].content
        }

    }

    override fun getItemCount(): Int {
        return filteredNotes.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredNotes = if (charSearch.isEmpty()) {
                    notes as ArrayList<NoteEntity>
                } else {
                    val resultList = ArrayList<NoteEntity>()
                    for (row in notes) {
                        if ((row.title.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)))
                            or (row.content.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredNotes
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredNotes = results?.values as ArrayList<NoteEntity>
                notifyDataSetChanged()
            }

        }
    }

}