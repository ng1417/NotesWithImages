package com.cs211d.noteswithdetailview

import android.content.Context

class NoteList private constructor(context: Context) {
    var noteList: MutableList<Note> = mutableListOf()

    companion object {
        private var instance: NoteList? = null

        fun getInstance(context: Context): NoteList {
            if(instance==null) {
                instance = NoteList(context)
            }
            return instance!!
        }
    }


    fun getNote(noteId: Int): Note? {
        return noteList.find { it.id == noteId}
    }
}