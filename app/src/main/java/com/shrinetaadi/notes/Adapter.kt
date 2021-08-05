package com.shrinetaadi.notes

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_create_note.view.*
import kotlinx.android.synthetic.main.item_rv.view.*

class Adapter() : RecyclerView.Adapter<Adapter.NotesViewHolder>() {

    var list = ArrayList<Notes>()
    var selectedList = ArrayList<Notes>()
    var listener: OnItemClickedListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rv, parent, false)
        )

    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.txtItemNoteTitle.text = list[position].title?.capitalize()
        holder.itemView.txtItemNoteDesc.text = list[position].noteText
        holder.itemView.txtItemDateTime.text = list[position].dateTime
        if (list[position].color != null) {
            if (list[position].color == "#ffffffff") {
                holder.itemView.txtItemNoteTitle.setTextColor(Color.parseColor("#000000"))
                holder.itemView.txtItemDateTime.setTextColor(Color.parseColor("#000000"))
                holder.itemView.txtItemNoteDesc.setTextColor(Color.parseColor("#000000"))
                holder.itemView.crdViewHome.setCardBackgroundColor(Color.parseColor(list[position].color))
            } else {
                holder.itemView.crdViewHome.setCardBackgroundColor(Color.parseColor(list[position].color))
            }
        } else {
            holder.itemView.crdViewHome.setCardBackgroundColor(Color.parseColor("#171C26"))
        }

        if (list[position].imgPath != null) {
            holder.itemView.imgItemNote.setImageBitmap(BitmapFactory.decodeFile(list[position].imgPath))
            holder.itemView.imgItemNote.visibility = View.VISIBLE
        } else {
            holder.itemView.imgItemNote.visibility = View.GONE
        }
        if (list[position].webLink != null) {
            holder.itemView.txtItemWebLink.text = list[position].webLink
            holder.itemView.txtItemWebLink.visibility = View.VISIBLE
        } else {
            holder.itemView.txtItemWebLink.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (selectedList.isEmpty()) {
                listener!!.onClicked(list[position].id!!)
            } else {
                if (list[position].selected) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_search)
                    list[position].selected = false
                    selectedList.remove(list[position])
                } else if (!(list[position].selected)) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_selected)
                    list[position].selected = true
                    selectedList.add(list[position])
                }
                listener!!.onLongClick(selectedList)
            }

        }
        holder.itemView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                if (selectedList.isEmpty()) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_selected)
                    list[position].selected = true
                    selectedList.add(list[position])
                }
                listener!!.onLongClick(selectedList)
                return true
            }

        })

    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun saveData(notesList: List<Notes>) {
        list = notesList as ArrayList<Notes>

    }

    fun setOnItemClick(listener1: OnItemClickedListener) {
        listener = listener1
    }

    class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    interface OnItemClickedListener {
        fun onClicked(noteId: Int)
        fun onLongClick(list: ArrayList<Notes>)
    }

}