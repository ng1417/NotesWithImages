package com.cs211d.noteswithdetailview

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import java.io.PrintWriter
import java.io.File


class ListFragment : Fragment(), MenuProvider {

    private lateinit var noteList : MutableList<Note>
    private lateinit var recyclerView : RecyclerView

    private lateinit var selectedNote : Note
    private var selectedNotePosition = RecyclerView.NO_POSITION
    private var actionMode : ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)
        // set up the menu
        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        noteList = NoteList.getInstance(requireContext()).noteList
        if(noteList.isEmpty()) {
            readNotesFromFile(); // fills the noteList from the file
        }

        // set up the recycler view
        recyclerView = rootView.findViewById<RecyclerView>(R.id.note_title_list_recyclerview)
        recyclerView.adapter = NoteAdapter(noteList)

        /* when a new note has been created, add it to the list and write it to the file */
        if(arguments!=null) {
            val title = requireArguments().getString(getString(R.string.note_title_key))
            val details = requireArguments().getString(getString(R.string.note_details_key))

            // this method is deprecated, but used because newer ones aren't compatible with older versions of android
            val photoUri: Uri? = requireArguments().getParcelable(getString(R.string.photo_uri_key));  // ***** YOUR STEP 3 PART A CODE HERE *****

            if(title!=null && details!=null) {
                var note = addNoteToList(title!!, details!!, photoUri)
                writeNoteToFile(note)
            }
            requireArguments().clear()
        }

        return rootView
    }



    /*****************CREATING NEW NOTES************************************/
    private fun getNewNoteId  () : Int {
        var max = -1
        for(note in noteList) {
            if(note.id > max) {
                max = note.id
            }
        }
        return max+1
    }

    private fun addNoteToList(title: String, details : String, photoUri : Uri?) : Note{
        var id = getNewNoteId()

        var note : Note
        if(photoUri==null) {
            note = (Note(id, title!!, details!!))
        } else {
            note = (Note(id, title!!, details!!, photoUri!!))
        }
        noteList.add(note)
        recyclerView.scrollToPosition(noteList.size-1)
        return note
    }

    /******************WRITING NOTES TO FILE *********************************/
    private fun writeNoteToFile(note : Note) {
        val outputStream = requireContext().openFileOutput(getString(R.string.notes_file_name), Context.MODE_PRIVATE or Context.MODE_APPEND)
        val writer = PrintWriter(outputStream)

        // line 1- the tital
        writer.println(note.title)

        // ***** YOUR STEP 3 PART B CODE HERE *****
        // line 2- photo info
        if(note.photoUri != null){
            writer.println(note.photoUri)
        } else {
            writer.println(getString(R.string.no_photo_text_indicator))
        }

        // line 3 or more- details
        writer.println(note.details)
        writer.println(getString(R.string.end_details_text_indicator))

        writer.close()
    }


    /*****************READ IN STORED NOTES FROM FILE************************/
    private fun readNotesFromFile() {
        var id = 1
        val file = File(requireContext().filesDir, getString(R.string.notes_file_name))

        if(file.exists()) {
            val inputStream = requireContext().openFileInput(file.name)
            val reader = inputStream.bufferedReader()
            var inputString = reader.readLine()

            /* parse the file */
            while(inputString!=null) {
                // for each note, line1 is the title
                val title = inputString

                // line2 is the photoUri or text indicating the note does not have a photo
                // ***** YOUR STEP 3 PART C CODE HERE *****
                // use these variables:
                var hasPhoto = false
                var photoUriString = ""
                var photoUri : Uri? = null // this should store the Uri if there is one
                val photoTextStore = reader.readLine() // read in "line2"
                // then your code goes here, after the line is read!
                if(photoTextStore != getString(R.string.no_photo_text_indicator)){
                    hasPhoto = true
                    photoUri = Uri.parse(photoTextStore)
                }


                //the remaining lines 3 or more are the details, with a text indicator marking the end
                inputString = reader.readLine()

                val detailsBuilder = StringBuilder()
                val lineSeparator = System.getProperty("line.separator")

                // accounts for multi-line details
                while(!inputString.equals(getString(R.string.end_details_text_indicator))) {
                    detailsBuilder.append(inputString).append(lineSeparator)
                    inputString = reader.readLine()

                }
                var note : Note
                if(hasPhoto) {
                    note = Note(id, title, detailsBuilder.toString(), photoUri!!)
                } else {
                    note = Note(id, title, detailsBuilder.toString())
                }
                id++
                noteList.add(note)
                inputString = reader.readLine()
            }
        }

    }


    /************************** RECYCLER VIEW CODE ************************************************/
    private inner class NoteAdapter(private val noteList:MutableList<Note>) : RecyclerView.Adapter<NoteHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return NoteHolder(layoutInflater, parent)
        }
        override fun onBindViewHolder(holder: NoteHolder, position: Int) {
            val note = noteList[position]
            holder.bind(note)
        }

        override fun getItemCount(): Int {
            return noteList.size
        }

        fun removeNote(note : Note) {
            val index = noteList.indexOf(note)
            if(index>=0) {
                noteList.removeAt(index)
                notifyItemRemoved(index)

                // clears the file
                var outputStream = requireContext().openFileOutput(getString(R.string.notes_file_name), Context.MODE_PRIVATE )
                outputStream.close()

                // rewrites existing note to file
                for(note in noteList) {
                    writeNoteToFile(note)
                }
            }
        }
    }


    private inner class NoteHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(/* itemView = */ inflater.inflate(/* resource = */ R.layout.note_title_list_item, /* root = */
            parent, /* attachToRoot = */
            false        )         ),
        View.OnLongClickListener {

        private var note: Note? = null

        private var noteTitleTextView: TextView = itemView.findViewById(R.id.note_title_item_textview)

        init {
            noteTitleTextView.setOnLongClickListener(this)

            noteTitleTextView.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt(getString(R.string.note_id_key), note?.id ?: -1)
                }
                Navigation.findNavController(itemView).navigate(R.id.action_list_fragment_to_detailsFragment, bundle)
            }
        }

        fun bind(note: Note) {
            this.note = note

            if(note.hasPhoto){
                noteTitleTextView.text = note.title + "\uD83D\uDCF7"
            } else {
                noteTitleTextView.text = note.title
            }

            if (selectedNotePosition == this.bindingAdapterPosition) {
                noteTitleTextView.setBackgroundColor(Color.RED)
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (actionMode != null) {
                return false
            }
            selectedNote = note!!
            selectedNotePosition = absoluteAdapterPosition

            recyclerView.adapter?.notifyItemChanged(selectedNotePosition)

            actionMode = this@ListFragment.requireActivity().startActionMode(actionModeCallback)
            return true
        }
    }
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu) : Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.context_menu, menu)
            return true
        }
        override fun onPrepareActionMode(mode: ActionMode, menu:Menu): Boolean {
            return false
        }
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem) : Boolean {
            if(item.itemId == R.id.delete) {
                (recyclerView.adapter as NoteAdapter).removeNote(selectedNote)
                mode.finish()
                return true
            }
            return false
        }
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null

            recyclerView.adapter?.notifyItemChanged(selectedNotePosition)
            selectedNotePosition = RecyclerView.NO_POSITION
        }
    }
    /***************** MENU *************************************************/
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_menu, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.create_fragment -> {
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_list_fragment_to_createFragment)
                return true
            }
            R.id.clear_list_dialog -> {
                clearDialog()
                return true
            }
            else -> return false
        }

    }
    /***************** CLEAR THE LIST **************************************/
    private fun clearDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getString(R.string.clear_list_dialog_text))
        builder.setPositiveButton(getString(R.string.yes_text)) { dialog, id ->
            noteList.clear()
            val file = File(requireContext().filesDir, getString(R.string.notes_file_name))
            if (file.exists()) {
                val outputStream = requireContext().openFileOutput(file.name, Context.MODE_PRIVATE)
                outputStream.close()
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
        builder.setNegativeButton(getString(R.string.no_text), null)
        builder.setCancelable(true)
        builder.create().show()
    }

}