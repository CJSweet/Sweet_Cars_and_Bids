package com.example.carsandbids.submit_car

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.example.carsandbids.R
import com.example.carsandbids.databinding.SubmitCarBinding
import com.example.carsandbids.links
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.more_links_edit_text.*
import kotlinx.android.synthetic.main.submit_car.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

//FIXME: more than 80 views in XML, maybe convert to recycler view?
//TODO: Document variables and functions/methods

class SubmitCarFragment : Fragment(), PhotoAdapter.OnDeletePhotoListener, AdapterView.OnItemSelectedListener {

    //for adding more links
    private lateinit var parentLayout: LinearLayout

    //for camera access
    var cameraUri: Uri? = null

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


        //add adapter to Recycler
        val adapter = PhotoAdapter(submitCarViewModel.imgBitmaps, this)
        binding.submitPhotosRecycler.adapter = adapter

        //call more links as many times as the number of viewModel._extraLinks
        // that way through rotation, the number of UI elements are saved
        for (link in 0 until links) {
            moreLinks()
        }

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
        binding.submitTitleStatusSpinner.onItemSelectedListener = this

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
        binding.submitTitleLocationUsSpinner.onItemSelectedListener = this

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
        binding.submitTitleLocationCanSpinner.onItemSelectedListener = this

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
        //add year spinner item selected listener
        binding.submitYearSpinner.onItemSelectedListener = this

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

