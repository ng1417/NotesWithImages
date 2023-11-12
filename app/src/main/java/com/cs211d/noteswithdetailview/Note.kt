package com.cs211d.noteswithdetailview

import android.net.Uri

class Note(var id: Int, var title: String, var details: String,
           var photoUri: Uri? = null) {

    val hasPhoto: Boolean
        get() = photoUri != null

    override fun toString(): String {
        return "$id $title $details"
    }
}
