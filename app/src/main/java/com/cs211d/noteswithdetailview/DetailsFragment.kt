package com.cs211d.noteswithdetailview

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class DetailsFragment : Fragment() {

    private lateinit var photoImageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        val noteID = arguments?.getInt(getString(R.string.note_id_key))!!
        val note = NoteList.getInstance(requireContext()).getNote(noteID)

        photoImageView = rootView.findViewById(R.id.photo_display_image_view)
        photoImageView.visibility = View.INVISIBLE

        if(note!=null) {
            val titleTextView = rootView.findViewById<TextView>(R.id.note_title_textview)
            val detailsTextView = rootView.findViewById<TextView>(R.id.note_details_textview)

            titleTextView.text = note!!.title
            detailsTextView.text = note!!.details

            // ***** YOUR STEP 4 CODE HERE *****
            if(note.hasPhoto){
                note.photoUri?.let { displayPhoto(it) }
            }
        }

        return rootView
    }

    private fun displayPhoto(photoUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(photoUri)
            if (inputStream != null) {
                val photoBitmap = BitmapFactory.decodeStream(inputStream)
                photoImageView.setImageBitmap(photoBitmap)
                photoImageView.visibility = View.VISIBLE
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}