package com.cs211d.noteswithdetailview

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateFragment : Fragment() {

    private var photoFile: File? = null
    private lateinit var photoUri : Uri
    private lateinit var photoImageView: ImageView
    private lateinit var rootView : View
    private var hasPhoto = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        rootView =  inflater.inflate(R.layout.fragment_create, container, false)

        val titleEditText : EditText = rootView.findViewById(R.id.enter_title_edittext)
        val detailsEditText = rootView.findViewById<EditText>(R.id.enter_details_edittext)

        val includePhotoSwitch = rootView.findViewById<Switch>(R.id.photo_switch)
        val takePhotoButton = rootView.findViewById<Button>(R.id.take_photo_button)
        takePhotoButton.visibility = View.INVISIBLE
        takePhotoButton.setOnClickListener {takeAndSavePhoto() }

        photoImageView = rootView.findViewById(R.id.photo_image_view)
        photoImageView.visibility = View.INVISIBLE

        rootView.findViewById<Button>(R.id.add_note_button).setOnClickListener {
            val title = titleEditText.text.toString()
            val details = detailsEditText.text.toString()

            if(title.isNotEmpty() && details.isNotEmpty()) {
                val bundle = Bundle().apply {
                    putString(getString(R.string.note_title_key), title)
                    putString(getString(R.string.note_details_key), details)

                    // ***** YOUR STEP 2 PART C CODE HERE *****
                    if(hasPhoto) {
                        putParcelable(getString(R.string.photo_uri_key), photoUri)
                    }

                }
                Navigation.findNavController(rootView)
                      .navigate(R.id.action_createFragment_to_list_fragment, bundle)

            }
        }

        includePhotoSwitch.setOnCheckedChangeListener { _, isChecked ->
            // ***** YOUR STEP 2 PART A CODE HERE *****
            takePhotoButton.visibility = if (isChecked) View.VISIBLE else View.INVISIBLE
        }

        return rootView
    }


    private fun takeAndSavePhoto()  {
        // ***** YOUR STEP 2 PART B CODE HERE *****
        // You might have new variables you declare that are used in this method.
        // You should write other functions that are invoked from this method.
        photoFile = createImageFile()

        // Use requireContext() to get the context
        val contextInFragment: Context = requireContext()
        // create a content URI so the camera can access this app-specific file
        val photoUri = FileProvider.getUriForFile(
            contextInFragment,
            "com.cs211d.noteswithdetailview.fileprovider", photoFile!!
        )
        takePictureResultLauncher.launch(photoUri)
    }

    private val takePictureResultLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult( /* contract = */ ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Log.d("ActivityResultLauncher", "TRUE")
                displayImage()
                savePhoto()

            }
        }
    private fun createImageFile(): File {
        // create a unique image filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFilename = "{$timeStamp}_photo.jpg"
        Log.d("TEST", "in createImageFile, ${imageFilename}")
        // get file path where the app can save a private image
        val storageDir = rootView.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.d("image create path", "$storageDir , $imageFilename")
        return File(storageDir, imageFilename)
    }

    private fun displayImage() {

        //BitmapFactory.decodeFile(photoFile!!.absolutePath)
        Log.d("TEST", "in displayImage, ${photoFile}")
        val notePicture = BitmapFactory.decodeFile(photoFile!!.absolutePath)
        Log.d("TEST", "in displayImage, notePicture = , ${notePicture}")
        photoImageView.setImageBitmap(notePicture)
     }

    private fun savePhoto() {
        Log.d("TEST", "in savePhotoClick ${photoFile}")

        if (photoFile != null) {

            // save image in background thread
            CoroutineScope(Dispatchers.Main).launch {

                val imageValues = ContentValues()
                imageValues.put(MediaStore.MediaColumns.DISPLAY_NAME, photoFile!!.name)
                imageValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imageValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val resolver = rootView.context.applicationContext.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageValues)

                // save bitmap as JPEG
                uri?.let {
                    runCatching {
                        resolver.openOutputStream(it).use { outStream ->
                            val photoBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath, null)
                            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream!!)
                        }
                    }
                }
            }
        }
    }
    }

