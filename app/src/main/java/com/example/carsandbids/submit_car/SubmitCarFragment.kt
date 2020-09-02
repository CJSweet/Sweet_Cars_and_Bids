package com.example.carsandbids.submit_car


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.carsandbids.R
import com.example.carsandbids.databinding.SubmitCarBinding
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.submit_car.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

//FIXME: more than 80 views in XML, maybe convert to recycler view?
//  or maybe ViewStubs for error messages? (Counted roughly 130 views......wayyyy to many)

/**
 * Class for implementing the UI of the Sell-A-Car Fragment
 *
 * @constructor OnCreateView initiliazes all variables, adapters, and listeners
 */

class SubmitCarFragment : Fragment(), PhotoAdapter.OnDeletePhotoListener,
    AdapterView.OnItemSelectedListener {

    // the parent layout of the add more links section
    private lateinit var parentLayout: LinearLayout

    // for camera access
    var cameraUri: Uri? = null

    // late init the ViewModel for this fragment
    private lateinit var submitCarViewModel: SubmitCarViewModel

    // late init binding var for this fragment
    private lateinit var binding: SubmitCarBinding

    // These values are to help with managing the image selection or camera capture
    companion object {

        // Image pick code
        private const val IMAGE_PICK_CODE = 1000

        // Permission code for gallery photos
        private const val PERMISSION_CODE_PHOTO = 1001

        // Permission code for camera access
        private const val PERMISSION_CODE_CAMERA = 1002

        // when taking picture
        private const val CAMERA_CAPTURE_CODE = 1003

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate binding with this fragment
        binding = SubmitCarBinding.inflate(inflater)

        // get viewmodel
        submitCarViewModel = ViewModelProvider(this).get(SubmitCarViewModel::class.java)

        // To use the View Model with data binding, you have to explicitly
        // give the binding object a reference to it.
        binding.viewModel = submitCarViewModel

        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner


        // add adapter to Recycler
        val adapter = PhotoAdapter(submitCarViewModel.imgBitmaps, this)
        binding.submitPhotosRecycler.adapter = adapter

        // Calculate years list for spinner
        val arrayListYears = arrayListYears()

        // bind title status spinner (code based off of spinner documentation: https://developer.android.com/guide/topics/ui/controls/spinner)
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.title_status_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                // Specify layout to use when list of choices appears
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)
                // apply adapter to spinner
                binding.submitTitleStatusSpinner.adapter = adapter
            }
        binding.submitTitleStatusSpinner.onItemSelectedListener = this

        // bind US state location spinner
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.states_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationUsSpinner.adapter = adapter
            }
        binding.submitTitleLocationUsSpinner.onItemSelectedListener = this

        // bind CAN province location spinner
        ArrayAdapter.createFromResource(
            requireActivity().applicationContext,
            R.array.can_prov_array,
            R.layout.spinner_layout_selected
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitTitleLocationCanSpinner.adapter = adapter
            }
        binding.submitTitleLocationCanSpinner.onItemSelectedListener = this

        // bind year spinner adapter
        ArrayAdapter(
            requireActivity().applicationContext,
            R.layout.spinner_layout_selected,
            arrayListYears
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown)

                binding.submitYearSpinner.adapter = adapter
            }

        // add year spinner item selected listener
        binding.submitYearSpinner.onItemSelectedListener = this

        // add more lines for user to enter more links
        // based on code from: https://www.tutorialspoint.com/add-and-remove-views-in-android-dynamically
        parentLayout = binding.submitMoreLinksLayout
        binding.submitMoreListingLinksText.setOnClickListener {
            moreLinks()
        }

        // apply links back to UI if rebuilt
        recreateLinks()

        // set on click for photo button
        binding.submitSelectPhotosButton.setOnClickListener {
            checkPhotoPermission()
        }

        // set on click for camera button
        binding.submitTakePhotosButton.setOnClickListener {
            checkCameraPermission()
        }

        // on click of clr photos button
        binding.submitClearPhotosButton.setOnClickListener {
            submitCarViewModel.onClrBtnClick()

            binding.submitPhotosRecycler.adapter!!.notifyItemRangeRemoved(
                0,
                submitCarViewModel.imgBitmaps.size
            )
        }

        // onclick dealer radio button, call method for animation
        binding.submitRadioDealer.setOnClickListener {
            openDealerInfo()
        }

        // onclick private seller button, call method for animation
        binding.submitRadioPrivate.setOnClickListener {
            closeDealerInfo()
        }

        // onclick yes for sale else where button, for animation
        binding.submitSaleElsewhereYesRadio.setOnClickListener {
            openMoreLinks()
        }

        // no elsewhere
        binding.submitSaleElsewhereNoRadio.setOnClickListener {
            closeMoreLinks()
        }

        // on submit button click
        binding.submitSubmitButton.setOnClickListener {
            checkValidation()
        }

        return binding.root
    }

    /**
     * Method for recreating links if fragment is recreated, such as during rotation
     *
     * If app is able to rotate, this method should allow for added links to remain on screen
     *
     */
    private fun recreateLinks() {
        if (submitCarViewModel.linksList.isNotEmpty()) {
            for (views in 0 until submitCarViewModel.linksList.size) {
                val parent = submitCarViewModel.linksList[views].parent as ViewGroup
                parent.removeView(submitCarViewModel.linksList[views])
            }
            for (edit in 0 until submitCarViewModel.linksList.size) {
                parentLayout.addView(submitCarViewModel.linksList[edit])
            }
        }
    }

    /**
     * Method to find all the years between 1980 to now
     *
     * This method inclusively adds all years from 1980 till now into a list. This
     * list is returned so it can be applied to the year spinner
     *
     * @return ArrayList<String>
     */
    fun arrayListYears(): ArrayList<String> {
        val years = ArrayList<String>()

        years.add("Choose year")

        for (year in 1980..Calendar.getInstance().get(Calendar.YEAR)) {
            years.add(year.toString())
        }

        return years
    }

    /**
     * On clicking "+ Add more links text" dynamically add links (EditText) to UI
     *
     * When user clicks "+ Add more links text" this method is called to add another EditText
     * below any other editTexts that are in that section. Each EditText except the first one is
     * dynamically created.
     *
     */

    fun moreLinks() {

        // get inflater to inflate a new EditText view
        val inflater =
            this.requireActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val newEditView = inflater!!.inflate(R.layout.submit_more_links_edit_text, null) as EditText

        // get proper dimen value from dimen resource xml
        // from: https://stackoverflow.com/questions/11121028/load-dimension-value-from-res-values-dimension-xml-from-source-code
        val editWidth = resources.getDimension(R.dimen.three_hundred)
        // resources.displayMetrics.density

        // layout params code inspired by: https://stackoverflow.com/questions/47673723/relative-layout-params-in-kotlin
        var param: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(editWidth.toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        param.setMargins(12, 6, 6, 10)
        newEditView.layoutParams = param

        newEditView.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT

        submitCarViewModel.linksList.add(newEditView)

        // Add the new row at end of layout
        parentLayout.addView(newEditView, parentLayout.childCount)
    }

    /**
     * Called when user selects "Delete" text that is under a picture
     *
     */
    override fun onDeletePhotoClick(position: Int) {
        submitCarViewModel.imgBitmaps.removeAt(position)
        binding.submitPhotosRecycler.adapter!!.notifyItemRemoved(position)
        submitCarViewModel.photoTextVisibility()
    }


    /**
     * When user taps for-sale-by-dealer radio button, this animation is called
     *
     * On for-sale-by-dealer radio button click, this small animation happens to drop down
     * the dealer info views. This way, the user knows something has changed in the UI
     *
     */
    private fun openDealerInfo() {
        // TODO: Make the animations more fluid

        // if dealer radio group error is shown, remove it since a radio button has been selected
        submitCarViewModel.seeDealerError(false)
        //if dealer info view is not visible already, then animate
        if (submitCarViewModel.dealerInfoView.value == View.GONE) {
            val dropAnim = AnimationUtils.loadAnimation(this.activity, R.anim.drop_down)
            dropAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    submitCarViewModel.dealerInfoVisible(true)
                }

                override fun onAnimationEnd(animation: Animation?) {
                    println("animation end")
                }

                override fun onAnimationStart(animation: Animation?) {
                    submitCarViewModel.dealerInfoVisible(true)
                }

            })
            binding.submitDealerInfoLayout.startAnimation(dropAnim)
        }
    }

    /**
     * Slide up animation for dealer information
     *
     * If dealer info is already open and user then taps for-sale-by-private-party
     * radio button, this method is called to animate a slide up motion.
     *
     */
    private fun closeDealerInfo() {
        submitCarViewModel.seeDealerError(false)
        if (submitCarViewModel.dealerInfoView.value == View.VISIBLE) {
            val upAnim = AnimationUtils.loadAnimation(this.activity, R.anim.slide_up)
            upAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    submitCarViewModel.dealerInfoVisible(false)
                }

                override fun onAnimationEnd(animation: Animation?) {
                    submitCarViewModel.dealerInfoVisible(false)
                }

                override fun onAnimationStart(animation: Animation?) {
                    println("animation start")
                }

            })
            binding.submitDealerInfoLayout.startAnimation(upAnim)
        }
    }

    /**
     * The Opening more links ViewGroup animation
     *
     * Very similar code to open dealer info method
     *
     */
    private fun openMoreLinks() {
        submitCarViewModel.seeSaleError(false)
        if (submitCarViewModel.carListingsView.value == View.GONE) {
            val dropAnim = AnimationUtils.loadAnimation(this.activity, R.anim.drop_down)
            dropAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    submitCarViewModel.carListingsVisible(true)
                    println("Open more links repeated")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    println("animation end")
                }

                override fun onAnimationStart(animation: Animation?) {
                    submitCarViewModel.carListingsVisible(true)
                }

            })
            binding.submitSaleElsewhereLayout.startAnimation(dropAnim)
        }
    }

    /**
     * The closing more links ViewGroup animation
     *
     * Very similar code to closing dealer info method
     *
     */
    private fun closeMoreLinks() {
        submitCarViewModel.seeSaleError(false)
        if (submitCarViewModel.carListingsView.value == View.VISIBLE) {
            val upAnim = AnimationUtils.loadAnimation(this.activity, R.anim.slide_up)
            upAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                    submitCarViewModel.carListingsVisible(false)
                }

                override fun onAnimationEnd(animation: Animation?) {
                    submitCarViewModel.carListingsVisible(false)
                }

                override fun onAnimationStart(animation: Animation?) {
                    println("animation start")
                }

            })
            binding.submitSaleElsewhereLayout.startAnimation(upAnim)
        }
    }

    /**
     * Method to check phone permissions to access image gallery
     *
     * To get a photo from image gallery, first need to check if permission has been
     * granted from user to read external storage.
     * If permission is granted, then call pickImageFromGallery
     * Code is from:
     * https://stackoverflow.com/questions/55933590/select-photo-on-gallery-kotlin
     *
     */
    fun checkPhotoPermission() {
        // version must be greater than Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // if permission denied, request permission
            if (ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission, PERMISSION_CODE_PHOTO)
            }
            // once permission granted, pick Image
            else {
                pickImageFromGallery()
            }
        }
        // phone OS older than Marshmallow
        else {
            pickImageFromGallery()
        }
    }

    /**
     * When picking an image from gallery, this is the method needed
     *
     * This method starts the activity of launching image picker and selecting multiple
     * images from user's phone gallery. This method identifies the intent, the type of file that
     * will be picked, then starts the activity by calling startActivityForResult
     * Code is from:
     * https://stackoverflow.com/questions/55933590/select-photo-on-gallery-kotlin
     *
     */
    private fun pickImageFromGallery() {
        // we intend to pick something
        val intent = Intent(Intent.ACTION_PICK)
        // multiple images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        // we want to pick an image
        intent.type = "image/*"
        // now we know the type, so start the activity
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    /*FIXME: When image is resized, it is added to image gallery, thus, I now have the same image
        but saved twice for two different sizes. only want one
    * */
    /**
     * After permission granted, and intent determined, now must override the startActivityResult function
     *
     * Overriding the onActivityResult function allows us to return image
     * from camera or from gallery, and put into array for viewing and sending to storage.
     * onActivityResult is called after startActivity for result finishes. This method gets the images selected or
     * the camera photo taken, compresses it to 20% quality so it is much smaller memory size, and stores it into
     * Bitmap array to be used to view in photo recyclerview and to be used when sending to google storage.
     * Code is from:
     * https://stackoverflow.com/questions/55933590/select-photo-on-gallery-kotlin
     *
     * @param requestCode the requestCode will determine if the param data is from the gallery or camera
     * @param resultCode the resultCode will determine if the Activity resulted in a success or failure
     * @param data the data is the array of images from the gallery or the camera photo taken
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_CODE) {
                // make sure data (in this case the image) is not null
                // https://www.youtube.com/watch?v=ZCs7RFZQ_To
                if (data != null && data.data != null) {
                    val clipData = data.clipData
                    if (clipData != null) {
                        when {
                            // getBitmap is deprecated after SDK 28
                            Build.VERSION.SDK_INT < 28 -> {
                                for (photo in 0 until clipData.itemCount) {
                                    // get URI from gallery
                                    val imageUri = clipData.getItemAt(photo).uri
                                    // get bitmap from uri
                                    val bitmap = MediaStore.Images.Media.getBitmap(
                                        requireContext().contentResolver, imageUri
                                    )
                                    // create byte array output stream for compression
                                    val stream = ByteArrayOutputStream()
                                    // compress bitmap to 20% quality of original, placing in stream
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                                    // stream to byte array
                                    val byteArray = stream.toByteArray()
                                    // get new bitmap from byte array
                                    val resizeBitmap =
                                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                    // add new resized bitmap to bitmap array
                                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
                                }
                            }
                            else -> {
                                // TODO: Make compressing images faster so UI is not slowed down
                                for (photo in 0 until clipData.itemCount) {
                                    val imageUri = clipData.getItemAt(photo).uri
                                    val source = ImageDecoder.createSource(
                                        requireContext().contentResolver,
                                        imageUri!!
                                    )
                                    val stream = ByteArrayOutputStream()
                                    val bitmap = ImageDecoder.decodeBitmap(source)
                                    bitmap.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        20,
                                        stream
                                    )
                                    val byteArray = stream.toByteArray()
                                    val resizeBitmap = BitmapFactory.decodeByteArray(
                                        byteArray,
                                        0,
                                        byteArray.size
                                    )
                                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
                                    Log.i("SubmitCarFragment", "Photo number: ${photo}")
                                }
                            }
                        }
                    }
                }
            }
            //FIXME: When photo taken in landscape, after accepting picture, app crashes
        } else if (requestCode == CAMERA_CAPTURE_CODE) {
            when {
                Build.VERSION.SDK_INT < 28 -> {
                    val cameraBitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver, cameraUri
                    )
                    val stream = ByteArrayOutputStream()
                    cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                    val byteArray = stream.toByteArray()
                    val resizeBitmap =
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
                }
                else -> {
                    val source =
                        ImageDecoder.createSource(requireContext().contentResolver, cameraUri!!)
                    val cameraBitmap = ImageDecoder.decodeBitmap(source)
                    val stream = ByteArrayOutputStream()
                    cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                    val byteArray = stream.toByteArray()
                    val resizeBitmap =
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
                }
            }
        }

        // make a new adapter so new images are loaded on and clear helper text
        binding.submitPhotosRecycler.adapter!!.notifyDataSetChanged()
        if (submitCarViewModel.imgBitmaps.isNotEmpty()) {
            submitCarViewModel.photoTextVis.value = View.GONE
            submitCarViewModel.clrBtnVis.value = View.VISIBLE
        }
    }

    /**
     * Similar to checkPhotoPermission, must check to see if permission has been granted to
     * camera and write external storage
     *
     * Must check if user has allowed for app access to camera and to write to external storage.
     * Once permission is granted, call openCamera method
     *
     */
    fun checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext().applicationContext,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(
                    requireContext().applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {

                // permission was not enabled/denied
                val permission =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // show pop-up to request permission
                requestPermissions(permission, PERMISSION_CODE_CAMERA)
            } else {
                // permission already granted
                openCamera()
            }
        } else {
            // OS < Marshmallo
            openCamera()
        }
    }

    /**
     * Method to open Camera Activity
     *
     * This method creates a camera Uri that is applied to an intent, then the intent
     * becomes a paramenter for the startActivityResults. I did not make this code myself, I
     * copied it from somewhere but I forgot to cite my source. I do not claim ownership of this code.
     *
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        cameraUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        // camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        startActivityForResult(cameraIntent, CAMERA_CAPTURE_CODE)

    }

    /**
     * When permission popup is shown and granted access, access image gallery or camera
     *
     * When request permissions returns a value, either pickImageFromGallery or openCamera.
     * Code from: https://www.youtube.com/watch?v=gd300jxLEe0
     *
     * @param requestCode this determines if permission has been granted to image gallery or camera
     * @param permissions
     * @param grantResults this array stores the results of the permission. Whether they have been granted or not
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE_PHOTO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from popup granted so call image gallery method
                    pickImageFromGallery()
                }
                // else permission was denied toast is shown
                else {
                    Toast.makeText(requireContext(), "Permission Photos Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from popup was granted
                    openCamera()
                } else {
                    // permission from popup was denied
                    Toast.makeText(context, "Permission Camera Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * When submit button clicked, validate, then if valid, send info to database
     *
     * On submit button click, this method is called. First it calls the validate function, and if
     * validation come back true, then the user entered data is formatted into proper data classes to be
     * sent to the database. Show an alert dialog telling the user their info has been submitted
     *
     */
    private fun checkValidation() {
        // validate that all fields have been entered properly
        val validation = validate()

        // if validation true, submitinfo and show an alert dialog letting the user know the info is
        // on it's way to being processed
        if (validation) {
            // Show alert dialog saying all info has been submitted
            val submittedDialog = AlertDialog.Builder(this.requireContext())
            submittedDialog.setTitle("Car Submitted!")
            submittedDialog.setMessage("After reviewing your car, we will send you an email letting you know if we will host your car on Cars & Bids.")
            submittedDialog.setPositiveButton("Ok", { dialog: DialogInterface, which: Int ->
                val action = SubmitCarFragmentDirections.actionSubmitCarFragmentToMainFragment()
                NavHostFragment.findNavController(this).navigate(action)
            })
            submittedDialog.setCancelable(false)
            submittedDialog.show()
            // TODO: proper UI and colors for submitted alert dialog

            // TODO: send email to user, with all information that they submitted about their car to confirm we received the information

            //begin submitting the info by organizing data into proper data classes
            submitInfo()

        } else {

            // TODO: Create custom background that looks like normal Toast, just black instead of grey background

            // create custom Toast so the grey default background does not blend in with rest of
            // code from: https://stackoverflow.com/questions/31175601/how-can-i-change-default-toast-message-color-and-background-color-in-android
            val invalidToast =
                Toast.makeText(this.requireContext(), "Invalid entries", Toast.LENGTH_SHORT)
            val invalidView = invalidToast.view
            invalidView.setBackgroundColor(Color.BLACK)
            val invalidText = invalidView.findViewById<TextView>(android.R.id.message)
            invalidText.setTextColor(Color.WHITE)

            invalidToast.show()

        }
    }

    /**
     * Once validation is confirmed, this method is called to prep entered info for sending to database
     *
     * Entered info is put into it's respective data classes (from the InfoClass.kt file). Once all info has been stored,
     * the upload method is called from the viewModel so the uploading process can begin
     *
     */
    private fun submitInfo() {
        // Get the car location string
        val carLoc: String
        if (binding.submitCarLocationUsRadio.isChecked) {
            carLoc = "US " + binding.submitCarLocationUsZipEdit.editText!!.text.trim().toString()
        } else {
            carLoc = binding.submitCarLocationCanEdit.editText!!.text.trim().toString()
        }

        // Get the title location string
        val titleLoc: String
        if (binding.submitTitleLocationUsRadio.isChecked) {
            titleLoc = binding.submitTitleLocationUsSpinner.selectedItem.toString()
        } else {
            titleLoc = binding.submitTitleLocationCanSpinner.selectedItem.toString()
        }

        // Gather list of strings for the links provide if car is being sold elsewhere
        val listOfLinks = ArrayList<String>()
        listOfLinks.add(binding.submitLinkOtherListingsEdit.editText?.text?.trim().toString())

        for (link in 0 until submitCarViewModel.linksList.size) {
            listOfLinks.add(submitCarViewModel.linksList[link].text.trim().toString())
        }

        // Make yourInfo data class
        val yourInfo = YourInfo(
            binding.submitYourNameEdit.editText!!.text.trim().toString(),
            binding.submitPhoneEdit.editText!!.text.trim().toString(),
            binding.submitAdditionalFeesEdit.editText?.text?.trim().toString(),
            binding.submitDealerNameEdit.editText?.text?.trim().toString(),
            binding.submitDealerWebsiteEdit.editText?.text?.trim().toString()
        )

        // Make carDetails data class
        val carDetails = CarDetails(
            listOfLinks,
            binding.submitYearSpinner.selectedItem.toString(),
            binding.submitMakeEdit.editText!!.text.trim().toString(),
            binding.submitModelEdit.editText!!.text.trim().toString(),
            binding.submitVinEdit.editText!!.text.trim().toString(),
            binding.submitMileageEdit.editText!!.text.trim().toString(),
            carLoc,
            binding.submitNoteworthyEdit.editText!!.text.trim().toString(),
            binding.submitCarModifiedEdit.editText?.text?.trim().toString()
        )

        // Make title info data class
        val titleInfo = TitleInfo(
            titleLoc,
            binding.submitNameOnTitleEdit.editText?.text?.trim().toString(),
            binding.submitLienholderRadioYes.isChecked,
            binding.submitTitleStatusSpinner.selectedItem.toString()
        )

        // Make reservePrice data class
        val reservePrice = ReservePrice(
            binding.submitReserveAPriceEdit.editText?.text?.trim().toString()
        )

        // Make referral data class
        val referral = Referral(
            binding.submitReferralEdit.text?.trim().toString()
        )

        // Upload image to storage, then once complete, upload information to database
        submitCarViewModel.uploadInfo(
            requireContext().applicationContext,
            yourInfo,
            carDetails,
            titleInfo,
            reservePrice,
            referral
        )
    }

    /**
     * Method to check if all fields have been filled out properly
     *
     * Starting at top of submit page and works down, field by field. If any view is incomplete, or improperly
     * filled out an error is displayed to the user and the validation variable is set to false. If validation all views are properly filled out,
     * return true. Else, return false
     *
     * @return validation - the boolean to determine if errors have occured or not
     */
    private fun validate(): Boolean {
        // boolean that is init to true, if any errors, set to false
        var validation: Boolean = true

        // PERSON INFO

        // Dealer info
        // if neither dealer nor private party selected, flag it
        if (binding.submitDealerRadioGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeDealerError(true)
            validation = false
        }
        // if dealer radio button selected, check to make sure all dealer fields have been
        // filled out properly
        else if (binding.submitDealerRadioGroup.checkedRadioButtonId == binding.submitRadioDealer.id) {
            // additional fees
            if (binding.submitAdditionalFeesEdit.editText?.text.toString().trim().isEmpty()) {
                binding.submitAdditionalFeesEdit.error = "* Please specify fees, or enter no"
                validation = false
            } else {
                // if validation okay, set error to null so it does not show or disappears
                binding.submitAdditionalFeesEdit.isErrorEnabled = false
            }
            // Dealer name
            if (binding.submitDealerNameEdit.editText?.text.toString().isEmpty()) {
                binding.submitDealerNameEdit.error = "* Please enter dealer name"
                validation = false
            } else {
                binding.submitDealerNameEdit.isErrorEnabled = false
            }
            // Dealer website
            val dealweb = binding.submitDealerWebsiteEdit.editText?.text.toString().trim()
            if (dealweb.isEmpty()) {
                binding.submitDealerWebsiteEdit.error = "* Please enter dealer site"
                validation = false
            }
            // does not match the URL pattern
            else if (!Patterns.WEB_URL.matcher(dealweb).matches()) {
                binding.submitDealerWebsiteEdit.error = "* Please enter proper website"
                validation = false
            } else {
                binding.submitDealerWebsiteEdit.isErrorEnabled = false
            }

        }

        // Seller name
        val name = binding.submitYourNameEdit.editText?.text.toString().trim()
        if (name.isEmpty()) {
            binding.submitYourNameEdit.error = "* Please enter a name"
            validation = false
        } else {
            binding.submitYourNameEdit.isErrorEnabled = false
        }

        // Seller contact number
        val contnumber = binding.submitPhoneEdit.editText?.text.toString().trim()
        if (contnumber.isEmpty()) {
            binding.submitPhoneEdit.error = "* Please enter a phone number"
            validation = false
        } else if (!Patterns.PHONE.matcher(contnumber).matches()) {
            binding.submitPhoneEdit.error = "* Invalid phone number"
            validation = false
        } else {
            binding.submitPhoneEdit.isErrorEnabled = false
        }


        // CAR DETAILS

        // car sale elsewhere
        if (binding.submitSaleElsewhereGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeSaleError(true)
            validation = false
        } else if (binding.submitSaleElsewhereGroup.checkedRadioButtonId == binding.submitSaleElsewhereYesRadio.id) {
            if (binding.submitLinkOtherListingsEdit.editText!!.text.trim().toString().isEmpty()) {
                binding.submitLinkOtherListingsEdit.error = "* Please enter a website link"
                validation = false
            } else if (!Patterns.WEB_URL.matcher(
                    binding.submitLinkOtherListingsEdit.editText!!.text.trim().toString()
                ).matches()
            ) {
                binding.submitLinkOtherListingsEdit.error = "* Please enter a proper website link"
                validation = false
            } else {
                binding.submitLinkOtherListingsEdit.isErrorEnabled = false
            }
            // Do not care to check errors on the other additional links, because user may have made more than needed.
        } else {
            binding.submitLinkOtherListingsEdit.isErrorEnabled = false
        }

        // Year
        if (binding.submitYearSpinner.selectedItem == "Choose year") {
            submitCarViewModel.seeYearError(true)
            validation = false
        }

        // Make
        if (binding.submitMakeEdit.editText?.text.toString().trim().isEmpty()) {
            binding.submitMakeEdit.error = "* Please enter make"
            validation = false
        } else {
            binding.submitMakeEdit.isErrorEnabled = false
        }

        // Model
        if (binding.submitModelEdit.editText?.text.toString().trim().isEmpty()) {
            binding.submitModelEdit.error = "* Please enter model"
            validation = false
        } else {
            binding.submitModelEdit.isErrorEnabled = false
        }

        // VIN
        if (binding.submitVinEdit.editText?.text.toString().trim().isEmpty()) {
            binding.submitVinEdit.error = "* Please enter VIN"
            validation = false
        } else {
            binding.submitVinEdit.isErrorEnabled = false
        }

        // Mileage
        if (binding.submitMileageEdit.editText?.text.toString().trim().isEmpty()) {
            binding.submitMileageEdit.error = "* Please enter mileage"
            validation = false
        } else {
            binding.submitMileageEdit.isErrorEnabled = false
        }

        // Car Location Radio`
        if (binding.submitCarLocationGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeCarLocError(true)
            validation = false
        } else if (binding.submitCarLocationGroup.checkedRadioButtonId == binding.submitCarLocationUsRadio.id) {
            if (binding.submitCarLocationUsZipEdit.editText?.text.toString().trim().isEmpty()) {
                binding.submitCarLocationUsZipEdit.error = "* Must enter a Zip code"
                validation = false
            } else if (binding.submitCarLocationUsZipEdit.editText?.text.toString()
                    .trim().length < 5
            ) {
                binding.submitCarLocationUsZipEdit.error = "* Entered Zip is to short"
                validation = false
            } else {
                binding.submitCarLocationUsZipEdit.isErrorEnabled = false
            }
        } else if (binding.submitCarLocationGroup.checkedRadioButtonId == binding.submitCarLocationCanRadio.id) {
            if (binding.submitCarLocationCanEdit.editText?.text.toString().trim().isEmpty()) {
                binding.submitCarLocationCanEdit.error = "* Please enter city/province"
                validation = false
            } else {
                binding.submitCarLocationCanEdit.isErrorEnabled = false
            }
        }

        // Noteworthy Features and Options
        if (binding.submitNoteworthyEdit.editText?.text.toString().isEmpty()) {
            binding.submitNoteworthyEdit.error = "* Please provide some features"
            validation = false
        } else {
            binding.submitNoteworthyEdit.isErrorEnabled = false
        }

        // Been modified radio group
        if (binding.submitModifiedGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeCarModError(true)
            validation = false
        } else if (binding.submitModifiedGroup.checkedRadioButtonId == binding.submitModifiedRadio.id) {
            if (binding.submitCarModifiedEdit.editText?.text.toString().trim().isEmpty()) {
                binding.submitCarModifiedEdit.error = "* Please specify modifications"
                validation = false
            } else {
                binding.submitCarModifiedEdit.isErrorEnabled = false
            }
        }

        // TITLE INFO
        // where is car titled radio
        if (binding.submitTitleLocationGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeTitleLocError(true)
            validation = false
        } else if (binding.submitTitleLocationGroup.checkedRadioButtonId == binding.submitTitleLocationUsRadio.id) {
            if (binding.submitTitleLocationUsSpinner.selectedItem == "Select state") {
                submitCarViewModel.seeTitleLocSpinError(true)
                validation = false
            }
        } else if (binding.submitTitleLocationGroup.checkedRadioButtonId == binding.submitTitleLocationCanRadio.id) {
            if (binding.submitTitleLocationCanSpinner.selectedItem == "Select a province") {
                submitCarViewModel.seeTitleLocSpinError(true)
                validation = false
            }
        }

        // Title in who's name?
        // dont care about if user name is on the title, only check if
        // radio unchecked, or if user name not on title
        if (binding.submitTitleWhosNameGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeTitleNameError(true)
            validation = false
        } else if (binding.submitTitleWhosNameGroup.checkedRadioButtonId == binding.submitTitleWhosNameRadioNo.id) {
            if (binding.submitNameOnTitleEdit.editText?.text.toString().isEmpty()) {
                binding.submitNameOnTitleEdit.error = "* Please enter the name on title"
                validation = false
            } else {
                binding.submitNameOnTitleEdit.isErrorEnabled = false
            }
        }

        // lienholder
        if (binding.submitLienholderGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeLienError(true)
            validation = false
        }

        // Title status
        if (binding.submitTitleStatusSpinner.selectedItem == "Choose") {
            submitCarViewModel.seeTitleStatusError(true)
            validation = false
        }


        // RESERVE PRICE
        if (binding.submitReserveGroup.checkedRadioButtonId == -1) {
            submitCarViewModel.seeReserveError(true)
            validation = false
        } else if (binding.submitReserveGroup.checkedRadioButtonId == binding.submitReserveRadioYes.id) {
            if (binding.submitReserveAPriceEdit.editText?.text.toString().isEmpty()) {
                binding.submitReserveAPriceEdit.error = "* Please specify reserve"
                validation = false
            } else {
                binding.submitReserveAPriceEdit.isErrorEnabled = false
            }
        }

        // PHOTOS
        // must be between 8-16 photos
        if (submitCarViewModel.imgBitmaps.size > 16) {
            submitCarViewModel.seePhotoError(true)
            binding.submitPhotosError.setText(getString(R.string.too_many_photos_error))
            validation = false
        } else if (submitCarViewModel.imgBitmaps.size < 8) {
            submitCarViewModel.seePhotoError(true)
            binding.submitPhotosError.setText(getString(R.string.not_enough_photos_error))
            validation = false
        } else {
            submitCarViewModel.seePhotoError(false)
        }

        // Validation for referral not necessary

        return validation
    }

    // onNothingSelected and onItemSelected for Spinner selections
    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nothing to do
    }

    /**
     * For the spinner selections interface
     *
     * OnItemSelected is called when an item in a spinner is selected. In this case, the method determines which spinner
     * has had an item selected, and if the item is not the default item then the error message is set to visibility GONE.
     * This way errors are not present after user selects a proper item
     *
     * @param parent The spinner of the item selected
     * @param view The view of the item selected
     * @param position The position of the item selected in the spineer
     * @param id The id of the item selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            binding.submitYearSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeYearError(false)
            }
            binding.submitTitleLocationUsSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeTitleLocSpinError(false)
            }
            binding.submitTitleLocationCanSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeTitleLocSpinError(false)
            }
            binding.submitTitleStatusSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeTitleStatusError(false)
            }
        }
    }
}
