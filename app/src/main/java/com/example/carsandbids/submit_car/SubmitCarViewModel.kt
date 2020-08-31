package com.example.carsandbids.submit_car

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class SubmitCarViewModel : ViewModel() {

    private val _dealerInfoView = MutableLiveData<Int>()
    val dealerInfoView: LiveData<Int>
        get() = _dealerInfoView

    private val _carListingsView = MutableLiveData<Int>()
    val carListingsView: LiveData<Int>
        get() = _carListingsView

    private val _carLocationUSView = MutableLiveData<Int>()
    val carLocationUSView: LiveData<Int>
        get() = _carLocationUSView

    private val _carLocationCanView = MutableLiveData<Int>()
    val carLocationCanView: LiveData<Int>
        get() = _carLocationCanView

    private val _carModifiedView = MutableLiveData<Int>()
    val carModifiedView: LiveData<Int>
        get() = _carModifiedView

    private val _titleLocUSView = MutableLiveData<Int>()
    val titleLocUSView: LiveData<Int>
        get() = _titleLocUSView

    private val _titleLocCanView = MutableLiveData<Int>()
    val titleLocCanView: LiveData<Int>
        get() = _titleLocCanView

    private val _titleNameView = MutableLiveData<Int>()
    val titleNameView: LiveData<Int>
        get() = _titleNameView

    private val _reserveView = MutableLiveData<Int>()
    val reserveView: LiveData<Int>
        get() = _reserveView


    private val _extraLinksNum = MutableLiveData<Int>()
    val extraLinksNum: LiveData<Int>
        get() = _extraLinksNum

    ///any following variables ending with Error are for
    // managing the viewing of the error textviews
    private val _dealerError = MutableLiveData<Int>()
    val dealerError: LiveData<Int>
        get() = _dealerError

    private val _saleError = MutableLiveData<Int>()
    val saleError: LiveData<Int>
        get() = _saleError

    private val _yearError = MutableLiveData<Int>()
    val yearError: LiveData<Int>
        get() = _yearError

    //to show or not show the car location error radio
    private val _carLocError = MutableLiveData<Int>()
    val carLocError: LiveData<Int>
        get() = _carLocError

    //to show modified radiogroup error
    private val _modGroupError = MutableLiveData<Int>()
    val modGroupError: LiveData<Int>
        get() = _modGroupError

    //to show title location radiogroup error
    private val _titleLocError = MutableLiveData<Int>()
    val titleLocError: LiveData<Int>
        get() = _titleLocError

    //to show the title location spinner error
    private val _titleLocSpinError = MutableLiveData<Int>()
    val titleLocSpinError: LiveData<Int>
        get() = _titleLocSpinError

    //to show the title name error
    private val _titleNameError = MutableLiveData<Int>()
    val titleNameError: LiveData<Int>
        get() = _titleNameError

    //to show the lienholder error
    private val _lienError = MutableLiveData<Int>()
    val lienError: LiveData<Int>
        get() = _lienError

    //to show title status error
    private val _titleStatusError = MutableLiveData<Int>()
    val titleStatusError: LiveData<Int>
        get() = _titleStatusError

    //to show photo error message
    private val _photoError = MutableLiveData<Int>()
    val photoError: LiveData<Int>
        get() = _photoError

    //to show title status error
    private val _reserveError = MutableLiveData<Int>()
    val reserveError: LiveData<Int>
        get() = _reserveError

    //determine if helper text can be displayed on photo
    var photoTextVis = MutableLiveData<Int>()

    //determines if clear button can be displayed for photos
    var clrBtnVis = MutableLiveData<Int>()

    //arraylist for the photos added to submit page
    var imgBitmaps = ArrayList<Bitmap>()

    //Declaring a storage reference to upload images to storage
    private lateinit var storageRef: StorageReference

    //Declaring a firestore reference to upload all data
    private lateinit var firestore: FirebaseFirestore

    //initialize all variable views to GONE
    init {
        _dealerInfoView.value = View.GONE
        _carListingsView.value = View.GONE
        _carLocationUSView.value = View.GONE
        _carLocationCanView.value = View.GONE
        _carModifiedView.value = View.GONE
        _titleLocUSView.value = View.GONE
        _titleLocCanView.value = View.GONE
        _titleNameView.value = View.GONE
        _reserveView.value = View.GONE

        _extraLinksNum.value = 0

        photoTextVis.value = View.VISIBLE
        clrBtnVis.value = View.GONE

        _dealerError.value = View.GONE
        _saleError.value = View.GONE
        _yearError.value = View.GONE
        _carLocError.value = View.GONE
        _modGroupError.value = View.GONE
        _titleLocError.value = View.GONE
        _titleLocSpinError.value = View.GONE
        _titleNameError.value = View.GONE
        _lienError.value = View.GONE
        _titleStatusError.value = View.GONE
        _reserveError.value = View.GONE
        _photoError.value = View.GONE

        //initialize storage reference
        storageRef = FirebaseStorage.getInstance().getReference("images")

        //initialize firestore reference
        firestore = Firebase.firestore
    }

    //if user selects dealer as seller, then display dealer info views
    fun dealerInfoVisible(showDealerInfo: Boolean) {

        if (!showDealerInfo) {
            _dealerInfoView.value = View.GONE

        } else {
            _dealerInfoView.value = View.VISIBLE
        }
    }

    //if user selects car is for sale elsewhere, show elsewhere warning and views
    fun carListingsVisible(showCarListings: Boolean) {

        if (!showCarListings)
            _carListingsView.value = View.GONE
        else
            _carListingsView.value = View.VISIBLE
    }

    //when user selects where car is located (US or CAN), show proper views
    // zip code for US and City/Province for CAN
    // parameter true means car is in US
    // parameter false means car is in CAN
    fun carLocationVisible(showCarLocation: Boolean) {
        if (showCarLocation) {
            _carLocationUSView.value = View.VISIBLE
            _carLocationCanView.value = View.GONE
        } else {
            //Invisible, instead of View.GONE so layout margins are maintained
            // for the views below
            _carLocationUSView.value = View.INVISIBLE
            _carLocationCanView.value = View.VISIBLE
        }
        //once either button clicked, make sure to hide error if already displayed
        _carLocError.value = View.GONE
    }

    //if car is stock, true
    //if car has been modified, false
    fun isCarStock(carStock: Boolean) {
        if (carStock)
            _carModifiedView.value = View.GONE
        else
            _carModifiedView.value = View.VISIBLE

        _modGroupError.value = View.GONE
    }

    //if title in US, true
    //if title in CAN, false
    fun isTitleInUS(titleLoc: Boolean) {
        if (titleLoc) {
            _titleLocUSView.value = View.VISIBLE
            _titleLocCanView.value = View.GONE
        } else {
            _titleLocUSView.value = View.INVISIBLE
            _titleLocCanView.value = View.VISIBLE
        }
        _titleLocError.value = View.GONE
    }

    //if title in user name, true
    //if title in other name, false
    fun isTitleInUserName(titleName: Boolean) {
        if (titleName) {
            _titleNameView.value = View.GONE
        } else
            _titleNameView.value = View.VISIBLE

        _titleNameError.value = View.GONE
    }

    fun wantAReserve(wantsReserve: Boolean) {
        if (wantsReserve) {
            _reserveView.value = View.VISIBLE
        } else {
            _reserveView.value = View.GONE
        }
        _reserveError.value = View.GONE
    }

    fun onClrBtnClick() {
        imgBitmaps.clear()
        photoTextVis.value = View.VISIBLE
    }

    fun photoTextVisibility() {
        if (imgBitmaps.isEmpty())
            photoTextVis.value = View.VISIBLE
    }

    fun seeDealerError(see: Boolean) {
        if (see)
            _dealerError.value = View.VISIBLE
        else
            _dealerError.value = View.GONE
    }

    fun seeSaleError(see: Boolean) {
        if (see)
            _saleError.value = View.VISIBLE
        else
            _saleError.value = View.GONE
    }

    fun seeYearError(see: Boolean) {
        if (see)
            _yearError.value = View.VISIBLE
        else
            _yearError.value = View.GONE
    }

    fun seeCarLocError(see: Boolean) {
        if (see)
            _carLocError.value = View.VISIBLE
        else
            _carLocError.value = View.GONE
    }

    fun seeCarModError(see: Boolean) {
        if (see)
            _modGroupError.value = View.VISIBLE
        else
            _modGroupError.value = View.GONE
    }

    fun seeTitleLocError(see: Boolean) {
        if (see)
            _titleLocError.value = View.VISIBLE
        else
            _titleLocError.value = View.GONE
    }

    fun seeTitleLocSpinError(see: Boolean) {
        if (see)
            _titleLocSpinError.value = View.VISIBLE
        else
            _titleLocSpinError.value = View.GONE
    }

    fun seeTitleNameError(see: Boolean) {
        if (see)
            _titleNameError.value = View.VISIBLE
        else
            _titleNameError.value = View.GONE
    }

    fun seeLienError(see: Boolean) {
        if (see)
            _lienError.value = View.VISIBLE
        else
            _lienError.value = View.GONE
    }

    fun seeTitleStatusError(see: Boolean) {
        if (see)
            _titleStatusError.value = View.VISIBLE
        else
            _titleStatusError.value = View.GONE
    }

    fun seeReserveError(see: Boolean) {
        if (see)
            _reserveError.value = View.VISIBLE
        else
            _reserveError.value = View.GONE
    }

    fun seePhotoError(see: Boolean) {
        if (see)
            _photoError.value = View.VISIBLE
        else
            _photoError.value = View.GONE
    }


    private fun getFileExtension(context: Context, uri: Uri): String {
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))!!
    }


    //structure of function from: https://www.youtube.com/watch?v=lPfQN-Sfnjw&t=884s
    fun uploadImage(
        context: Context,
        yourInfo: YourInfo,
        carDetails: CarDetails,
        titleInfo: TitleInfo,
        reservePrice: ReservePrice,
        referral: Referral
    ) {

        /*
            FIXME: Takes a very long time to upload the photos, is there a better, more efficient/faster
                way to do it?
                Can make this run on another dispatcher with right code
         */

        //from documentation
        viewModelScope.launch {
            var imgUrls = ArrayList<String>()

            for (photo in 0 until imgBitmaps.size) {

                //convert bitmap to URI
                //https://freakycoder.com/android-notes-72-how-to-convert-bitmap-to-uri-e535391ebdac
                //title is current time so that way pictures do not override each other more than 32 times, which will throw an exception
                val path = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    imgBitmaps[photo],
                    System.currentTimeMillis().toString(),
                    null
                )

                val uri = Uri.parse(path)

                //since storageRef points to the "images" folder, this no makes a child of images with a name of currentTimeMills() . file extension
                val fileRef = storageRef.child(
                    System.currentTimeMillis().toString() + "." + getFileExtension(
                        context,
                        uri
                    )
                )

                fileRef.putFile(uri)
                    .addOnSuccessListener {

                        val downloadUri = it.uploadSessionUri.toString()
                        imgUrls.add(downloadUri)

                        //if all images have been uploaded, then
                        // upoad all info to firestore database
                        if (imgUrls.size == imgBitmaps.size) {
                            val allInfo = AllInfo(
                                yourInfo,
                                carDetails,
                                titleInfo,
                                reservePrice,
                                imgUrls,
                                referral
                            )

                            val id = System.currentTimeMillis().toString()

                            println("Sent to Firestore")

                            //Upload info to database
                            firestore.collection("Submitted Cars")
                                .document(id)
                                .set(allInfo)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
//                    .addOnProgressListener {
//                        Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
//                    }
            }
        }
    }
}