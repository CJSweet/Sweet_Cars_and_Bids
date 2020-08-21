package com.example.carsandbids.submit_car

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.carsandbids.R
import com.example.carsandbids.databinding.SubmitCarBinding
import com.example.carsandbids.links
import kotlinx.android.synthetic.main.more_links_edit_text.view.*
import kotlinx.android.synthetic.main.submit_car.*
import java.util.*
import kotlin.collections.ArrayList


class SubmitCarFragment : Fragment(), PhotoAdapter.OnDeletePhotoListener {

    //for adding more links
    private lateinit var parentLayout: LinearLayout

    //for camera access
    var camera_uri: Uri? = null

    private lateinit var submitCarViewModel: SubmitCarViewModel

    private lateinit var binding: SubmitCarBinding

    companion object {

        //Image pick code
        private val IMAGE_PICK_CODE = 1000

        //Permission code for gallery photos
        private val PERMISSION_CODE_PHOTO = 1001

        //Permission code for camera access
        private val PERMISSION_CODE_CAMERA = 1002

        //when taking picture
        private val CAMERA_CAPTURE_CODE = 1003

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = SubmitCarBinding.inflate(inflater)

        submitCarViewModel = ViewModelProvider(this).get(SubmitCarViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = submitCarViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        val arrayListYears = arrayListYears()

        //bind title status spinner (code based off of spinner documentation: https://developer.android.com/guide/topics/ui/controls/spinner)
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.title_status_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                //Specify layout to use when list of choices appears
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)
                //apply adapter to spinner
                binding.submitTitleStatusSpinner.adapter = adapter
            }

        //bind US state location spinner
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.states_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationUsSpinner.adapter = adapter
            }

        //bind CAN province location spinner
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.can_prov_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationCanSpinner.adapter = adapter
            }

        //bind year spinner adapter
        ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.spinner_layout_selected,
            arrayListYears
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitYearSpinner.adapter = adapter
            }

        //add more lines for user to enter more links
        // based on code from: https://www.tutorialspoint.com/add-and-remove-views-in-android-dynamically
        parentLayout = binding.submitMoreLinksLayout
        binding.submitMoreListingLinksText.setOnClickListener {
            moreLinks()
            links = links.plus(1)
        }

        //set on click for photo button
        binding.submitSelectPhotosButton.setOnClickListener {
            checkPhotoPermission()
        }

        //set on click for camera button
        binding.submitTakePhotosButton.setOnClickListener {
            checkCameraPermission()
        }

        //on click of clr photos button
        binding.submitClearPhotosButton.setOnClickListener {
            submitCarViewModel.onClrBtnClick()

            val adapter = PhotoAdapter(submitCarViewModel.imgUris, this)

            binding.submitPhotosRecycler.adapter = adapter
        }

        //call more links as many times as the number of viewModel._extraLinks
        // that way through rotation, the number of UI elements are saved
        for (link in 0 until links) {
            moreLinks()
        }


        //add adapter to Recycler the first time
        val adapter = PhotoAdapter(submitCarViewModel.imgUris, this)
        binding.submitPhotosRecycler.adapter = adapter

        //on submit, gather all the information and send to database
        binding.submitSubmitButton.setOnClickListener {
            println(submit_name_edit.text.toString())
        }

        return binding.root
    }

    fun arrayListYears(): ArrayList<Int> {

        val years = ArrayList<Int>()

        for (year in 1980..Calendar.getInstance().get(Calendar.YEAR)) {
            years.add(year)
        }

        return years
    }

    fun moreLinks() {
        val inflater =
            this.requireActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val newEditView: View = inflater!!.inflate(R.layout.more_links_edit_text, null)

        //get proper dimen value from dimen resource xml
        // from: https://stackoverflow.com/questions/11121028/load-dimension-value-from-res-values-dimension-xml-from-source-code
        val editWidth = resources.getDimension(R.dimen.three_hundred)
        ///resources.displayMetrics.density

        //layout params code inspired by: https://stackoverflow.com/questions/47673723/relative-layout-params-in-kotlin
        var param: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(editWidth.toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        param.setMargins(12, 6, 6, 10)
        newEditView.layoutParams = param

        // Add the new row at end of layout
        parentLayout.addView(newEditView, parentLayout.childCount)
    }

    //to get photo, first need permission
    //code for this method and following 2 from:
    // https://stackoverflow.com/questions/55933590/select-photo-on-gallery-kotlin
    fun checkPhotoPermission() {
        //version must be greater than Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //if permission denied, request permission
            if (ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_PHOTO)
            }
            //once permission granted, pick Image
            else {
                pickImageFromGallery()
            }
        }
        ///phone OS older than Marshmallow
        else {
            pickImageFromGallery()
        }
    }

    //for multiple image pics:
    private fun pickImageFromGallery() {
        //we intend to pick something
        val intent = Intent(Intent.ACTION_PICK)
        //multiple images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        //we want to pick an image
        intent.type = "image/*"
        //now we know the type, so start the activity
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    //now must override the startActivityResult function so we can return image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == IMAGE_PICK_CODE) {
//            make sure data (in this case the image) is not null
//            https://www.youtube.com/watch?v=ZCs7RFZQ_To
                if (data != null && data.data != null) {

                    //if more than one photo
                    if (data.clipData != null) {
                        val clipData = data.clipData
                        if (clipData != null) {
                            for (photo in 0 until clipData.itemCount) {
                                submitCarViewModel.imgUris.add(clipData.getItemAt(photo).uri)
                            }
                        }
                    }
                    //if user selected only one photo
                    else {
                        //image URI
                        val image = data.data
                        submitCarViewModel.imgUris.add(image!!)
                    }
                }
            } else if (requestCode == CAMERA_CAPTURE_CODE) {
                submitCarViewModel.imgUris.add(camera_uri!!)
            }
        }

        //make a new adapter so new images are loaded on and clear helper text
        submit_photos_recycler.adapter = PhotoAdapter(submitCarViewModel.imgUris, this)
        if (submitCarViewModel.imgUris.isNotEmpty()) {
            submitCarViewModel.photoTextVis.value = View.GONE
            submitCarViewModel.clrBtnVis.value = View.VISIBLE
        }
    }

    //to access Camera, need permission first
    //just like selecting photo from gallery
    fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(
                    requireContext().applicationContext,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(
                    requireContext().applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {

                //permission was not enabled/denied
                val permission =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show pop-up to request permission
                requestPermissions(permission, PERMISSION_CODE_CAMERA)
            } else {
                //permission already granted
                openCamera()
            }
        } else {
            //OS < Marshmallo
            openCamera()
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        camera_uri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, camera_uri)
        startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE)

    }

    //when permission popup is shown and granted access, access image gallery
    //if user denies access, do nothing
    // from: https://www.youtube.com/watch?v=gd300jxLEe0
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE_PHOTO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted so call image gallery method
                    pickImageFromGallery()
                }
                //else permission was denied toast is shown
                else {
                    Toast.makeText(requireContext(), "Permission Photos Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    openCamera()
                } else {
                    //permission from popup was denied
                    Toast.makeText(context, "Permission Camera Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //TODO: Refresh the screen after deleting a photo
    override fun onDeletePhotoClick(position: Int) {
        submitCarViewModel.imgUris.removeAt(position)
        submit_photos_recycler.adapter = PhotoAdapter(submitCarViewModel.imgUris, this)
    }
}