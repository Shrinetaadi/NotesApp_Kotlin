package com.shrinetaadi.notes

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*


class CreateNoteFragment : BaseFragment(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {
    private var currDate: String? = null
    private var selectedColor = "#171C26"
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 457
    private var webLink: String? = null
    private var selectedImagePath: String? = null
    private var noteId = -1
    private var moreClicked = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getInt("id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateNoteFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        currDate = sdf.format(Date())
        txtDateTime.text = currDate

        if (noteId != -1) {
            launch {
                context?.let {
                    txtAppName.text = getString(R.string.app_name)
                    webLink = null
                    rltxtWebLink.visibility = View.GONE
                    txtWebLink.visibility = View.GONE
                    selectedImagePath = null
                    rlImageNote.visibility = View.GONE
                    imgNote.visibility = View.GONE
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    etNotesTitle.setText(notes.title)
                    etNotesSubTitle.setText(notes.subTitle)
                    etNotesDesc.setText(notes.noteText)
                    txtDateTime.text = notes.dateTime
                    currDate = notes.dateTime.toString()
                    vwSub.setCardBackgroundColor(Color.parseColor(notes.color))
                    if (notes.imgPath != null) {
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        selectedImagePath = notes.imgPath.toString()
                        rlImageNote.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgImageNew.text = "Change Image"
                    } else {
                        selectedImagePath = null
                        imgNote.visibility = View.GONE
                        rlImageNote.visibility = View.GONE
                    }
                    if (notes.webLink != null) {
                        webLink = notes.webLink.toString()
                        txtWebLink.text = notes.webLink
                        rltxtWebLink.visibility = View.VISIBLE
                        txtWebLink.visibility = View.VISIBLE
                        imgLinkNew.text = "Change Link"
                    } else {
                        webLink = null
                        rltxtWebLink.visibility = View.GONE
                        txtWebLink.visibility = View.GONE
                    }

                    colourSetting(notes.color!!)
                    imgDelete.visibility = View.VISIBLE

                }
            }
        }

        imgCloseLink.setOnClickListener {
            webLink = null
            txtWebLink.text = ""
            rltxtWebLink.visibility = View.GONE

        }
        imgCloseNoteImage.setOnClickListener {
            selectedImagePath = null
            imgNote.setImageResource(0)
            rlImageNote.visibility = View.GONE
            imgNote.visibility = View.GONE
        }



        imgDelete.setOnClickListener {

            launch {
                context?.let {
                    NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                    etNotesTitle.setText("")
                    etNotesSubTitle.setText("")
                    etNotesDesc.setText("")
                    imgNote.setImageResource(0)
                    selectedImagePath = null
                    webLink = null
                    imgNote.visibility = View.GONE
                    rlImageNote.visibility = View.GONE
                    llWebLink.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                    Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        imgCheck.setOnClickListener {
            hideKeyboard()
            saveNote()


        }
        clrBlue.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorBlueNote))
        }
        clrBlack.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorBlackNote))
        }
        clrWhite.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorWhiteNote))
        }
        clrYellow.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorYellowNote))
        }
        clrOrange.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorOrangeNote))
        }
        clrGreen.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorGreenNote))
        }
        clrPurple.setOnClickListener {
            colourSetting(resources.getString(0 + R.color.ColorPurpleNote))
        }

        imgImageNew.setOnClickListener {
            readStorageTask()
        }
        imgLinkNew.setOnClickListener {
            etWebLink.setText(webLink)
            etWebLink.isEnabled = true
            llWebLink.visibility = View.VISIBLE
            etWebLink.setSelection(etWebLink.text.toString().length)
            etWebLink.showKeyboard()
            rltxtWebLink.visibility = View.GONE
            txtWebLink.visibility = View.GONE
        }
        btnOk.setOnClickListener {
            if (etWebLink.text.toString().trim().isNotEmpty()) {
                checkUrl()
                imgLinkNew.text = "Change Link"
            } else {
                Toast.makeText(requireContext(), "Url is Required", Toast.LENGTH_SHORT).show()
            }
        }
        btnCancel.setOnClickListener {
            if (noteId != -1) {
                if (webLink != null) {
                    llWebLink.visibility = View.GONE
                    rltxtWebLink.visibility = View.VISIBLE
                    txtWebLink.visibility = View.VISIBLE
                } else {
                    webLink = null
                    llWebLink.visibility = View.GONE
                    txtWebLink.visibility = View.GONE
                    rltxtWebLink.visibility = View.GONE
                }
            } else {
                webLink = null
                llWebLink.visibility = View.GONE
                txtWebLink.visibility = View.GONE
                rltxtWebLink.visibility = View.GONE
            }
        }
        txtWebLink.setOnClickListener {
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(etWebLink.text.toString()))
            startActivity(intent)
        }
        imgMore.setOnClickListener {
            if (moreClicked == 0) {
                var params = llMenus.layoutParams
                params.height = 380
                llMenus.layoutParams = params
                moreClicked = 1
            } else if (moreClicked == 1) {
                var params = llMenus.layoutParams
                params.height = 0
                llMenus.layoutParams = params
                moreClicked = 0
            }
        }

    }

    private fun checkUrl() {
        if (Patterns.WEB_URL.matcher(etWebLink.text.toString()).matches()) {
            llWebLink.visibility = View.GONE
            etWebLink.isEnabled = false
            webLink = etWebLink.text.toString()
            txtWebLink.visibility = View.VISIBLE
            rltxtWebLink.visibility = View.VISIBLE
            txtWebLink.text = webLink

        } else {
            Toast.makeText(requireContext(), "Url is not valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveNote() {
        if (etNotesTitle.text.isNullOrEmpty() && etNotesSubTitle.text.isNullOrEmpty() && etNotesDesc.text.isNullOrEmpty()) {
            etNotesTitle.error = "Note Title is Required"
            etNotesDesc.error = "Note Description is Required"
            etNotesSubTitle.error = "Note Sub Title is Required"
        } else if (etNotesSubTitle.text.isNullOrEmpty() && etNotesDesc.text.isNullOrEmpty()) {
            etNotesDesc.error = "Note Description is Required"
            etNotesSubTitle.error = "Note Sub Title is Required"
        } else if (etNotesTitle.text.isNullOrEmpty() && etNotesSubTitle.text.isNullOrEmpty()) {
            etNotesTitle.error = "Note Title is Required"
            etNotesSubTitle.error = "Note Sub Title is Required"
        } else if (etNotesTitle.text.isNullOrEmpty() && etNotesDesc.text.isNullOrEmpty()) {
            etNotesTitle.error = "Note Title is Required"
            etNotesDesc.error = "Note Description is Required"
        } else if (etNotesTitle.text.isNullOrEmpty()) {
            etNotesTitle.error = "Note Title is Required"
        } else if (etNotesSubTitle.text.isNullOrEmpty()) {
            etNotesSubTitle.error = "Note Sub Title is Required"
        } else if (etNotesDesc.text.isNullOrEmpty()) {
            etNotesDesc.error = "Note Description is Required"
        } else {

            launch {
                if (noteId == -1) {
                    val notes = Notes()
                    notes.title = etNotesTitle.text.toString()
                    notes.subTitle = etNotesSubTitle.text.toString()
                    notes.noteText = etNotesDesc.text.toString()
                    notes.dateTime = currDate
                    notes.color = selectedColor
                    notes.imgPath = selectedImagePath
                    notes.webLink = webLink
                    context?.let {
                        NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                        etNotesTitle.setText("")
                        etNotesSubTitle.setText("")
                        etNotesDesc.setText("")
                        imgNote.setImageResource(0)
                        imgNote.visibility = View.GONE
                        rlImageNote.visibility = View.GONE
                        llWebLink.visibility = View.GONE
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                } else {
                    val notes = Notes()
                    notes.id = noteId
                    notes.title = etNotesTitle.text.toString()
                    notes.subTitle = etNotesSubTitle.text.toString()
                    notes.noteText = etNotesDesc.text.toString()
                    notes.dateTime = currDate
                    notes.color = selectedColor
                    notes.imgPath = selectedImagePath
                    notes.webLink = webLink
                    context?.let {
                        NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                        etNotesTitle.setText("")
                        etNotesSubTitle.setText("")
                        etNotesDesc.setText("")
                        imgNote.setImageResource(0)
                        selectedImagePath = null
                        webLink = null
                        rlImageNote.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        llWebLink.visibility = View.GONE
                        requireActivity().supportFragmentManager.popBackStack()

                    }
                }
            }
            Toast.makeText(context, "Created Successfully", Toast.LENGTH_LONG).show()
        }

    }


    private fun Fragment.hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun hasReadStoragePerm(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }


    private fun readStorageTask() {
        if (hasReadStoragePerm()) {
            pickImageFromGallery()
        } else {
            EasyPermissions.requestPermissions(
                requireActivity(),
                getString(R.string.storage_permission),
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

    }

    private fun pickImageFromGallery() {

        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(contentUri: Uri): String? {
        var filePath: String? = null
        var cursor = requireActivity().contentResolver.query(contentUri, null, null, null, null)
        if (cursor == null) {
            filePath = contentUri.path
        } else {
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    try {
                        val inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        rlImageNote.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgImageNew.text = "Change Image"
                        selectedImagePath = getPathFromUri(selectedImageUri)!!
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    }

                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(), perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    fun colourSetting(colorString: String) {


        when (colorString) {
            resources.getString(0 + R.color.ColorBlueNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorBlueNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorBlueNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorBlueNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(R.drawable.cg_circle_ring)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorBlackNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorBlackNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorBlackNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorBlackNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(R.drawable.cg_circle_ring)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorWhiteNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorWhiteNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorWhiteNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorWhiteNote))
                txtBtnOk.setTextColor(Color.parseColor("#000000"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(R.drawable.cg_circle_ring_white)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorYellowNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorYellowNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorYellowNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorYellowNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(R.drawable.cg_circle_ring)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorOrangeNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorOrangeNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorOrangeNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorOrangeNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(R.drawable.cg_circle_ring)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorGreenNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorGreenNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorGreenNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorGreenNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(R.drawable.cg_circle_ring)
                imgClrPurple.setImageResource(0)
            }
            resources.getString(0 + R.color.ColorPurpleNote) -> {
                selectedColor = resources.getString(0 + R.color.ColorPurpleNote)
                vwSub.setCardBackgroundColor(resources.getColor(R.color.ColorPurpleNote))
                btnOk.setCardBackgroundColor(resources.getColor(R.color.ColorPurpleNote))
                txtBtnOk.setTextColor(Color.parseColor("#ffffff"))
                imgClrBlue.setImageResource(0)
                imgClrBlack.setImageResource(0)
                imgClrWhite.setImageResource(0)
                imgClrYellow.setImageResource(0)
                imgClrOrange.setImageResource(0)
                imgClrGreen.setImageResource(0)
                imgClrPurple.setImageResource(R.drawable.cg_circle_ring)
            }

        }
    }

    fun View.showKeyboard() {
        this.requestFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun View.hideKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }


}