            binding.submitPhotosRecycler.adapter!!.notifyItemRangeRemoved(
                0,
                submitCarViewModel.imgBitmaps.size
            )
        }

        //onclick dealer radio button, call method for animation
        binding.submitRadioDealer.setOnClickListener {
            openDealerInfo()
        }

        //onclick private seller button, call method for animation
        binding.submitRadioPrivate.setOnClickListener {
            closeDealerInfo()
        }

        //onclick yes for sale else where button, for animation
        binding.submitSaleElsewhereYesRadio.setOnClickListener {
            openMoreLinks()
        }

        //no elsewhere
        binding.submitSaleElsewhereNoRadio.setOnClickListener {
            closeMoreLinks()
        }

        //on submit button click
        binding.submitSubmitButton.setOnClickListener {
            submitInfo()
        }

        return binding.root
    }

    // figure out the years array needed for array spinner
    // TODO: Maybe need to include the upcoming year for brand new models?
    fun arrayListYears(): ArrayList<String> {

        val years = ArrayList<String>()

        years.add("Choose year")

        for (year in 1980..Calendar.getInstance().get(Calendar.YEAR)) {
            years.add(year.toString())
        }

        return years
    }

    // on clicking "+ Add more links text" dynamically add links (EditText) to UI
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
    //code for this method and following 2 methods from:
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
    // from camera or from gallery, and put into array for viewing and sending to storage
    /*FIXME: When image is resized, it is added to image gallery, thus, I now have the same image
        but saved twice for two different sizes
    * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == IMAGE_PICK_CODE) {
//            make sure data (in this case the image) is not null
//            https://www.youtube.com/watch?v=ZCs7RFZQ_To
                if (data != null && data.data != null) {
                    val clipData = data.clipData
                    if (clipData != null) {
                        when {
                            //getBitmap is deprecated after SDK 28
                            Build.VERSION.SDK_INT < 28 -> {
                                for (photo in 0 until clipData.itemCount) {
                                    //get URI from gallery
                                    val imageUri = clipData.getItemAt(photo).uri
                                    //get bitmap from uri
                                    val bitmap = MediaStore.Images.Media.getBitmap(
                                        requireContext().contentResolver, imageUri
                                    )
                                    //create byte array output stream for compression
                                    val stream = ByteArrayOutputStream()
                                    //compress bitmap to 20% quality of original, placing in stream
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                                    //stream to byte array
                                    val byteArray = stream.toByteArray()
                                    // get new bitmap from byte array
                                    val resizeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                    //add new resized bitmap to bitmap array
                                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
                                }

                            }
                            else -> {
                                for (photo in 0 until clipData.itemCount) {
                                    val imageUri = clipData.getItemAt(photo).uri
                                    val source = ImageDecoder.createSource(
                                        requireContext().contentResolver,
                                        imageUri!!
                                    )
                                    val stream = ByteArrayOutputStream()
                                    val bitmap = ImageDecoder.decodeBitmap(source)
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                                    val byteArray = stream.toByteArray()
                                    val resizeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                                    submitCarViewModel.imgBitmaps.add(resizeBitmap)
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
                        val resizeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        submitCarViewModel.imgBitmaps.add(resizeBitmap)
                    }
                    else -> {
                        val source =
                            ImageDecoder.createSource(requireContext().contentResolver, cameraUri!!)
                        val cameraBitmap = ImageDecoder.decodeBitmap(source)
                        val stream = ByteArrayOutputStream()
                        cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
                        var byteArray = stream.toByteArray()
                        val resizeBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        submitCarViewModel.imgBitmaps.add(resizeBitmap)
                    }
                }
            }
        }

        //make a new adapter so new images are loaded on and clear helper text
        submit_photos_recycler.adapter!!.notifyDataSetChanged()
        if (submitCarViewModel.imgBitmaps.isNotEmpty()) {
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
        cameraUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
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

    //called when user selects a delete text in photos
    override fun onDeletePhotoClick(position: Int) {
        submitCarViewModel.imgBitmaps.removeAt(position)
        binding.submitPhotosRecycler.adapter!!.notifyItemRemoved(position)
        submitCarViewModel.photoTextVisibility()
    }

    //TODO: Make the animations more fluid
    private fun openDealerInfo() {
        submitCarViewModel.seeDealerError(false)
        if(submitCarViewModel.dealerInfoView.value == View.GONE){
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

    // close dealer info animation
    private fun closeDealerInfo() {
        submitCarViewModel.seeDealerError(false)
        if(submitCarViewModel.dealerInfoView.value == View.VISIBLE){
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

    //opening links animation
    private fun openMoreLinks() {
        submitCarViewModel.seeSaleError(false)
        if(submitCarViewModel.carListingsView.value == View.GONE){
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

    //more links closing animation
    private fun closeMoreLinks() {
        submitCarViewModel.seeSaleError(false)
        if (submitCarViewModel.carListingsView.value == View.VISIBLE){
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

    //When submit button clicked, validate, then if valid, send info to database
    private fun submitInfo(){
        //validate()

        submitCarViewModel.uploadImage(requireContext().applicationContext)
    }

    //Method to check if all fields have been filled out properly
    //starts at top of submit page and works down, field by field
    private fun validate(){
        //this string will be concatenated with non-edit Text fields that need to be selected
        var invalidString = "Please fill out these fields: \n"

        // PERSON INFO

        //Dealer info
        //if neither dealer nor private party selected, flag it
        if(binding.submitDealerRadioGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeDealerError(true)
        }
        //if dealer radio button selected, check to make sure all dealer fields have been
        // filled out properly
        else if (binding.submitDealerRadioGroup.checkedRadioButtonId == submit_radio_dealer.id){
            //additional fees
            if (binding.submitAdditionalFeesEdit.editText?.text.toString().trim().isEmpty()){
                binding.submitAdditionalFeesEdit.error = "* Please specify fees, or enter no"
            }
            else{
                //if validation okay, set error to null so it does not show or disappears
                binding.submitAdditionalFeesEdit.isErrorEnabled = false
            }
            //Dealer name
            if(binding.submitDealerNameEdit.editText?.text.toString().isEmpty()){
                binding.submitDealerNameEdit.error = "* Please enter dealer name"
            }
            else{
                binding.submitDealerNameEdit.isErrorEnabled = false
            }
            //Dealer website
            val dealweb = binding.submitDealerWebsiteEdit.editText?.text.toString().trim()
            if(dealweb.isEmpty()){
                binding.submitDealerWebsiteEdit.error = "* Please enter dealer site"
            }
            //does not match the URL pattern
            else if (!Patterns.WEB_URL.matcher(dealweb).matches()){
                binding.submitDealerWebsiteEdit.error = "* Please enter proper website"
            }
            else{
                binding.submitDealerWebsiteEdit.isErrorEnabled = false
            }

        }

        // Seller name
        val name = binding.submitYourNameEdit.editText?.text.toString().trim()
        if(name.isEmpty()){
            binding.submitYourNameEdit.error = "* Please enter a name"
        }
        else{
            binding.submitYourNameEdit.isErrorEnabled = false
        }

        // Seller contact number
        val contnumber = binding.submitPhoneEdit.editText?.text.toString().trim()
        if(contnumber.isEmpty()){
            binding.submitPhoneEdit.error = "* Please enter a phone number"
        }
        else if (!Patterns.PHONE.matcher(contnumber).matches()){
            binding.submitPhoneEdit.error = "* Invalid phone number"
        }
        else{
            binding.submitPhoneEdit.isErrorEnabled = false
        }


        // CAR DETAILS

        //car sale elsewhere
        if(binding.submitSaleElsewhereGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeSaleError(true)
        }
        else if (submit_sale_elsewhere_group.checkedRadioButtonId == submit_sale_elsewhere_yes_radio.id){
            //TODO: validate dynamic links entry
        }
        else{
            binding.submitLinkOtherListingsEdit.isErrorEnabled = false
        }

        //Year
        if (binding.submitYearSpinner.selectedItem == "Choose year"){
            submitCarViewModel.seeYearError(true)
        }

        //Make
        if (binding.submitMakeEdit.editText?.text.toString().trim().isEmpty()){
            binding.submitMakeEdit.error = "* Please enter make"
        }
        else{
            binding.submitMakeEdit.isErrorEnabled = false
        }

        //Model
        if(binding.submitModelEdit.editText?.text.toString().trim().isEmpty()){
            binding.submitModelEdit.error = "* Please enter model"
        }
        else{
            binding.submitModelEdit.isErrorEnabled = false
        }

        //VIN
        if (binding.submitVinEdit.editText?.text.toString().trim().isEmpty()){
            binding.submitVinEdit.error = "* Please enter VIN"
        }
        else{
            binding.submitVinEdit.isErrorEnabled = false
        }

        //Mileage
        if (binding.submitMileageEdit.editText?.text.toString().trim().isEmpty()){
            binding.submitMileageEdit.error = "* Please enter mileage"
        }
        else{
            binding.submitMileageEdit.isErrorEnabled = false
        }

        //Car Location Radio`
        if(binding.submitCarLocationGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeCarLocError(true)
        }
        else if (binding.submitCarLocationGroup.checkedRadioButtonId == binding.submitCarLocationUsRadio.id){
            if (binding.submitCarLocationUsZipEdit.editText?.text.toString().trim().isEmpty()){
                binding.submitCarLocationUsZipEdit.error ="* Must enter a Zip code"
            }
            else if (binding.submitCarLocationUsZipEdit.editText?.text.toString().trim().length < 5){
                binding.submitCarLocationUsZipEdit.error = "* Entered Zip is to short"
            }
            else{
                binding.submitCarLocationUsZipEdit.isErrorEnabled = false
            }
        }
        else if (binding.submitCarLocationGroup.checkedRadioButtonId == binding.submitCarLocationCanRadio.id){
            if (binding.submitCarLocationCanEdit.editText?.text.toString().trim().isEmpty()){
                binding.submitCarLocationCanEdit.error = "* Please enter city/province"
            }
            else{
                binding.submitCarLocationCanEdit.isErrorEnabled = false
            }
        }

        //Noteworthy Features and Options
        if(binding.submitNoteworthyEdit.editText?.text.toString().isEmpty()){
            binding.submitNoteworthyEdit.error = "* Please provide some features"
        }
        else {
            binding.submitNoteworthyEdit.isErrorEnabled = false
        }

        //Been modified radio group
        if(binding.submitModifiedGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeCarModError(true)
        }
        else if(binding.submitModifiedGroup.checkedRadioButtonId == binding.submitModifiedRadio.id){
            if (binding.submitCarModifiedEdit.editText?.text.toString().trim().isEmpty()){
                binding.submitCarModifiedEdit.error ="* Please specify modifications"
            }
            else{
                binding.submitCarModifiedEdit.isErrorEnabled = false
            }
        }

        // TITLE INFO
        //where is car titled radio
        if (binding.submitTitleLocationGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeTitleLocError(true)
        }
        else if (binding.submitTitleLocationGroup.checkedRadioButtonId == binding.submitTitleLocationUsRadio.id){
            if (binding.submitTitleLocationUsSpinner.selectedItem == "Select state"){
                submitCarViewModel.seeTitleLocSpinError(true)
            }
        }
        else if (binding.submitTitleLocationGroup.checkedRadioButtonId == binding.submitTitleLocationCanRadio.id){
            if(binding.submitTitleLocationCanSpinner.selectedItem == "Select a province"){
                submitCarViewModel.seeTitleLocSpinError(true)
            }
        }

        //Title in who's name?
        //dont care about if user name is on the title, only check if
        // radio unchecked, or if user name not on title
        if (binding.submitTitleWhosNameGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeTitleNameError(true)
        }
        else if (binding.submitTitleWhosNameGroup.checkedRadioButtonId == binding.submitTitleWhosNameRadioNo.id){
            if (binding.submitNameOnTitleEdit.editText?.text.toString().isEmpty()){
                binding.submitNameOnTitleEdit.error = "* Please enter the name on title"
            }
            else{
                binding.submitNameOnTitleEdit.isErrorEnabled = false
            }
        }

        //lienholder
        if(binding.submitLienholderGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeLienError(true)
        }

        //Title status
        if(binding.submitTitleStatusSpinner.selectedItem == "Choose"){
            submitCarViewModel.seeTitleStatusError(true)
        }


        //RESERVE PRICE
        if(binding.submitReserveGroup.checkedRadioButtonId == -1){
            submitCarViewModel.seeReserveError(true)
        }
        else if (binding.submitReserveGroup.checkedRadioButtonId == binding.submitReserveRadioYes.id){
            if(binding.submitReserveAPriceEdit.editText?.text.toString().isEmpty()){
                binding.submitReserveAPriceEdit.error = "* Please specify reserve"
            }
            else {
                binding.submitReserveAPriceEdit.isErrorEnabled = false
            }
        }

        //PHOTOS
        //must be between 8-16 photos
        if (submitCarViewModel.imgBitmaps.size > 16){
            submitCarViewModel.seePhotoError(true)
            binding.submitPhotosError.setText(getString(R.string.too_many_photos_error))
        }
        else if (submitCarViewModel.imgBitmaps.size < 8){
            submitCarViewModel.seePhotoError(true)
            binding.submitPhotosError.setText(getString(R.string.not_enough_photos_error))
        }
        else{
            submitCarViewModel.seePhotoError(false)
        }

        //Validation for referral not necessary
    }

    //onNothingSelected and onItemSelected for Spinner selections
    override fun onNothingSelected(parent: AdapterView<*>?) {
        //nothing to do
    }

    //if any item except for the first item is selected in any spinner, make sure
    // visibility of errors are gone
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent!!.id){
            binding.submitYearSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeYearError(false)
            }
            binding.submitTitleLocationUsSpinner.id ->{
                if(position != 0)
                    submitCarViewModel.seeTitleLocSpinError(false)
            }
            binding.submitTitleLocationCanSpinner.id ->{
                if(position != 0)
                    submitCarViewModel.seeTitleLocSpinError(false)
            }
            binding.submitTitleStatusSpinner.id -> {
                if (position != 0)
                    submitCarViewModel.seeTitleStatusError(false)
            }
        }
    }
}
