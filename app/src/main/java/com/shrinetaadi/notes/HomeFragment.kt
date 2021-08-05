package com.shrinetaadi.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment() {

    var notesAdapter: Adapter = Adapter()
    var listNotes = ArrayList<Notes>()
    var selectedList = ArrayList<Notes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imgDeleteSelected.visibility = View.GONE
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        launch {
            context?.let {
                val notes = NotesDatabase.getDatabase(it).noteDao().allNotes()
                notesAdapter.saveData(notes)
                listNotes = notes as ArrayList<Notes>
                recyclerView.adapter = notesAdapter

            }
        }
        notesAdapter!!.setOnItemClick(onClicked)

        fabHome.setOnClickListener {
            val fragment: Fragment = CreateNoteFragment.newInstance()
            var arr = Bundle()
            arr.putInt("id", -1)
            fragment.arguments = arr

            replaceFragment(fragment, true)
        }
        imgDeleteSelected.setOnClickListener {
            for (arr in selectedList) {

                launch {
                    context?.let {
                        NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(arr.id!!)
                        val notes = NotesDatabase.getDatabase(it).noteDao().allNotes()
                        notesAdapter.saveData(notes)
                        recyclerView.adapter = notesAdapter
                    }

                }
            }
            notesAdapter.notifyDataSetChanged()
            imgDeleteSelected.setVisibility(false)
            Toast.makeText(requireContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show()
        }




        mainSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempArr = ArrayList<Notes>()

                for (arr in listNotes) {
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(newText.toString())) {
                        tempArr.add(arr)
                    }
                }

                notesAdapter.saveData(tempArr)
                notesAdapter.notifyDataSetChanged()

                return true
            }

        })

    }

    private val onClicked = object : Adapter.OnItemClickedListener {
        override fun onClicked(noteId: Int) {

            val fragment: Fragment = CreateNoteFragment.newInstance()
            var arr = Bundle()
            arr.putInt("id", noteId)
            fragment.arguments = arr

            replaceFragment(fragment, true)
        }

        override fun onLongClick(list: ArrayList<Notes>) {
            selectedList = list
            if (selectedList.isNotEmpty()) {
                imgDeleteSelected.setVisibility(true)
            } else {
                imgDeleteSelected.setVisibility(false)
            }
        }

    }

    private fun replaceFragment(fragment: Fragment, isTransition: Boolean) {
        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (isTransition) {
            fragmentTransition.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransition.replace(R.id.frameLayout, fragment)
            .addToBackStack(fragment.javaClass.simpleName).commit()
    }

    fun View.setVisibility(boolean: Boolean) {
        if (boolean) {
            this.visibility = View.VISIBLE
        } else {
            this.visibility = View.GONE
        }

    }
}