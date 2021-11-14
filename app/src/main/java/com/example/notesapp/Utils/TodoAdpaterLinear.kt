package com.example.notesapp.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R

class TodoAdpaterLinear(var todos:List<Note>): RecyclerView.Adapter<TodoAdpaterLinear.TodoViewHolderLinear>() {
    inner class TodoViewHolderLinear(itemview:View,listener:onItemClickListner):RecyclerView.ViewHolder(itemview){
        init {
            itemview.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    private lateinit var mListner:onItemClickListner

    interface onItemClickListner{
        fun onItemClick(position:Int)
    }

    fun setOnItemClickListner(listener:onItemClickListner){
        mListner=listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolderLinear {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.linear_layout,parent,false)
        return TodoViewHolderLinear(view,mListner)
    }

    override fun onBindViewHolder(holder: TodoAdpaterLinear.TodoViewHolderLinear, position: Int) {
        val title= holder.itemView.findViewById<TextView>(R.id.linearTitle)
        val note= holder.itemView.findViewById<TextView>(R.id.linearNote)
        holder.itemView.apply {
            title.text=todos[position].title
            note.text=todos[position].note
        }
    }

    override fun getItemCount(): Int {
        return todos.size
    }

}